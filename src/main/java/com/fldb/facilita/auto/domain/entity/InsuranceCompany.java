package com.fldb.facilita.auto.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "insurance_companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "base_code", length = 50)
    private String baseCode;

    @Column(name = "operational_system_url", length = 255)
    private String operationalSystemUrl;

    @Column(name = "closing_system_url", length = 255)
    private String closingSystemUrl;

    @Column(name = "system_username", length = 100)
    private String systemUsername;

    @Column(name = "system_password", length = 100)
    private String systemPassword;

    @Column(name = "contact_phones", columnDefinition = "TEXT")
    private String contactPhones;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}