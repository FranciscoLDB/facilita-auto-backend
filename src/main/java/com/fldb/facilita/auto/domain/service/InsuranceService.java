package com.fldb.facilita.auto.domain.service;

import com.fldb.facilita.auto.api.dto.insurance.CreateInsuranceRequest;
import com.fldb.facilita.auto.api.dto.insurance.InsuranceMapper;
import com.fldb.facilita.auto.api.dto.insurance.InsuranceResponse;
import com.fldb.facilita.auto.api.dto.insurance.UpdateInsuranceRequest;
import com.fldb.facilita.auto.api.exception.BusinessException;
import com.fldb.facilita.auto.api.exception.ResourceNotFoundException;
import com.fldb.facilita.auto.domain.entity.InsuranceCompany;
import com.fldb.facilita.auto.domain.repository.InsuranceRepository;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final InsuranceRepository insuranceRepository;

    public InsuranceResponse create(CreateInsuranceRequest request) {
        InsuranceCompany insuranceCompany = InsuranceCompany.builder()
                .name(request.getName())
                .baseCode(request.getBaseCode())
                .operationalSystemUrl(request.getOperationalSystemUrl())
                .closingSystemUrl(request.getClosingSystemUrl())
                .systemUsername(request.getSystemUsername())
                .systemPassword(request.getSystemPassword())
                .contactPhones(request.getContactPhones())
                .notes(request.getNotes())
        .build();

        insuranceCompany = insuranceRepository.save(insuranceCompany);

        return InsuranceResponse.fromEntity(insuranceCompany);
    }

    public Page<InsuranceResponse> findAll(Pageable pageable) {
        return insuranceRepository.findAll(pageable).map(InsuranceResponse::fromEntity);
    }

    public void delete(UUID id) {
        if (!insuranceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Insurance company not found");
        }
        insuranceRepository.deleteById(id);
    }

    @Transactional
    public InsuranceResponse updatePatch(UUID id, UpdateInsuranceRequest request) {
        InsuranceCompany insuranceCompany = insuranceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found"));

        InsuranceMapper.INSTANCE.updateEntityFromDto(request, insuranceCompany);

        return InsuranceResponse.fromEntity(insuranceCompany);
    }
}
