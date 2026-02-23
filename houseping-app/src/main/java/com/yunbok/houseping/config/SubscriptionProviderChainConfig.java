package com.yunbok.houseping.config;

import com.yunbok.houseping.infrastructure.api.ApplyhomeApiClient;
import com.yunbok.houseping.infrastructure.api.LhApiClient;
import com.yunbok.houseping.infrastructure.persistence.ApplyhomeDbStore;
import com.yunbok.houseping.infrastructure.persistence.LhDbStore;
import com.yunbok.houseping.infrastructure.api.LhWebScraperClient;
import com.yunbok.houseping.core.domain.SubscriptionSource;
import com.yunbok.houseping.core.port.SubscriptionProvider;
import com.yunbok.houseping.core.service.subscription.FallbackProviderChain;
import com.yunbok.houseping.core.service.subscription.SubscriptionProviderChain;
import com.yunbok.houseping.support.annotation.ApplyhomeSource;
import com.yunbok.houseping.support.annotation.LhSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 소스별 Fallback Provider Chain 등록 설정
 */
@Configuration
public class SubscriptionProviderChainConfig {

    @Bean
    @LhSource
    public List<SubscriptionProvider> lhProviders(
            @Autowired(required = false) LhApiClient lhApiAdapter,
            @Autowired(required = false) LhWebScraperClient lhWebAdapter,
            @Autowired(required = false) LhDbStore lhDbAdapter) {
        List<SubscriptionProvider> providers = new ArrayList<>();
        Optional.ofNullable(lhWebAdapter).ifPresent(providers::add);
        Optional.ofNullable(lhApiAdapter).ifPresent(providers::add);
        Optional.ofNullable(lhDbAdapter).ifPresent(providers::add);
        return providers;
    }

    @Bean
    @ApplyhomeSource
    public List<SubscriptionProvider> applyhomeProviders(
            @Autowired(required = false) ApplyhomeApiClient applyhomeApiAdapter,
            @Autowired(required = false) ApplyhomeDbStore applyhomeDbAdapter) {
        List<SubscriptionProvider> providers = new ArrayList<>();
        Optional.ofNullable(applyhomeApiAdapter).ifPresent(providers::add);
        Optional.ofNullable(applyhomeDbAdapter).ifPresent(providers::add);
        return providers;
    }

    @Bean
    @ConditionalOnProperty(name = "feature.subscription.lh-api-enabled", havingValue = "true")
    public SubscriptionProviderChain lhChain(@LhSource List<SubscriptionProvider> providers) {
        return new FallbackProviderChain(providers, SubscriptionSource.LH.getValue());
    }

    @Bean
    @ConditionalOnProperty(name = "feature.subscription.applyhome-api-enabled", havingValue = "true")
    public SubscriptionProviderChain applyhomeChain(@ApplyhomeSource List<SubscriptionProvider> providers) {
        return new FallbackProviderChain(providers, SubscriptionSource.APPLYHOME.getValue());
    }
}
