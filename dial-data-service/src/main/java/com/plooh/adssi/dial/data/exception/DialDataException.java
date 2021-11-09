package com.plooh.adssi.dial.data.exception;

import org.springframework.http.HttpStatus;

public abstract class DialDataException extends RuntimeException {

    public abstract HttpStatus status();

}