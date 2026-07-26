package com.fldb.facilita.auto.domain.entity;

import com.fldb.facilita.auto.domain.enums.ItemType;
import com.fldb.facilita.auto.domain.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrder serviceOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private ItemType itemType;

    @Column(length = 100)
    private String description;

    @Column(name = "authorization_code", length = 50)
    private String authorizationCode;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.valueOf(1.00);

    @Builder.Default
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_manual_override", nullable = false)
    private Boolean isManualOverride = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private OrderItemStatus status = OrderItemStatus.TO_CHARGE;

    @Builder.Default
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}