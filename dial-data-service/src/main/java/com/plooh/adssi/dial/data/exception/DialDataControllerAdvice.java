package com.plooh.adssi.dial.data.exception;

import com.plooh.adssi.dial.data.domain.ApiError;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class DialDataControllerAdvice {

    private final Clock clock;

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<ApiError> handleFachlicheException(DialDataException e) {
        log.info("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        final var apiError = ApiError.builder()
            .error(e.status().getReasonPhrase())
            .errorMessage(e.getMessage())
            .status(e.status().value())
            .timestamp(OffsetDateTime.now(clock))
            .build();
        return new ResponseEntity<>(apiError, e.status());
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<ApiError> handleException(Exception e) {
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        final var apiError = ApiError.builder()
            .errorMessage(e.getMessage())
            .timestamp(OffsetDateTime.now(clock));
        if (e instanceof HttpMessageNotReadableException
            || e instanceof MethodArgumentNotValidException
            || e instanceof ServletRequestBindingException) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }
        status = Optional.ofNullable(AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class))
            .map(ResponseStatus::value).orElse(status);
        final var response = apiError
            .status(status.value())
            .error(status.getReasonPhrase())
            .build();
        log.warn("Exception: ", e);
        return new ResponseEntity<>(response, status);
    }

}
