package com.assetpulse.backend.holding;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
public class HoldingController {

    private final HoldingService holdingService;

    @GetMapping
    public ResponseEntity<List<HoldingResponse>> getAllHoldings() {
        return ResponseEntity.ok(holdingService.getAllHoldings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoldingResponse> getHolding(@PathVariable Long id) {
        return ResponseEntity.ok(holdingService.getHolding(id));
    }

    @PostMapping
    public ResponseEntity<HoldingResponse> createHolding(
            @RequestBody HoldingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(holdingService.createHolding(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HoldingResponse> updateHolding(
            @PathVariable Long id,
            @RequestBody HoldingRequest request) {
        return ResponseEntity.ok(holdingService.updateHolding(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHolding(@PathVariable Long id) {
        holdingService.deleteHolding(id);
        return ResponseEntity.noContent().build();
    }
}