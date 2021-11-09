package com.plooh.adssi.dial.data.exception;

import java.text.MessageFormat;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class BlockNotFound extends DialDataException {

    private final String id;

    @Override
    public String getMessage() {
        return MessageFormat.format("Block with ID {0} not found.", id);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }

}
