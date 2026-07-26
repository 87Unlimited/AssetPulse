package com.assetpulse.backend.holding;

import com.assetpulse.backend.common.model.User;
import com.assetpulse.backend.market.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final MarketDataService marketDataService;

    private User getCurrentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
    }

    public List<HoldingResponse> getAllHoldings() {
        User user = getCurrentUser();
        return holdingRepository.findByUserIdOrderBySymbolAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public HoldingResponse getHolding(Long id) {
        User user = getCurrentUser();
        Holding holding = holdingRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Holding not found"));
        return toResponse(holding);
    }

    @Transactional
    public HoldingResponse createHolding(HoldingRequest request) {
        User user = getCurrentUser();

        if (holdingRepository.existsBySymbolAndUserId(
                request.getSymbol().toUpperCase(), user.getId())) {
            throw new IllegalArgumentException(
                    "Holding with symbol " + request.getSymbol() + " already exists");
        }

        // Validate symbol exists in Futu before saving
        int market = request.getMarket() != null ? request.getMarket() : 11;
        if (!marketDataService.isValidSymbol(request.getSymbol().toUpperCase(), market)) {
            throw new IllegalArgumentException(
                    "Symbol " + request.getSymbol().toUpperCase() +
                            " not found in market " + market +
                            ". For HK stocks use full code e.g. 00700");
        }

        // Fetch real current price from Futu
        double livePrice;
        try {
            livePrice = marketDataService.getPrice(
                    request.getSymbol().toUpperCase(), market);
        } catch (Exception e) {
            livePrice = request.getCurrentPrice().doubleValue();
            log.warn("Could not fetch live price for {}, using manual price",
                    request.getSymbol());
        }

        Holding holding = Holding.builder()
                .user(user)
                .symbol(request.getSymbol().toUpperCase())
                .name(request.getName())
                .quantity(request.getQuantity())
                .averageCost(request.getAverageCost())
                .currentPrice(request.getCurrentPrice())
                .market(request.getMarket() != null ? request.getMarket() : 11) // default US
                .build();

        return toResponse(holdingRepository.save(holding));
    }

    @Transactional
    public HoldingResponse updateHolding(Long id, HoldingRequest request) {
        User user = getCurrentUser();
        Holding holding = holdingRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Holding not found"));

        holding.setQuantity(request.getQuantity());
        holding.setAverageCost(request.getAverageCost());
        holding.setCurrentPrice(request.getCurrentPrice());
        holding.setName(request.getName());

        return toResponse(holdingRepository.save(holding));
    }

    @Transactional
    public void deleteHolding(Long id) {
        User user = getCurrentUser();
        Holding holding = holdingRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Holding not found"));
        holdingRepository.delete(holding);
    }

    private HoldingResponse toResponse(Holding holding) {
        return new HoldingResponse(
                holding.getId(),
                holding.getSymbol(),
                holding.getName(),
                holding.getQuantity(),
                holding.getAverageCost(),
                holding.getCurrentPrice(),
                holding.getMarketValue(),
                holding.getGainLoss(),
                holding.getGainLossPercent(),
                holding.getMarket(),
                holding.getCreatedAt(),
                holding.getUpdatedAt()
        );
    }
}