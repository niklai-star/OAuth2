package com.niklai.oauth2.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ApiException extends RuntimeException {
    private String code;

    private String msg;

    public ApiException(String code) {
        this.code = code;
    }

    public ApiException(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ApiException(String code, Throwable throwable) {
        super(throwable);
        this.code = code;
    }
}
