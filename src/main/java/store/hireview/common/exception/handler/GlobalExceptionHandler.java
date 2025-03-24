package store.hireview.common.exception.handler;

import store.hireview.common.exception.CustomException;
import store.hireview.common.response.ApiResponse;
import store.hireview.common.util.ErrorMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@Order(-1)
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public Mono<ApiResponse<?>> handleApiException(CustomException e) {
        return Mono.just(ApiResponse.fail(e.getErrorCode().getStatus().value(), e.getMessage()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ApiResponse<?>> handleWebExchangeBindException(WebExchangeBindException e) {
        return Mono.just(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), ErrorMessageUtil.formatFieldErrors(e.getFieldErrors())));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ApiResponse<?>> handleGenericException(Exception e) {
        return Mono.just(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }
}
