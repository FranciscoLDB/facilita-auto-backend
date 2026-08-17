package com.fldb.facilita.auto.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.TenantId;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "insurance_companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
public class InsuranceCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}