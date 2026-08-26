package com.fldb.facilita.auto.domain.entity.company;

import com.fldb.facilita.auto.domain.model.Address;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;

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

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
