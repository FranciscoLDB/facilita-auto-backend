package com.fldb.facilita.auto.domain.entity;

import com.fldb.facilita.auto.domain.model.Address;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "yards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Yard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "yard_street", nullable = false, length = 150)),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "yard_neighborhood", nullable = false, length = 100)),
            @AttributeOverride(name = "city", column = @Column(name = "yard_city", nullable = false, length = 100)),
            @AttributeOverride(name = "state", column = @Column(name = "yard_state", nullable = false, length = 50)),
            @AttributeOverride(name = "complement", column = @Column(name = "yard_complement", length = 100))
    })
    private Address address;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
