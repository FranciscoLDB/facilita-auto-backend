package com.fldb.facilita.auto.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDetails {
    private String plate;
    private String model;
    private String color;
    private Integer year;
}