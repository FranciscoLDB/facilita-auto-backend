package com.fldb.facilita.auto.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotBlank(message = "A rua é obrigatória.")
    @Size(max = 150, message = "A rua não pode ter mais de 150 caracteres.")
    private String street;

    @NotBlank(message = "O bairro é obrigatório.")
    @Size(max = 100, message = "O bairro não pode ter mais de 100 caracteres.")
    private String neighborhood;

    @NotBlank(message = "A cidade é obrigatória.")
    @Size(max = 100, message = "A cidade não pode ter mais de 100 caracteres.")
    private String city;

    @NotBlank(message = "O estado é obrigatório.")
    @Size(max = 50, message = "O estado não pode ter mais de 50 caracteres.")
    private String state;

    @Size(max = 100, message = "O complemento não pode ter mais de 100 caracteres.")
    private String complement;
}