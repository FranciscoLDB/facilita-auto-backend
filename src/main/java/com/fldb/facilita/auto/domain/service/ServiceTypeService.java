package com.fldb.facilita.auto.domain.service;

import com.fldb.facilita.auto.api.dto.service.type.CreateServiceTypeRequest;
import com.fldb.facilita.auto.api.dto.service.type.ServiceTypeMapper;
import com.fldb.facilita.auto.api.dto.service.type.ServiceTypeResponse;
import com.fldb.facilita.auto.api.dto.service.type.UpdateServiceTypeRequest;
import com.fldb.facilita.auto.api.exception.ResourceNotFoundException;
import com.fldb.facilita.auto.domain.entity.ServiceType;
import com.fldb.facilita.auto.domain.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceTypeResponse create(CreateServiceTypeRequest request) {
        ServiceType entity = ServiceType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        entity = serviceTypeRepository.save(entity);

        return ServiceTypeResponse.fromEntity(entity);
    }

    public Page<ServiceTypeResponse> findAll(Pageable pageable) {
        return serviceTypeRepository.findAll(pageable).map(ServiceTypeResponse::fromEntity);
    }

    public void delete(UUID id) {
        if (!serviceTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service type not found");
        }
        serviceTypeRepository.deleteById(id);
    }

    @Transactional
    public ServiceTypeResponse updatePatch(UUID id, UpdateServiceTypeRequest request) {
        ServiceType entity = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service type not found"));

        ServiceTypeMapper.INSTANCE.updateEntityFromDto(request, entity);

        return ServiceTypeResponse.fromEntity(entity);
    }
}
