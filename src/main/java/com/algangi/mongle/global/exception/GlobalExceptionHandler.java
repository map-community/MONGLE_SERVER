package com.algangi.mongle.global.exception;

import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.algangi.mongle.global.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<ErrorInfo>> handleApplicationException(
        ApplicationException exception) {
        log.error(exception.getMessage(), exception);

        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity.status(errorCode.getStatus())
            .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage(),
                ErrorInfo.of(exception.getErrorInfo())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorInfo>> handleIllegalArgumentException(
        IllegalArgumentException exception) {
        log.error(exception.getMessage(), exception);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage()));
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public  ResponseEntity<ApiResponse<ErrorInfo>> handlePessimisticLockingFailure(
        PessimisticLockingFailureException exception) {
        log.error(exception.getMessage(), exception);

        return ResponseEntity.status(HttpStatus.LOCKED)
            .body(ApiResponse.error(HttpStatus.LOCKED.getReasonPhrase(),
                exception.getMessage()));
    }
}
