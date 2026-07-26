package com.assetpulse.backend.market;

import com.futu.openapi.FTAPI_Conn;
import com.futu.openapi.FTSPI_Conn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FutuTradeConnectionHandler implements FTSPI_Conn {

    @Override
    public void onInitConnect(FTAPI_Conn client, long errCode, String desc) {
        if (errCode == 0) {
            log.info("✅ Connected to OpenD successfully — connID: {}", client.getConnectID());
        } else {
            log.error("❌ Failed to connect to OpenD — errCode: {}, desc: {}", errCode, desc);
        }
    }

    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        log.warn("⚠️ Disconnected from OpenD — errCode: {}", errCode);
    }
}