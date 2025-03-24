package store.hireview.common.exception;

import store.hireview.common.constant.ResponseMessages;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ClientErrorCode implements ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, ResponseMessages.INVALID_INPUT),
    NOT_FOUND(HttpStatus.NOT_FOUND, ResponseMessages.NOT_FOUND),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, ResponseMessages.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN, ResponseMessages.FORBIDDEN);

    private final HttpStatus status;
    private final String message;

    ClientErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}