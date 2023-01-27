package com.niklai.oauth2.ddd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niklai.oauth2.ddd.entity.Code;
import com.niklai.oauth2.ddd.entity.Token;
import com.niklai.oauth2.utils.CacheUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TokenService {


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public Token createNewToken(String appKey, Long accountId) {
        return new Token(
                RandomStringUtils.randomAlphanumeric(56),
                RandomStringUtils.randomAlphanumeric(56),
                appKey,
                accountId
        );
    }

    public Optional<Token> getByAccessToken(String accessToken) throws JsonProcessingException {
        String s = redisTemplate.opsForValue().get(CacheUtils.accessTokenKey(accessToken));
        if (StringUtils.isEmpty(s)) {
            return Optional.empty();
        }

        return Optional.of(objectMapper.readValue(s, Token.class));
    }

    public Optional<Token> getByRefreshToken(String refreshToken) throws JsonProcessingException {
        String s = redisTemplate.opsForValue().get(CacheUtils.refreshTokenKey(refreshToken));
        if (StringUtils.isEmpty(s)) {
            return Optional.empty();
        }

        return Optional.of(objectMapper.readValue(s, Token.class));
    }
}
