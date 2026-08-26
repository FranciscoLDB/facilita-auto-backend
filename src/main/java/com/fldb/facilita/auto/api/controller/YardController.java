package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.dto.ApiResponseData;
import com.fldb.facilita.auto.api.dto.yard.CreateYardRequest;
import com.fldb.facilita.auto.api.dto.yard.YardResponse;
import com.fldb.facilita.auto.api.dto.yard.UpdateYardRequest;
import com.fldb.facilita.auto.domain.service.YardService;
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
@RequestMapping("/api/v1/yards")
@RequiredArgsConstructor
public class YardController {

    private final YardService yardService;

    @PostMapping
    public ResponseEntity<ApiResponseData<YardResponse>> create(@Valid @RequestBody CreateYardRequest request) {
        log.info("Creating yard");

        YardResponse response = yardService.create(request);
        log.info("Yard created successfully for tenant: {}", response.getTenantId());

        ApiResponseData<YardResponse> apiResponse = ApiResponseData.<YardResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Yard created successfully.")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponseData<Page<YardResponse>>> findAll(Pageable pageable) {
        Page<YardResponse> yards = yardService.findAll(pageable);

        ApiResponseData<Page<YardResponse>> response = ApiResponseData.<Page<YardResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Yards retrieved successfully.")
                .data(yards)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseData<Void>> delete(@PathVariable UUID id) {
        yardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseData<YardResponse>> updatePatch(@PathVariable UUID id, @Valid @RequestBody UpdateYardRequest request) {
        YardResponse response = yardService.updatePatch(id, request);

        ApiResponseData<YardResponse> apiResponse = ApiResponseData.<YardResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Yard updated successfully.")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}