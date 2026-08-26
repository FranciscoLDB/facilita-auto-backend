package com.fldb.facilita.auto.domain.service;

import com.fldb.facilita.auto.api.dto.yard.CreateYardRequest;
import com.fldb.facilita.auto.api.dto.yard.YardMapper;
import com.fldb.facilita.auto.api.dto.yard.YardResponse;
import com.fldb.facilita.auto.api.dto.yard.UpdateYardRequest;
import com.fldb.facilita.auto.api.exception.ResourceNotFoundException;
import com.fldb.facilita.auto.domain.entity.company.User;
import com.fldb.facilita.auto.domain.entity.company.Yard;
import com.fldb.facilita.auto.domain.model.Address;
import com.fldb.facilita.auto.domain.repository.UserRepository;
import com.fldb.facilita.auto.domain.repository.YardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class YardService {

    private final YardRepository yardRepository;
    private final UserRepository userRepository;

    public YardResponse create(CreateYardRequest request) {
        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        }

        Address address = Address.builder()
                .street(request.getAddress().getStreet())
                .neighborhood(request.getAddress().getNeighborhood())
                .city(request.getAddress().getCity())
                .state(request.getAddress().getState())
                .complement(request.getAddress().getComplement())
                .build();

        Yard entity = Yard.builder()
                .name(request.getName())
                .address(address)
                .manager(manager)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        entity = yardRepository.save(entity);

        return YardResponse.fromEntity(entity);
    }

    public Page<YardResponse> findAll(Pageable pageable) {
        return yardRepository.findAll(pageable).map(YardResponse::fromEntity);
    }

    public void delete(UUID id) {
        if (!yardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Yard not found");
        }
        yardRepository.deleteById(id);
    }

    @Transactional
    public YardResponse updatePatch(UUID id, UpdateYardRequest request) {
        Yard entity = yardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yard not found"));

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            entity.setManager(manager);
        }

        if (request.getAddress() != null) {
            if (entity.getAddress() == null) {
                entity.setAddress(new Address());
            }
            YardMapper.INSTANCE.updateAddressFromDto(request.getAddress(), entity.getAddress());
        }

        YardMapper.INSTANCE.updateEntityFromDto(request, entity);

        return YardResponse.fromEntity(entity);
    }
}