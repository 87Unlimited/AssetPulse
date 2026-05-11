package com.assetpulse.backend.market;

import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotGetSecuritySnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final FutuClientService futuClientService;
    private final FutuQuoteHandler quoteHandler;

    public double getPrice(String symbol, int market) throws Exception {
        QotCommon.Security security = QotCommon.Security.newBuilder()
                .setMarket(market)
                .setCode(symbol)
                .build();

        QotGetSecuritySnapshot.C2S c2s = QotGetSecuritySnapshot.C2S.newBuilder()
                .addSecurityList(security)
                .build();

        QotGetSecuritySnapshot.Request request = QotGetSecuritySnapshot.Request.newBuilder()
                .setC2S(c2s)
                .build();

        CompletableFuture<Double> future = new CompletableFuture<>();
        int serialNo = futuClientService.getQotClient().getSecuritySnapshot(request);
        quoteHandler.registerPendingRequest(serialNo, future);

        log.info("Requested snapshot for {} serialNo: {}", symbol, serialNo);

        // Wait max 5 seconds for response
        return future.get(5, TimeUnit.SECONDS);
    }
}