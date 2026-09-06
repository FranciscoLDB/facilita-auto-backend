package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.dto.ApiResponseData;
import com.fldb.facilita.auto.api.dto.insurance.CreateInsuranceRequest;
import com.fldb.facilita.auto.api.dto.insurance.InsuranceResponse;
import com.fldb.facilita.auto.api.dto.insurance.UpdateInsuranceRequest;
import com.fldb.facilita.auto.domain.service.InsuranceService;
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
@RequestMapping("/api/v1/insurances")
@PreAuthorize("hasAnyAuthority('ADMIN', 'MASTER')")
@RequiredArgsConstructor
public class InsuranceController {

    private final InsuranceService insuranceService;

    @PostMapping
    public ResponseEntity<ApiResponseData<InsuranceResponse>> create(@Valid @RequestBody CreateInsuranceRequest request) {
        log.info("Creating insurance");

        InsuranceResponse response = insuranceService.create(request);
        log.info("Insurance created successfully for tenant: {}", response.getTenantId());

        ApiResponseData<InsuranceResponse> apiResponse = ApiResponseData.<InsuranceResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Insurance created successfully.")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponseData<Page<InsuranceResponse>>> findAll(Pageable pageable) {
        Page<InsuranceResponse> users = insuranceService.findAll(pageable);

        ApiResponseData<Page<InsuranceResponse>> response = ApiResponseData.<Page<InsuranceResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Insurance companies retrieved successfully.")
                .data(users)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseData<Void>> delete(@PathVariable UUID id) {
        insuranceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseData<InsuranceResponse>> updatePatch(@PathVariable UUID id, @Valid @RequestBody UpdateInsuranceRequest request) {
        InsuranceResponse response = insuranceService.updatePatch(id, request);

        ApiResponseData<InsuranceResponse> apiResponse = ApiResponseData.<InsuranceResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Insurance updated successfully.")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
