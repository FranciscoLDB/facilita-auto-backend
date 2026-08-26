package com.fldb.facilita.auto.api.dto.yard;

import com.fldb.facilita.auto.api.dto.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateYardRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres.")
    private String name;

    @NotNull(message = "O endereço é obrigatório.")
    @Valid
    private AddressDto address;

    private UUID managerId;

    @Builder.Default
    private Boolean isActive = true;
}