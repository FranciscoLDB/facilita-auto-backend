package com.fldb.facilita.auto.domain.service;

import com.fldb.facilita.auto.api.dto.tenant.CreateTenantRequest;
import com.fldb.facilita.auto.api.dto.tenant.TenantResponse;
import com.fldb.facilita.auto.api.exception.BusinessException;
import com.fldb.facilita.auto.api.exception.ErrorCode;
import com.fldb.facilita.auto.api.exception.ResourceNotFoundException;
import com.fldb.facilita.auto.domain.entity.Tenant;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional
    public TenantResponse create(CreateTenantRequest request) {
        if (request == null || request.getCnpj() == null) {
            throw new BusinessException("O CNPJ é obrigatório para cadastrar uma empresa.");
        }

        String cleanCnpj = request.getCnpj().replaceAll("\\D", "");

        if (tenantRepository.existsByTaxId(cleanCnpj)) {
            throw new BusinessException(ErrorCode.TENANT_ALREADY_EXISTS);
        }

        Tenant tenant = Tenant.builder()
                .companyName(request.getName())
                .taxId(cleanCnpj)
                .isActive(true)
                .build();

        Tenant saved = tenantRepository.save(tenant);
        return TenantResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TenantResponse findById(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TENANT_NOT_FOUND, "Empresa (Tenant) não encontrada para o ID informado."));
        return TenantResponse.fromEntity(tenant);
    }

    @Transactional(readOnly = true)
    public Page<TenantResponse> findAll(Pageable pageable) {
        return tenantRepository.findAll(pageable)
                .map(TenantResponse::fromEntity);
    }
}
