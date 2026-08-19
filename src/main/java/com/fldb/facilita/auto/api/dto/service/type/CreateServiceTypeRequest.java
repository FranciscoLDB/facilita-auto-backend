package com.fldb.facilita.auto.api.dto.service.type;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceTypeRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 50, message = "O nome não pode ter mais de 50 caracteres.")
    private String name;

    @Size(max = 200, message = "A descrição não pode ter mais de 200 caracteres.")
    private String description;
}