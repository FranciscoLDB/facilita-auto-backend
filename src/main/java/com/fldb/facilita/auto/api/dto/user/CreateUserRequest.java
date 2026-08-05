package com.fldb.facilita.auto.api.dto.user;

import com.fldb.facilita.auto.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres.")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Formato de e-mail inválido.")
    @Size(max = 100, message = "O e-mail não pode ter mais de 100 caracteres.")
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    private String password;

    @NotNull(message = "O perfil do usuário (role) é obrigatório.")
    private UserRole role;
}
