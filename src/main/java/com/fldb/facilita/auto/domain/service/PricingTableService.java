package com.fldb.facilita.auto.domain.service;

import com.fldb.facilita.auto.api.dto.pricing.table.CreatePricingTableRequest;
import com.fldb.facilita.auto.api.dto.pricing.table.PricingTableMapper;
import com.fldb.facilita.auto.api.dto.pricing.table.PricingTableResponse;
import com.fldb.facilita.auto.api.dto.pricing.table.UpdatePricingTableRequest;
import com.fldb.facilita.auto.api.exception.ResourceNotFoundException;
import com.fldb.facilita.auto.domain.entity.InsuranceCompany;
import com.fldb.facilita.auto.domain.entity.PricingTable;
import com.fldb.facilita.auto.domain.entity.ServiceType;
import com.fldb.facilita.auto.domain.repository.InsuranceRepository;
import com.fldb.facilita.auto.domain.repository.PricingTableRepository;
import com.fldb.facilita.auto.domain.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingTableService {

    private final PricingTableRepository pricingTableRepository;
    private final InsuranceRepository insuranceRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    public PricingTableResponse create(CreatePricingTableRequest request) {
        InsuranceCompany insuranceCompany = insuranceRepository.findById(request.getInsuranceCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found"));

        ServiceType serviceType = serviceTypeRepository.findById(request.getServiceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Service type not found"));

        PricingTable entity = PricingTable.builder()
                .insuranceCompany(insuranceCompany)
                .serviceType(serviceType)
                .baseFee(request.getBaseFee())
                .extraKmFee(request.getExtraKmFee())
                .dirtRoadKmFee(request.getDirtRoadKmFee())
                .includedKmAllowance(request.getIncludedKmAllowance())
                .idleHourFee(request.getIdleHourFee())
                .workedHourFee(request.getWorkedHourFee())
                .skateFee(request.getSkateFee() != null ? request.getSkateFee() : BigDecimal.ZERO)
                .nightShiftFee(request.getNightShiftFee() != null ? request.getNightShiftFee() : BigDecimal.ZERO)
                .build();

        entity = pricingTableRepository.save(entity);

        return PricingTableResponse.fromEntity(entity);
    }

    public Page<PricingTableResponse> findAll(Pageable pageable) {
        return pricingTableRepository.findAll(pageable).map(PricingTableResponse::fromEntity);
    }

    public void delete(UUID id) {
        if (!pricingTableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pricing table not found");
        }
        pricingTableRepository.deleteById(id);
    }

    @Transactional
    public PricingTableResponse updatePatch(UUID id, UpdatePricingTableRequest request) {
        PricingTable entity = pricingTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing table not found"));

        PricingTableMapper.INSTANCE.updateEntityFromDto(request, entity);

        return PricingTableResponse.fromEntity(entity);
    }
}