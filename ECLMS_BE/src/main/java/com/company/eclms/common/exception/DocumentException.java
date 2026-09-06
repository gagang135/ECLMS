package com.company.eclms.common.exception;

import org.springframework.http.HttpStatus;

public class DocumentException extends BusinessException {
    public DocumentException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
