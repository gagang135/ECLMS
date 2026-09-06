package com.company.eclms.common.exception;

import org.springframework.http.HttpStatus;

public class AIException extends BusinessException {
    public AIException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public AIException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
