package com.fldb.facilita.auto.util;

import com.fldb.facilita.auto.domain.entity.InsuranceCompany;
import com.fldb.facilita.auto.domain.entity.PricingTable;
import com.fldb.facilita.auto.domain.entity.ServiceType;
import com.fldb.facilita.auto.domain.entity.company.Tenant;
import com.fldb.facilita.auto.domain.repository.InsuranceRepository;
import com.fldb.facilita.auto.domain.repository.PricingTableRepository;
import com.fldb.facilita.auto.domain.repository.ServiceTypeRepository;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

import static com.fldb.facilita.auto.util.TenantTestUtils.runInTenantContext;

@Component
public class IntegrationTestHelper {

    private final TenantRepository tenantRepository;
    private final InsuranceRepository insuranceRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final PricingTableRepository pricingTableRepository;

    public IntegrationTestHelper(TenantRepository tenantRepository,
                                 InsuranceRepository insuranceRepository,
                                 ServiceTypeRepository serviceTypeRepository,
                                 PricingTableRepository pricingTableRepository) {
        this.tenantRepository = tenantRepository;
        this.insuranceRepository = insuranceRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.pricingTableRepository = pricingTableRepository;
    }

    public Tenant createTenant(String namePrefix) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);
        return tenantRepository.save(Tenant.builder()
                .companyName(namePrefix + " " + uniqueSuffix)
                .taxId("123" + Math.abs(uniqueSuffix.hashCode())) // Evita duplicidade de CNPJ/taxId
                .build());
    }

    public InsuranceCompany createInsuranceCompany(UUID tenantId, String name) {
        return runInTenantContext(tenantId, () -> insuranceRepository.save(
                InsuranceCompany.builder()
                        .name(name + " - " + UUID.randomUUID().toString().substring(0, 4))
                        .tenantId(tenantId)
                        .build()
        ));
    }

    public ServiceType createServiceType(UUID tenantId, String name) {
        return runInTenantContext(tenantId, () -> serviceTypeRepository.save(
                ServiceType.builder()
                        .name(name + " - " + UUID.randomUUID().toString().substring(0, 4))
                        .description("Descrição gerada dinamicamente")
                        .tenantId(tenantId)
                        .build()
        ));
    }

    public PricingTable createPricingTable(UUID tenantId, InsuranceCompany insurance, ServiceType serviceType) {
        return runInTenantContext(tenantId, () -> pricingTableRepository.save(
                PricingTable.builder()
                        .tenantId(tenantId)
                        .insuranceCompany(insurance)
                        .serviceType(serviceType)
                        .baseFee(new BigDecimal("150.00"))
                        .extraKmFee(new BigDecimal("3.50"))
                        .dirtRoadKmFee(new BigDecimal("5.00"))
                        .includedKmAllowance(30)
                        .idleHourFee(new BigDecimal("40.00"))
                        .workedHourFee(new BigDecimal("80.00"))
                        .skateFee(new BigDecimal("50.00"))
                        .nightShiftFee(new BigDecimal("30.00"))
                        .build()
        ));
    }
}
