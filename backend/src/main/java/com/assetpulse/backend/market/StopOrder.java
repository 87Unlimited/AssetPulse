package com.assetpulse.backend.market;

import com.assetpulse.backend.common.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stop_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StopOrderType orderType;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal triggerPrice;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal qty;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant lastSyncedAt;
}
