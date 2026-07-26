package com.assetpulse.backend.market;

import com.futu.openapi.FTAPI_Conn;
import com.futu.openapi.FTSPI_Trd;
import com.futu.openapi.pb.TrdCommon;
import com.futu.openapi.pb.TrdGetAccList;
import com.futu.openapi.pb.TrdGetOrderList;
import com.futu.openapi.pb.TrdGetPositionList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class FutuTradeHandler implements FTSPI_Trd {
    private final Map<Integer, CompletableFuture<List<TrdCommon.TrdAcc>>> pendingAccListRequests
            = new ConcurrentHashMap<>();

    public void registerAccListRequest(int serialNo, CompletableFuture<List<TrdCommon.TrdAcc>> future) {
        pendingAccListRequests.put(serialNo, future);
    }

    @Override
    public void onReply_GetAccList (FTAPI_Conn client, int nSerialNo, TrdGetAccList.Response rsp) {
        // Find the waiting future using the serialNo ticket
        CompletableFuture<List<TrdCommon.TrdAcc>> future = pendingAccListRequests.remove(nSerialNo);
        if (future == null) return;

        if (rsp.getRetType() == 0 && rsp.getS2C().getAccListCount() > 0) {
            // Extract the AccList from the response
            List<TrdCommon.TrdAcc> accList = rsp.getS2C().getAccListList();
            future.complete(accList);
        } else {
            log.error("❌ Snapshot failed: {}", rsp.getRetMsg());
            future.completeExceptionally(new RuntimeException(rsp.getRetMsg()));
        }
    }

    @Override
    public void onReply_GetPositionList(FTAPI_Conn client, int nSerialNo, TrdGetPositionList.Response rsp) {
        log.info("response: {}", rsp);
    }

    @Override
    public void onReply_GetOrderList(FTAPI_Conn client, int nSerialNo, TrdGetOrderList.Response rsp) {
        log.info("response: {}", rsp);
    }
}