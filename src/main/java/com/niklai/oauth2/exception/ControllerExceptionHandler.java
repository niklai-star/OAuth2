package com.niklai.oauth2.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.niklai.oauth2.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResult> handlerApiException(ApiException exception) {
        log.error("ApiException", exception.getCause());
        return ResponseEntity.ok().body(ApiResult.builder()
                .code(exception.getCode())
                .msg(exception.getMsg())
                .build()
        );
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiResult> handlerBindException(WebExchangeBindException exception) {
        log.error("WebExchangeBindException", exception);
        return ResponseEntity.ok().body(ApiResult.builder()
                .code(String.valueOf(exception.getStatus().value()))
                .msg("参数错误")
                .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult> handlerDefault(Exception exception) {
        log.error("Default", exception);
        return ResponseEntity.ok().body(ApiResult.builder()
                .code("500")
                .msg(exception.getMessage())
                .build()
        );
    }
}
