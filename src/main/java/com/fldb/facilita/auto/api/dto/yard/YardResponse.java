package com.fldb.facilita.auto.api.dto.yard;

import com.fldb.facilita.auto.api.dto.AddressDto;
import com.fldb.facilita.auto.domain.entity.company.Yard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YardResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private AddressDto address;
    private UUID managerId;
    private Boolean isActive;
    private OffsetDateTime createdAt;

    public static YardResponse fromEntity(Yard yard) {
        AddressDto addressDto = null;
        if (yard.getAddress() != null) {
            addressDto = AddressDto.builder()
                    .street(yard.getAddress().getStreet())
                    .neighborhood(yard.getAddress().getNeighborhood())
                    .city(yard.getAddress().getCity())
                    .state(yard.getAddress().getState())
                    .complement(yard.getAddress().getComplement())
                    .build();
        }

        return YardResponse.builder()
                .id(yard.getId())
                .tenantId(yard.getTenantId())
                .name(yard.getName())
                .address(addressDto)
                .managerId(yard.getManager() != null ? yard.getManager().getId() : null)
                .isActive(yard.getIsActive())
                .createdAt(yard.getCreatedAt())
                .build();
    }
}