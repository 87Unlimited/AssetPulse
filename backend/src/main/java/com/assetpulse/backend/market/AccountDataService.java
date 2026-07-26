package com.assetpulse.backend.market;

import com.futu.openapi.pb.TrdCommon;
import com.futu.openapi.pb.TrdGetAccList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountDataService {
    private final FutuTradeClientService futuTradeClientService;
    private final FutuTradeHandler futuTradeHandler;

    public List<TrdCommon.TrdAcc> getAccountList() throws Exception {
        TrdGetAccList.C2S c2s = TrdGetAccList.C2S.newBuilder()
                .setUserID(0)
                .build();

        TrdGetAccList.Request request = TrdGetAccList.Request.newBuilder()
                .setC2S(c2s)
                .build();

        CompletableFuture<List<TrdCommon.TrdAcc>> future = new CompletableFuture<>();
        int serialNo = futuTradeClientService.getTrdClient().getAccList(request);
        futuTradeHandler.registerAccListRequest(serialNo, future);

        log.info("Requested serialNo: {}",  serialNo);

        // Wait max 5 seconds for response
        return future.get(5, TimeUnit.SECONDS);
    }
}
