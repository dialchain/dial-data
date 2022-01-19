package com.plooh.adssi.dial.data.domain;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class ApiError {
    private final OffsetDateTime timestamp;
    private final Integer status;
    private final String error;
    private final String errorMessage;
}
