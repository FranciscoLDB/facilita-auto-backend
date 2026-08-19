package com.fldb.facilita.auto.api.dto.service.type;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceTypeRequest {

    @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres.")
    private String name;

    @Size(max = 200, message = "A descrição não pode ter mais de 200 caracteres.")
    private String description;
}