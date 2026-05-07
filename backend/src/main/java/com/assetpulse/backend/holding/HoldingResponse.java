package com.assetpulse.backend.holding;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class HoldingResponse {
    private Long id;
    private String symbol;
    private String name;
    private BigDecimal quantity;
    private BigDecimal averageCost;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal gainLoss;
    private BigDecimal gainLossPercent;
    private Instant createdAt;
    private Instant updatedAt;
}