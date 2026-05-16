package com.assetpulse.backend.market;

import com.assetpulse.backend.holding.Holding;
import com.assetpulse.backend.holding.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceRefreshService {

    private final HoldingRepository holdingRepository;
    private final MarketDataService marketDataService;

    // Runs every 5 minutes
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void refreshAllPrices() {
        List<Holding> holdings = holdingRepository.findAll();

        if (holdings.isEmpty()) {
            log.info("No holdings to refresh");
            return;
        }

        log.info("🔄 Refreshing prices for {} holdings...", holdings.size());
        int successCount = 0;
        int failCount = 0;

        for (Holding holding : holdings) {
            try {
                double price = marketDataService.getPrice(
                        holding.getSymbol(),
                        holding.getMarket()
                );
                holding.setCurrentPrice(BigDecimal.valueOf(price));
                holdingRepository.save(holding);
                successCount++;
                log.info("✅ Updated {} → {}", holding.getSymbol(), price);
            } catch (Exception e) {
                failCount++;
                log.warn("⚠️ Failed to update price for {} — {}",
                        holding.getSymbol(), e.getMessage());
            }
        }

        log.info("🏁 Price refresh complete — {} updated, {} failed",
                successCount, failCount);
    }

    // Also run once immediately on startup after 10 seconds
    @Scheduled(initialDelay = 10_000, fixedDelay = Long.MAX_VALUE)
    public void refreshOnStartup() {
        log.info("🚀 Running initial price refresh on startup...");
        refreshAllPrices();
    }
}