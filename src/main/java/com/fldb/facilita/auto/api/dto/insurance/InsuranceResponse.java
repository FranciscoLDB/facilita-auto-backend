package com.fldb.facilita.auto.api.dto.insurance;

import com.fldb.facilita.auto.domain.entity.InsuranceCompany;
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
public class InsuranceResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String baseCode;
    private String operationalSystemUrl;
    private String closingSystemUrl;
    private String systemUsername;
    private String systemPassword;
    private String contactPhones;
    private String notes;
    private Boolean isActive;
    private OffsetDateTime createdAt;

    public static InsuranceResponse fromEntity(InsuranceCompany insuranceCompany) {
        return InsuranceResponse.builder()
                .id(insuranceCompany.getId())
                .tenantId(insuranceCompany.getTenantId())
                .name(insuranceCompany.getName())
                .baseCode(insuranceCompany.getBaseCode())
                .operationalSystemUrl(insuranceCompany.getOperationalSystemUrl())
                .closingSystemUrl(insuranceCompany.getClosingSystemUrl())
                .systemUsername(insuranceCompany.getSystemUsername())
                .systemPassword(insuranceCompany.getSystemPassword())
                .contactPhones(insuranceCompany.getContactPhones())
                .notes(insuranceCompany.getNotes())
                .isActive(insuranceCompany.getIsActive())
                .createdAt(insuranceCompany.getCreatedAt())
                .build();
    }
}
