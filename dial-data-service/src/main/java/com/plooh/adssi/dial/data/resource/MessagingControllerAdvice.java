package com.plooh.adssi.dial.data.resource;

import com.plooh.adssi.dial.data.exceptions.ExpiredPopSignature;
import com.plooh.adssi.dial.data.exceptions.InvalidPopSignature;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MessagingControllerAdvice {

    @ExceptionHandler(value = {ExpiredPopSignature.class})
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public void expiredPopSignature() {
    }

    @ExceptionHandler(value = {InvalidPopSignature.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public void invalidPopSignature() {
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public void illegalArgument() {
    }
}
