package com.fldb.facilita.auto.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "pricing_tables",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pricing_insurance_service",
                        columnNames = {"tenant_id", "insurance_company_id", "service_type_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_company_id", nullable = false)
    private InsuranceCompany insuranceCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @Column(name = "base_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFee;

    @Column(name = "extra_km_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraKmFee;

    @Column(name = "dirt_road_km_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal dirtRoadKmFee;

    @Column(name = "included_km_allowance", nullable = false)
    private Integer includedKmAllowance;

    @Column(name = "idle_hour_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal idleHourFee;

    @Column(name = "worked_hour_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal workedHourFee;

    @Column(name = "skate_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal skateFee;

    @Column(name = "night_shift_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal nightShiftFee;

    @Builder.Default
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
