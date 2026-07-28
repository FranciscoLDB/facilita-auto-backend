package com.fldb.facilita.auto.api.exception.model;

import com.fldb.facilita.auto.api.exception.ErrorCode;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralErrorItem {

    private ErrorCode code;
    private String message;
}