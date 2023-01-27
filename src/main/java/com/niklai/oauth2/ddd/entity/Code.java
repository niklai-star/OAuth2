package com.niklai.oauth2.ddd.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niklai.oauth2.utils.CacheUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

@Data
@NoArgsConstructor
public class Code {

    private String code;

    private Long accountId;

    private String appKey;

    public Code(String code, String appKey, Long accountId) {
        this.code = code;
        this.appKey = appKey;
        this.accountId = accountId;
    }

    public void putCache(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper)
            throws JsonProcessingException {
        redisTemplate.opsForValue().set(
                CacheUtils.codeKey(this.code),
                objectMapper.writeValueAsString(this),
                CacheUtils.CODE_EXPIRE);
    }

    public void deleteCache(RedisTemplate redisTemplate) {
        redisTemplate.delete(CacheUtils.codeKey(this.code));
    }
}
