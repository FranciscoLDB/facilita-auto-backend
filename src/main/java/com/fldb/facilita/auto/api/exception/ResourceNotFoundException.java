package com.fldb.facilita.auto.api.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
