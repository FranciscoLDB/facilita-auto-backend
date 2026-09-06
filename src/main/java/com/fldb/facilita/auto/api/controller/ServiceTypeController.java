package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.dto.ApiResponseData;
import com.fldb.facilita.auto.api.dto.service.type.CreateServiceTypeRequest;
import com.fldb.facilita.auto.api.dto.service.type.ServiceTypeResponse;
import com.fldb.facilita.auto.api.dto.service.type.UpdateServiceTypeRequest;
import com.fldb.facilita.auto.domain.service.ServiceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/service-types")
@PreAuthorize("hasAnyAuthority('ADMIN', 'MASTER')")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @PostMapping
    public ResponseEntity<ApiResponseData<ServiceTypeResponse>> create(@Valid @RequestBody CreateServiceTypeRequest request) {
        log.info("Creating service type");

        ServiceTypeResponse response = serviceTypeService.create(request);
        log.info("Service type created successfully for tenant: {}", response.getTenantId());

        ApiResponseData<ServiceTypeResponse> apiResponse = ApiResponseData.<ServiceTypeResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Service type created successfully.")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('OPERATOR', 'ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponseData<Page<ServiceTypeResponse>>> findAll(Pageable pageable) {
        Page<ServiceTypeResponse> users = serviceTypeService.findAll(pageable);

        ApiResponseData<Page<ServiceTypeResponse>> response = ApiResponseData.<Page<ServiceTypeResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Service types retrieved successfully.")
                .data(users)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseData<Void>> delete(@PathVariable UUID id) {
        serviceTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseData<ServiceTypeResponse>> updatePatch(@PathVariable UUID id, @Valid @RequestBody UpdateServiceTypeRequest request) {
        ServiceTypeResponse response = serviceTypeService.updatePatch(id, request);

        ApiResponseData<ServiceTypeResponse> apiResponse = ApiResponseData.<ServiceTypeResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Service type updated successfully.")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
