package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.dto.ApiResponseData;
import com.fldb.facilita.auto.api.dto.pricing.table.CreatePricingTableRequest;
import com.fldb.facilita.auto.api.dto.pricing.table.PricingTableResponse;
import com.fldb.facilita.auto.api.dto.pricing.table.UpdatePricingTableRequest;
import com.fldb.facilita.auto.domain.service.PricingTableService;
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
@RequestMapping("/api/v1/pricing-tables")
@RequiredArgsConstructor
public class PricingTableController {

    private final PricingTableService pricingTableService;

    @PostMapping
    public ResponseEntity<ApiResponseData<PricingTableResponse>> create(@Valid @RequestBody CreatePricingTableRequest request) {
        log.info("Creating pricing table");

        PricingTableResponse response = pricingTableService.create(request);
        log.info("Pricing table created successfully for tenant: {}", response.getTenantId());

        ApiResponseData<PricingTableResponse> apiResponse = ApiResponseData.<PricingTableResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Pricing table created successfully.")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponseData<Page<PricingTableResponse>>> findAll(Pageable pageable) {
        Page<PricingTableResponse> pricingTables = pricingTableService.findAll(pageable);

        ApiResponseData<Page<PricingTableResponse>> response = ApiResponseData.<Page<PricingTableResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Pricing tables retrieved successfully.")
                .data(pricingTables)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseData<Void>> delete(@PathVariable UUID id) {
        pricingTableService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseData<PricingTableResponse>> updatePatch(@PathVariable UUID id, @Valid @RequestBody UpdatePricingTableRequest request) {
        PricingTableResponse response = pricingTableService.updatePatch(id, request);

        ApiResponseData<PricingTableResponse> apiResponse = ApiResponseData.<PricingTableResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Pricing table updated successfully.")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}