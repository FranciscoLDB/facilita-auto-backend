package com.fldb.facilita.auto.api.exception;

import lombok.Getter;

@Getter
public class SystemException extends RuntimeException {

    private final ErrorCode errorCode;

    public SystemException(String message) {
        super(message);
        this.errorCode = null;
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
