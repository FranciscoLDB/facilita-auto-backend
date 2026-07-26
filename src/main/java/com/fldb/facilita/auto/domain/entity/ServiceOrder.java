package com.fldb.facilita.auto.domain.entity;

import com.fldb.facilita.auto.domain.enums.ServiceOrderStatus;
import com.fldb.facilita.auto.domain.model.Address;
import com.fldb.facilita.auto.domain.model.InsuredDetails;
import com.fldb.facilita.auto.domain.model.VehicleDetails;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "service_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOrder {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yard_id")
    private Yard yard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_service_order_id")
    private ServiceOrder parentServiceOrder;

    @Column(name = "claim_number", nullable = false, length = 50)
    private String claimNumber;

    @Column(name = "request_number")
    private Integer requestNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private ServiceOrderStatus status = ServiceOrderStatus.PENDING;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "insured_details", nullable = false, columnDefinition = "jsonb")
    private InsuredDetails insuredDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vehicle_details", nullable = false, columnDefinition = "jsonb")
    private VehicleDetails vehicleDetails;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "origin_street", nullable = false, length = 150)),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "origin_neighborhood", nullable = false, length = 100)),
            @AttributeOverride(name = "city", column = @Column(name = "origin_city", nullable = false, length = 100)),
            @AttributeOverride(name = "state", column = @Column(name = "origin_state", nullable = false, length = 50)),
            @AttributeOverride(name = "complement", column = @Column(name = "origin_complement", length = 100))
    })
    private Address originAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "destination_street", nullable = false, length = 150)),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "destination_neighborhood", nullable = false, length = 100)),
            @AttributeOverride(name = "city", column = @Column(name = "destination_city", nullable = false, length = 100)),
            @AttributeOverride(name = "state", column = @Column(name = "destination_state", nullable = false, length = 50)),
            @AttributeOverride(name = "complement", column = @Column(name = "destination_complement", length = 100))
    })
    private Address destinationAddress;

    @Builder.Default
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "signature_url", length = 500)
    private String signatureUrl;

    @Builder.Default
    @Column(name = "opened_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime openedAt = OffsetDateTime.now();

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOrderItem> items = new ArrayList<>();
}