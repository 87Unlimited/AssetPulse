package com.assetpulse.backend.market;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketDataService marketDataService;

    @GetMapping("/price/{symbol}")
    public ResponseEntity<Map<String, Object>> getPrice(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "11") int market) {
        try {
            double price = marketDataService.getPrice(symbol, market);
            return ResponseEntity.ok(Map.of(
                    "symbol", symbol.toUpperCase(),
                    "price", price,
                    "market", market
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get price: " + e.getMessage(),
                    "symbol", symbol
            ));
        }
    }
}