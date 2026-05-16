package com.assetpulse.backend.holding;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class HoldingRequest {
    private String symbol;
    private String name;
    private BigDecimal quantity;
    private BigDecimal averageCost;
    private BigDecimal currentPrice;
    private Integer market;
}