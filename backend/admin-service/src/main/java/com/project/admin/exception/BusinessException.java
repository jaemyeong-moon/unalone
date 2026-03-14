package com.project.admin.exception;

import org.springframework.http.HttpStatus;

/**
 * 관리자 서비스 비즈니스 예외 기반 클래스
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
