package com.assetpulse.backend.market;

import com.futu.openapi.FTAPI_Conn;
import com.futu.openapi.FTSPI_Qot;
import com.futu.openapi.pb.QotGetSecuritySnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class FutuQuoteHandler implements FTSPI_Qot {

    private final Map<Integer, CompletableFuture<Double>> pendingRequests
            = new ConcurrentHashMap<>();

    public void registerPendingRequest(int serialNo, CompletableFuture<Double> future) {
        pendingRequests.put(serialNo, future);
    }

    @Override
    public void onReply_GetSecuritySnapshot(FTAPI_Conn client, int nSerialNo,
                                            QotGetSecuritySnapshot.Response rsp) {
        // Find the waiting future using the serialNo ticket
        CompletableFuture<Double> future = pendingRequests.remove(nSerialNo);
        if (future == null) return;

        if (rsp.getRetType() == 0 && rsp.getS2C().getSnapshotListCount() > 0) {
            // Extract the price from the response
            double price = rsp.getS2C().getSnapshotList(0).getBasic().getCurPrice();
            String code = rsp.getS2C().getSnapshotList(0).getBasic().getSecurity().getCode();
            log.info("📈 {} current price: {}", code, price);
            // Complete the future — this unblocks MarketDataService.getPrice()
            future.complete(price);
        } else {
            log.error("❌ Snapshot failed: {}", rsp.getRetMsg());
            future.completeExceptionally(new RuntimeException(rsp.getRetMsg()));
        }
    }
}