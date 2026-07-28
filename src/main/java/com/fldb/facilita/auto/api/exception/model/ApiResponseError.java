package com.fldb.facilita.auto.api.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseError {

    private Integer statusCode;
    private String statusMessage;
    private String message;
    private String detailedMessage;

    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();

    private List<ValidationErrorItem> validationErrors;

    private List<GeneralErrorItem> errors;
}
