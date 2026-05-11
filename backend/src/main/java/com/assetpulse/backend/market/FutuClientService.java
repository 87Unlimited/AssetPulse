package com.assetpulse.backend.market;

import com.futu.openapi.FTAPI;
import com.futu.openapi.FTAPI_Conn_Qot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Slf4j
@Service
@RequiredArgsConstructor
public class FutuClientService {

    private final FutuConfig futuConfig;
    private final FutuConnectionHandler connectionHandler;
    private final FutuQuoteHandler quoteHandler;
    private FTAPI_Conn_Qot qotClient;

    @PostConstruct
    public void init() {
        FTAPI.init();
        qotClient = new FTAPI_Conn_Qot();
        qotClient.setClientInfo("AssetPulse", 1);
        qotClient.setConnSpi(connectionHandler);
        qotClient.setQotSpi(quoteHandler);
        qotClient.initConnect(futuConfig.getHost(), (short) futuConfig.getPort(), false);
        log.info("Connecting to OpenD at {}:{}", futuConfig.getHost(), futuConfig.getPort());
    }

    @PreDestroy
    public void destroy() {
        if (qotClient != null) {
            qotClient.close();
            log.info("Disconnected from OpenD");
        }
    }

    public FTAPI_Conn_Qot getQotClient() {
        return qotClient;
    }
}