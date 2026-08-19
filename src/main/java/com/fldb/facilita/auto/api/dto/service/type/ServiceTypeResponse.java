package com.fldb.facilita.auto.api.dto.service.type;

import com.fldb.facilita.auto.domain.entity.ServiceType;
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
public class ServiceTypeResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private OffsetDateTime createdAt;

    public static ServiceTypeResponse fromEntity(ServiceType insuranceCompany) {
        return ServiceTypeResponse.builder()
                .id(insuranceCompany.getId())
                .tenantId(insuranceCompany.getTenantId())
                .name(insuranceCompany.getName())
                .description(insuranceCompany.getDescription())
                .createdAt(insuranceCompany.getCreatedAt())
                .build();
    }
}
