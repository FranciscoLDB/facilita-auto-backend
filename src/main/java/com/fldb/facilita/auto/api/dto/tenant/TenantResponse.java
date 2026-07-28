package com.fldb.facilita.auto.api.dto.tenant;

import com.fldb.facilita.auto.domain.entity.Tenant;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantResponse {

    private UUID id;
    private String name;
    private String cnpj;
    private Boolean active;
    private OffsetDateTime createdAt;

    public static TenantResponse fromEntity(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getCompanyName())
                .cnpj(tenant.getTaxId())
                .active(tenant.getIsActive())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}