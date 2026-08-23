package com.fldb.facilita.auto.api.dto.pricing.table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePricingTableRequest {

    @NotNull(message = "O ID da seguradora é obrigatório.")
    private UUID insuranceCompanyId;

    @NotNull(message = "O ID do tipo de serviço é obrigatório.")
    private UUID serviceTypeId;

    @NotNull(message = "A taxa base é obrigatória.")
    @PositiveOrZero(message = "A taxa base não pode ser negativa.")
    private BigDecimal baseFee;

    @NotNull(message = "A taxa de KM extra é obrigatória.")
    @PositiveOrZero(message = "A taxa de KM extra não pode ser negativa.")
    private BigDecimal extraKmFee;

    @NotNull(message = "A taxa de KM de terra é obrigatória.")
    @PositiveOrZero(message = "A taxa de KM de terra não pode ser negativa.")
    private BigDecimal dirtRoadKmFee;

    @NotNull(message = "A franquia de KM incluída é obrigatória.")
    @PositiveOrZero(message = "A franquia de KM não pode ser negativa.")
    private Integer includedKmAllowance;

    @NotNull(message = "A taxa de hora parada é obrigatória.")
    @PositiveOrZero(message = "A taxa de hora parada não pode ser negativa.")
    private BigDecimal idleHourFee;

    @NotNull(message = "A taxa de hora trabalhada é obrigatória.")
    @PositiveOrZero(message = "A taxa de hora trabalhada não pode ser negativa.")
    private BigDecimal workedHourFee;

    @PositiveOrZero(message = "A taxa de patins não pode ser negativa.")
    private BigDecimal skateFee;

    @PositiveOrZero(message = "A taxa de adicional noturno não pode ser negativa.")
    private BigDecimal nightShiftFee;
}