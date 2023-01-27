package com.niklai.oauth2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niklai.oauth2.ddd.entity.Account;
import com.niklai.oauth2.ddd.entity.Code;
import com.niklai.oauth2.ddd.entity.Token;
import com.niklai.oauth2.ddd.service.AccountService;
import com.niklai.oauth2.ddd.service.CodeService;
import com.niklai.oauth2.ddd.service.TokenService;
import com.niklai.oauth2.exception.ApiException;
import com.niklai.oauth2.jpa.entity.AccountInfo;
import com.niklai.oauth2.jpa.entity.AppInfo;
import com.niklai.oauth2.jpa.repository.AccountInfoRepository;
import com.niklai.oauth2.jpa.repository.AppInfoRepository;
import com.niklai.oauth2.request.login.LoginWithOauth;
import com.niklai.oauth2.request.token.AccessTokenRequest;
import com.niklai.oauth2.request.token.RefreshTokenRequest;
import com.niklai.oauth2.response.ApiResult;
import com.niklai.oauth2.response.TokenInfo;
import com.niklai.oauth2.utils.CacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
public class OauthController {

    @Autowired
    private CodeService codeService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountInfoRepository accountInfoRepository;

    @Autowired
    private AppInfoRepository appInfoRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity login(@Valid @RequestBody LoginWithOauth loginReqBody)
            throws JsonProcessingException {
        // 验证App
        Optional<AppInfo> appInfoOptional = appInfoRepository.findByAppKeyAndRedirectUrlAndDelFlagFalse(loginReqBody.getClientId(), loginReqBody.getRedirectUrl());
        if (appInfoOptional.isEmpty()) {
            throw ApiException.builder().code("500").msg("App不存在").build();
        }

        // 验证账号
        Optional<AccountInfo> accountInfoOptional = accountInfoRepository.findByAccountAndPasswordAndDelFlagFalse(loginReqBody.getAccount(), loginReqBody.getPassword());
        if (accountInfoOptional.isEmpty()) {
            throw ApiException.builder().code("500").msg("用户名或密码错误").build();
        }

        AppInfo appInfo = appInfoOptional.get();
        Code code = codeService.createNewCode(appInfo.getAppKey(), accountInfoOptional.get().getId());
        code.putCache(redisTemplate, objectMapper);
        if (!Objects.isNull(loginReqBody.getIsRedirect()) && !loginReqBody.getIsRedirect()) {
            HashMap<String, Object> body = new HashMap<>();
            body.put("code", code.getCode());
            if (StringUtils.isNotBlank(loginReqBody.getState())) {
                body.put("state", loginReqBody.getState());
            }

            return ResponseEntity.ok().body(ApiResult.builder().data(body).build());
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(appInfo.getRedirectUrl()).queryParam("code", code.getCode());
        if (!Objects.isNull(loginReqBody.getState())) {
            uriBuilder = uriBuilder.queryParam("state", loginReqBody.getState());
        }

        return ResponseEntity.status(HttpStatus.FOUND).header("Location", uriBuilder.toUriString()).build();
    }

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity token(@Valid @RequestBody AccessTokenRequest atReq)
            throws JsonProcessingException {
        Optional<Code> codeOptional = codeService.getCode(atReq.getCode());
        if (codeOptional.isEmpty()) {
            throw ApiException.builder().code("500").msg("code不存在").build();
        }

        Code code = codeOptional.get();
        if (!code.getAppKey().equals(atReq.getClientId())) {
            throw ApiException.builder().code("500").msg("非法的code").build();
        }

        // 验证App
        Optional<AppInfo> appInfoOptional = appInfoRepository.findByAppKeyAndAppSecretAndRedirectUrlAndDelFlagFalse(atReq.getClientId(), atReq.getClientSecret(), atReq.getRedirectUrl());
        if (appInfoOptional.isEmpty()) {
            throw ApiException.builder().code("500").msg("App不存在或失效").build();
        }

        Token token = tokenService.createNewToken(code.getAppKey(), code.getAccountId());
        token.putCache(redisTemplate, objectMapper);
        code.deleteCache(redisTemplate);
        return ResponseEntity.ok().body(ApiResult.builder().data(
                TokenInfo.builder()
                        .AccessToken(token.getAccessToken())
                        .refreshToken(token.getRefreshToken())
                        .expiresIn(CacheUtils.ACCESS_TOKEN_EXPIRE.getSeconds())
                        .refreshExpiresIn(CacheUtils.REFRESH_TOKEN_EXPIRE.getSeconds())
                        .build()
        ).build());
    }

    @PostMapping(value = "/refreshToken", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity refreshToken(@Valid @RequestBody RefreshTokenRequest reReq)
            throws JsonProcessingException {
        // 校验refreshToken是否存在
        Optional<Token> tokenOptional = tokenService.getByRefreshToken(reReq.getRefreshToken());
        if (tokenOptional.isEmpty()) {
            throw ApiException.builder().code("500").msg("Refresh Token已过期").build();
        }

        Token oldToken = tokenOptional.get();
        // 校验refreshToken关联app是否有效
        Optional<AppInfo> appInfoOptional = appInfoRepository.findByAppKeyAndDelFlagFalse(oldToken.getAppKey());
        if (appInfoOptional.isEmpty()) {
            throw ApiException.builder().code("500").msg("App不存在或失效").build();
        }

        // 校验refreshToken关联用户是否有效
        Optional<AccountInfo> accountInfoOptional = accountInfoRepository.findByIdAndDelFlagFalse(oldToken.getAccountId());
        if (accountInfoOptional.isEmpty()) {
            throw ApiException.builder().code("500").msg("账号不存在或已失效").build();
        }

        // 刷新token，失效旧token
        oldToken.deleteCache(redisTemplate);
        Token newToken = tokenService.createNewToken(oldToken.getAppKey(), oldToken.getAccountId());
        newToken.putCache(redisTemplate, objectMapper);

        return ResponseEntity.ok().body(ApiResult.builder().data(
                TokenInfo.builder()
                        .AccessToken(newToken.getAccessToken())
                        .refreshToken(newToken.getRefreshToken())
                        .expiresIn(CacheUtils.ACCESS_TOKEN_EXPIRE.getSeconds())
                        .refreshExpiresIn(CacheUtils.REFRESH_TOKEN_EXPIRE.getSeconds())
                        .build()
        ).build());
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult> logout(@Valid @NotEmpty @RequestHeader("Access-Token") String accessToken) {
        Optional<Token> tokenOptional = Optional.empty();
        try {
            tokenOptional = tokenService.getByAccessToken(accessToken);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }

        if (tokenOptional.isPresent()) {
            tokenOptional.get().deleteCache(redisTemplate);
        }
        
        return ResponseEntity.ok().body(ApiResult.builder().data(true).build());
    }

    @GetMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult> account(@Valid @NotEmpty @RequestHeader("Access-Token") String accessToken) {
        Optional<Token> tokenOptional = Optional.empty();
        try {
            tokenOptional = tokenService.getByAccessToken(accessToken);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw ApiException.builder().code("500").msg("无效的Access Token").build();
        }

        if (tokenOptional.isEmpty()) {
            throw ApiException.builder().code("500").msg("Access Token已过期").build();
        }

        Optional<Account> accountOptional = accountService.getById(tokenOptional.get().getAccountId());
        if (accountOptional.isEmpty()) {
            throw ApiException.builder().code("500").msg("账号不存在").build();
        }

        return ResponseEntity.ok().body(ApiResult.builder().data(accountOptional.get()).build());
    }
}
