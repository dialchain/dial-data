package com.plooh.adssi.dial.data.exception;

import java.text.MessageFormat;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class PayloadLarge extends DialDataException {

    @Override
    public HttpStatus status() {
        return HttpStatus.PAYLOAD_TOO_LARGE;
    }

}
