package com.plooh.adssi.dial.data.exception;

import java.text.MessageFormat;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class AddressNotFound extends DialDataException {

    private final String address;

    @Override
    public String getMessage() {
        return MessageFormat.format("Not transaction found for address {0}.", address);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }

}
