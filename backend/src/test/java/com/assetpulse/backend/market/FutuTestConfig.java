package com.assetpulse.backend.market;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import static org.mockito.Mockito.mock;

@TestConfiguration
public class FutuTestConfig {

    @Bean
    @Primary
    public FutuClientService futuClientService() {
        return mock(FutuClientService.class);
    }

    @Bean
    @Primary
    public MarketDataService marketDataService() {
        return mock(MarketDataService.class);
    }

    @Bean
    @Primary
    public PriceRefreshService priceRefreshService() {
        return mock(PriceRefreshService.class);
    }
}