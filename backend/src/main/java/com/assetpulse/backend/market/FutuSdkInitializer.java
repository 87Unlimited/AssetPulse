package com.assetpulse.backend.market;

import com.futu.openapi.FTAPI;
import com.futu.openapi.FTAPI_Conn_Qot;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FutuSdkInitializer {
    @PostConstruct
    public void init() {
        FTAPI.init();
        log.info("Futu SDK initialized");
    }
}
