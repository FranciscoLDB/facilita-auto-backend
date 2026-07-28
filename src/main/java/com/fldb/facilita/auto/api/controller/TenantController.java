package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.dto.ApiResponseData;
import com.fldb.facilita.auto.api.dto.tenant.CreateTenantRequest;
import com.fldb.facilita.auto.api.dto.tenant.TenantResponse;
import com.fldb.facilita.auto.domain.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<ApiResponseData<TenantResponse>> create(@Valid @RequestBody CreateTenantRequest request) {
        log.info("Creating tenant: {}", request.getName());
        TenantResponse response = tenantService.create(request);

        ApiResponseData<TenantResponse> apiResponse = ApiResponseData.<TenantResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Tenant cadastrado com sucesso.")
                .data(response)
                .build();

        log.info("New tenant created: {}:{}", apiResponse.getData().getName(), apiResponse.getData().getCnpj());
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseData<TenantResponse>> findById(@PathVariable UUID id) {
        TenantResponse response = tenantService.findById(id);

        ApiResponseData<TenantResponse> apiResponse = ApiResponseData.<TenantResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Tenant localizado com sucesso.")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponseData<Page<TenantResponse>>> findAll(Pageable pageable) {
        Page<TenantResponse> response = tenantService.findAll(pageable);

        ApiResponseData<Page<TenantResponse>> apiResponse = ApiResponseData.<Page<TenantResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Lista de tenants recuperada com sucesso.")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
