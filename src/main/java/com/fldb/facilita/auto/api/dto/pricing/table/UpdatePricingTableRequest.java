package com.fldb.facilita.auto.api.dto.pricing.table;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePricingTableRequest {

    @PositiveOrZero(message = "A taxa base não pode ser negativa.")
    private BigDecimal baseFee;

    @PositiveOrZero(message = "A taxa de KM extra não pode ser negativa.")
    private BigDecimal extraKmFee;

    @PositiveOrZero(message = "A taxa de KM de terra não pode ser negativa.")
    private BigDecimal dirtRoadKmFee;

    @PositiveOrZero(message = "A franquia de KM não pode ser negativa.")
    private Integer includedKmAllowance;

    @PositiveOrZero(message = "A taxa de hora parada não pode ser negativa.")
    private BigDecimal idleHourFee;

    @PositiveOrZero(message = "A taxa de hora trabalhada não pode ser negativa.")
    private BigDecimal workedHourFee;

    @PositiveOrZero(message = "A taxa de patins não pode ser negativa.")
    private BigDecimal skateFee;

    @PositiveOrZero(message = "A taxa de adicional noturno não pode ser negativa.")
    private BigDecimal nightShiftFee;
}