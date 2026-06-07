package com.assetpulse.backend;

import com.assetpulse.backend.market.FutuTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(FutuTestConfig.class)
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }
}