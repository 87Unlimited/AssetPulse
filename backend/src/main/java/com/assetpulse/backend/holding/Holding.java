package com.assetpulse.backend.holding;

import com.assetpulse.backend.common.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "holdings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holding {
    @Column(nullable = false)
    private Integer market;  // 1=HK, 11=US, 21=A-share

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String symbol;          // e.g. "AAPL", "BTC", "VOO"

    @Column(nullable = false)
    private String name;            // e.g. "Apple Inc."

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;    // supports crypto decimals e.g. 0.00045 BTC

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal averageCost; // average price paid per unit

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentPrice; // manually set for now

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Calculated fields — not stored in DB, computed on the fly
    @Transient
    public BigDecimal getMarketValue() {
        return quantity.multiply(currentPrice);
    }

    @Transient
    public BigDecimal getGainLoss() {
        return getMarketValue().subtract(quantity.multiply(averageCost));
    }

    @Transient
    public BigDecimal getGainLossPercent() {
        BigDecimal costBasis = quantity.multiply(averageCost);
        if (costBasis.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getGainLoss().divide(costBasis, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}