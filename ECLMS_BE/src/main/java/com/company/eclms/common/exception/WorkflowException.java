package com.company.eclms.common.exception;

import org.springframework.http.HttpStatus;

public class WorkflowException extends BusinessException {
    public WorkflowException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
