package com.plooh.adssi.dial.data.exception;

import java.util.Optional;
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
public class DialDataControllerAdvice {

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Void> handleMessagingException(DialDataException e) {
        log.info("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        return new ResponseEntity<>(e.status());
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Void> handleException(Exception e) {
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (e instanceof HttpMessageNotReadableException
            || e instanceof MethodArgumentNotValidException
            || e instanceof ServletRequestBindingException) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }
        status = Optional.ofNullable(AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class))
            .map(ResponseStatus::value).orElse(status);
        log.warn("Exception: ", e);
        return new ResponseEntity<>(status);
    }

}
