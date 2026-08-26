package com.fldb.facilita.auto.api.dto.yard;

import com.fldb.facilita.auto.api.dto.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateYardRequest {

    @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres.")
    private String name;

    @Valid
    private AddressDto address;

    private UUID managerId;

    private Boolean isActive;
}