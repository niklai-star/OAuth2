package com.niklai.oauth2.ddd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niklai.oauth2.ddd.entity.Code;
import com.niklai.oauth2.utils.CacheUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CodeService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public Code createNewCode(String appKey, Long userId) {
        return new Code(RandomStringUtils.randomAlphanumeric(8), appKey, userId);
    }

    public Optional<Code> getCode(String code) throws JsonProcessingException {
        String s = redisTemplate.opsForValue().get(CacheUtils.codeKey(code));
        if (StringUtils.isEmpty(s)) {
            return Optional.empty();
        }

        return Optional.of(objectMapper.readValue(s, Code.class));
    }
}
