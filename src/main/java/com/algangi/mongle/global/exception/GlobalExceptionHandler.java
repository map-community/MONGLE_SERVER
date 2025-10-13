package com.algangi.mongle.global.exception;

import java.util.List;
import java.util.Map;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.algangi.mongle.global.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<ErrorInfo>> handleApplicationException(
        ApplicationException exception) {

        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity.status(errorCode.getStatus())
            .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage(),
                ErrorInfo.of(exception.getErrorInfo())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorInfo>> handleIllegalArgumentException(
        IllegalArgumentException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage()));
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<ErrorInfo>> handlePessimisticLockingFailure(
        PessimisticLockingFailureException exception) {

        return ResponseEntity.status(HttpStatus.LOCKED)
            .body(ApiResponse.error(HttpStatus.LOCKED.getReasonPhrase(),
                exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ErrorInfo>>> handleValidationException(
        MethodArgumentNotValidException exception) {
        List<ErrorInfo> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> ErrorInfo.of(Map.of(
                fieldError.getField(),
                fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage()
                    : "유효하지 않은 값입니다."
            )))
            .toList();

        return ResponseEntity.badRequest()
            .body(ApiResponse.error("VALIDATION_FAILED", "요청 유효성 검사에 실패했습니다.", errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
        MethodArgumentTypeMismatchException exception) {
        String message = String.format("'%s' 파라미터에 유효하지 않은 값이 입력되었습니다.", exception.getName());
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("INVALID_PARAMETER_TYPE", message));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
        HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("HTTP_METHOD_NOT_SUPPORTED", "지원하지 않는 HTTP 메소드입니다."));
    }
}