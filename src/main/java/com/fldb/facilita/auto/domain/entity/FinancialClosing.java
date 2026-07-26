package com.fldb.facilita.auto.domain.entity;

import com.fldb.facilita.auto.domain.enums.FinancialClosingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "financial_closings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialClosing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_company_id", nullable = false)
    private InsuranceCompany insuranceCompany;

    @Column(name = "batch_code", nullable = false, length = 50)
    private String batchCode;

    @Builder.Default
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private FinancialClosingStatus status = FinancialClosingStatus.OPEN;

    @Builder.Default
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "financial_closing_items",
            joinColumns = @JoinColumn(name = "financial_closing_id"),
            inverseJoinColumns = @JoinColumn(name = "service_order_item_id")
    )
    private List<ServiceOrderItem> items = new ArrayList<>();
}
