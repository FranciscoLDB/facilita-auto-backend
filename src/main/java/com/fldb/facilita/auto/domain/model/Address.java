package com.fldb.facilita.auto.domain.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String complement;
}
