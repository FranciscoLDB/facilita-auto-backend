package com.fldb.facilita.auto.api.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseData<T> {

    private Integer statusCode;
    private String message;

    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();

    private T data;
}
