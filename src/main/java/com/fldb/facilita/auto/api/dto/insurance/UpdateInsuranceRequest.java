package com.fldb.facilita.auto.api.dto.insurance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInsuranceRequest {

    @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres.")
    private String name;

    @Size(max = 50, message = "O código base não pode ter mais de 50 caracteres.")
    private String baseCode;

    @Size(max = 255, message = "A URL do sistema operacional não pode ter mais de 255 caracteres.")
    private String operationalSystemUrl;

    @Size(max = 255, message = "A URL do sistema de fechamento não pode ter mais de 255 caracteres.")
    private String closingSystemUrl;

    @Size(max = 100, message = "O usuário do sistema não pode ter mais de 100 caracteres.")
    private String systemUsername;

    @Size(max = 100, message = "A senha do sistema não pode ter mais de 100 caracteres.")
    private String systemPassword;

    private String contactPhones;

    private String notes;
}