package com.assetpulse.backend.holding;

import com.assetpulse.backend.common.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
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

        Holding holding = Holding.builder()
                .user(user)
                .symbol(request.getSymbol().toUpperCase())
                .name(request.getName())
                .quantity(request.getQuantity())
                .averageCost(request.getAverageCost())
                .currentPrice(request.getCurrentPrice())
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
                holding.getCreatedAt(),
                holding.getUpdatedAt()
        );
    }
}