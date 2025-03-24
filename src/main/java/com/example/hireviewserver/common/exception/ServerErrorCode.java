package com.example.hireviewserver.common.exception;

import com.example.hireviewserver.common.constant.ResponseMessages;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ServerErrorCode implements ErrorCode {
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessages.INTERNAL_ERROR),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessages.DATABASE_ERROR);

    private final HttpStatus status;
    private final String message;

    ServerErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}