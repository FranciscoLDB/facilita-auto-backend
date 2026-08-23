package com.fldb.facilita.auto.api.dto.pricing.table;

import com.fldb.facilita.auto.domain.entity.PricingTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PricingTableResponse {

    private UUID id;
    private UUID tenantId;
    private UUID insuranceCompanyId;
    private String insuranceCompanyName;
    private UUID serviceTypeId;
    private String serviceTypeName;
    private BigDecimal baseFee;
    private BigDecimal extraKmFee;
    private BigDecimal dirtRoadKmFee;
    private Integer includedKmAllowance;
    private BigDecimal idleHourFee;
    private BigDecimal workedHourFee;
    private BigDecimal skateFee;
    private BigDecimal nightShiftFee;
    private OffsetDateTime createdAt;

    public static PricingTableResponse fromEntity(PricingTable pricingTable) {
        return PricingTableResponse.builder()
                .id(pricingTable.getId())
                .tenantId(pricingTable.getTenantId())
                .insuranceCompanyId(pricingTable.getInsuranceCompany() != null ? pricingTable.getInsuranceCompany().getId() : null)
                .insuranceCompanyName(pricingTable.getInsuranceCompany() != null ? pricingTable.getInsuranceCompany().getName() : null)
                .serviceTypeId(pricingTable.getServiceType() != null ? pricingTable.getServiceType().getId() : null)
                .serviceTypeName(pricingTable.getServiceType() != null ? pricingTable.getServiceType().getName() : null)
                .baseFee(pricingTable.getBaseFee())
                .extraKmFee(pricingTable.getExtraKmFee())
                .dirtRoadKmFee(pricingTable.getDirtRoadKmFee())
                .includedKmAllowance(pricingTable.getIncludedKmAllowance())
                .idleHourFee(pricingTable.getIdleHourFee())
                .workedHourFee(pricingTable.getWorkedHourFee())
                .skateFee(pricingTable.getSkateFee())
                .nightShiftFee(pricingTable.getNightShiftFee())
                .createdAt(pricingTable.getCreatedAt())
                .build();
    }
}