package com.plooh.adssi.dial.data.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NotChainHead extends DialDataException {

    @Override
    public String getMessage() {
        return "No chainhead available";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }

}
