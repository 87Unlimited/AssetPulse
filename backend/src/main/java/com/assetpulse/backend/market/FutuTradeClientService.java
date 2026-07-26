package com.assetpulse.backend.market;

import com.futu.openapi.FTAPI;
import com.futu.openapi.FTAPI_Conn_Trd;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FutuTradeClientService {

    private final FutuConfig futuConfig;
    private final FutuTradeConnectionHandler tradeConnectionHandler;
    private final FutuTradeHandler tradeHandler;
    private final FutuSdkInitializer sdkInitializer;
    private FTAPI_Conn_Trd trdClient;

    @PostConstruct
    public void init() {
        trdClient = new FTAPI_Conn_Trd();
        trdClient.setClientInfo("AssetPulse", 1);
        trdClient.setConnSpi(tradeConnectionHandler);
        trdClient.setTrdSpi(tradeHandler);
        trdClient.initConnect(futuConfig.getHost(), (short) futuConfig.getPort(), false);
        log.info("Connecting to OpenD at {}:{}", futuConfig.getHost(), futuConfig.getPort());
    }

    @PreDestroy
    public void destroy() {
        if (trdClient != null) {
            trdClient.close();
            log.info("Disconnected from OpenD");
        }
    }

    public FTAPI_Conn_Trd getTrdClient() {
        return trdClient;
    }
}