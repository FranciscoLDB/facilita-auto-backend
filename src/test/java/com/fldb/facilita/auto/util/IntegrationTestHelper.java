package com.fldb.facilita.auto.util;

import com.fldb.facilita.auto.api.dto.AddressDto;
import com.fldb.facilita.auto.domain.entity.InsuranceCompany;
import com.fldb.facilita.auto.domain.entity.PricingTable;
import com.fldb.facilita.auto.domain.entity.ServiceType;
import com.fldb.facilita.auto.domain.entity.company.User;
import com.fldb.facilita.auto.domain.entity.company.Yard;
import com.fldb.facilita.auto.domain.entity.company.Tenant;
import com.fldb.facilita.auto.domain.model.Address;
import com.fldb.facilita.auto.domain.repository.InsuranceRepository;
import com.fldb.facilita.auto.domain.repository.PricingTableRepository;
import com.fldb.facilita.auto.domain.repository.ServiceTypeRepository;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import com.fldb.facilita.auto.domain.repository.YardRepository;
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
    private final YardRepository yardRepository;

    public IntegrationTestHelper(TenantRepository tenantRepository,
                                 InsuranceRepository insuranceRepository,
                                 ServiceTypeRepository serviceTypeRepository,
                                 PricingTableRepository pricingTableRepository,
                                 YardRepository yardRepository) {
        this.tenantRepository = tenantRepository;
        this.insuranceRepository = insuranceRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.pricingTableRepository = pricingTableRepository;
        this.yardRepository = yardRepository;
    }

    // ==========================================
    // TENANT
    // ==========================================
    public Tenant createTenant(String namePrefix) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);
        return tenantRepository.save(Tenant.builder()
                .companyName(namePrefix + " " + uniqueSuffix)
                .taxId("123" + Math.abs(uniqueSuffix.hashCode())) // Evita duplicidade de CNPJ/taxId
                .build());
    }

    // ==========================================
    // INSURANCE COMPANY
    // ==========================================
    public InsuranceCompany createInsuranceCompany(UUID tenantId, String name) {
        return runInTenantContext(tenantId, () -> insuranceRepository.save(
                InsuranceCompany.builder()
                        .name(name + " - " + UUID.randomUUID().toString().substring(0, 4))
                        .tenantId(tenantId)
                        .build()
        ));
    }

    // ==========================================
    // SERVICE TYPE
    // ==========================================
    public ServiceType createServiceType(UUID tenantId, String name) {
        return runInTenantContext(tenantId, () -> serviceTypeRepository.save(
                ServiceType.builder()
                        .name(name + " - " + UUID.randomUUID().toString().substring(0, 4))
                        .description("Descrição gerada dinamicamente")
                        .tenantId(tenantId)
                        .build()
        ));
    }

    // ==========================================
    // PRICING TABLE
    // ==========================================
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

    // ==========================================
    // YARD ENTITY HELPERS
    // ==========================================
    public Yard createYard(UUID tenantId, String name, AddressDto address, User manager, Boolean isActive) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 4);
        AddressDto validAddress = address != null ? address : createAddressDto();
        return runInTenantContext(tenantId, () -> yardRepository.save(
                Yard.builder()
                        .name(name + " - " + uniqueSuffix)
                        .address(Address.builder()
                                .street(validAddress.getStreet())
                                .neighborhood(validAddress.getNeighborhood())
                                .city(validAddress.getCity())
                                .state(validAddress.getState())
                                .complement(validAddress.getComplement())
                                .build())
                        .manager(manager)
                        .isActive(isActive != null ? isActive : true)
                        .tenantId(tenantId)
                        .build()
        ));
    }

    // ==========================================
    // ADDRESS DTO HELPERS
    // ==========================================
    public AddressDto createAddressDto() {
        return createAddressDto("Rua das Flores", "Centro", "Curitiba", "PR", "Galpão A");
    }

    public AddressDto createAddressDto(String street, String neighborhood, String city, String state) {
        return createAddressDto(street, neighborhood, city, state, null);
    }

    public AddressDto createAddressDto(String street, String neighborhood, String city, String state, String complement) {
        return AddressDto.builder()
                .street(street)
                .neighborhood(neighborhood)
                .city(city)
                .state(state)
                .complement(complement)
                .build();
    }
}