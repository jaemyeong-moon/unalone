package com.project.admin.exception;

import org.springframework.http.HttpStatus;

/**
 * 잘못된 요청 예외 (400)
 */
public class InvalidRequestException extends BusinessException {

    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
