package com.plooh.adssi.dial.data.exception;

import java.text.MessageFormat;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class AddressFormatNotAllowed extends DialDataException {

    private final String message;

    @Override
    public String getMessage() {
        return MessageFormat.format("Address format not allowed: {0}.", message);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.BAD_REQUEST;
    }

}
