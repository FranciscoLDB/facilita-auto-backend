package com.fldb.facilita.auto.api.dto.user;

import com.fldb.facilita.auto.domain.entity.company.User;
import com.fldb.facilita.auto.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String email;
    private UserRole role;
    private Boolean active;
    private OffsetDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
