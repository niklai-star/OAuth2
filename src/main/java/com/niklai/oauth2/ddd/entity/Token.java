package com.niklai.oauth2.ddd.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niklai.oauth2.utils.CacheUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

@Data
@NoArgsConstructor
public class Token {

    private String accessToken;

    private String refreshToken;

    private String appKey;

    private Long accountId;

    public Token(String accessToken, String refreshToken, String appKey, Long accountId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.appKey = appKey;
        this.accountId = accountId;
    }

    public void putCache(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper)
            throws JsonProcessingException {
        redisTemplate.opsForValue().set(CacheUtils.accessTokenKey(this.accessToken), objectMapper.writeValueAsString(this), CacheUtils.ACCESS_TOKEN_EXPIRE);
        redisTemplate.opsForValue().set(CacheUtils.refreshTokenKey(this.refreshToken), objectMapper.writeValueAsString(this), CacheUtils.REFRESH_TOKEN_EXPIRE);
    }

    public void deleteCache(RedisTemplate redisTemplate) {
        redisTemplate.delete(CacheUtils.accessTokenKey(this.accessToken));
        redisTemplate.delete(CacheUtils.refreshTokenKey(this.refreshToken));
    }
}
