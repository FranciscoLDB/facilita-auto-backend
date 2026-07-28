package com.fldb.facilita.auto.api.exception.model;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorItem {
    private String field;
    private String message;
}
