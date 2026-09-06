package com.company.eclms.common.exception;

import org.springframework.http.HttpStatus;

public class VendorException extends BusinessException {
    public VendorException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
