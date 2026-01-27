package com.yunbok.houseping.infrastructure.config;

import com.yunbok.houseping.adapter.out.api.ApplyhomeApiAdapter;
import com.yunbok.houseping.adapter.out.api.LhApiAdapter;
import com.yunbok.houseping.adapter.out.persistence.ApplyhomeDbAdapter;
import com.yunbok.houseping.adapter.out.persistence.LhDbAdapter;
import com.yunbok.houseping.adapter.out.web.LhWebAdapter;
import com.yunbok.houseping.domain.port.out.SubscriptionProvider;
import com.yunbok.houseping.domain.service.FallbackProviderChain;
import com.yunbok.houseping.domain.service.SubscriptionProviderChain;
import com.yunbok.houseping.infrastructure.annotation.ApplyhomeSource;
import com.yunbok.houseping.infrastructure.annotation.LhSource;
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
            @Autowired(required = false) LhApiAdapter lhApiAdapter,
            @Autowired(required = false) LhWebAdapter lhWebAdapter,
            @Autowired(required = false) LhDbAdapter lhDbAdapter) {
        List<SubscriptionProvider> providers = new ArrayList<>();
        Optional.ofNullable(lhWebAdapter).ifPresent(providers::add);
        Optional.ofNullable(lhApiAdapter).ifPresent(providers::add);
        Optional.ofNullable(lhDbAdapter).ifPresent(providers::add);
        return providers;
    }

    @Bean
    @ApplyhomeSource
    public List<SubscriptionProvider> applyhomeProviders(
            @Autowired(required = false) ApplyhomeApiAdapter applyhomeApiAdapter,
            @Autowired(required = false) ApplyhomeDbAdapter applyhomeDbAdapter) {
        List<SubscriptionProvider> providers = new ArrayList<>();
        Optional.ofNullable(applyhomeApiAdapter).ifPresent(providers::add);
        Optional.ofNullable(applyhomeDbAdapter).ifPresent(providers::add);
        return providers;
    }

    @Bean
    @ConditionalOnProperty(name = "feature.subscription.lh-api-enabled", havingValue = "true")
    public SubscriptionProviderChain lhChain(@LhSource List<SubscriptionProvider> providers) {
        return new FallbackProviderChain(providers, "LH");
    }

    @Bean
    @ConditionalOnProperty(name = "feature.subscription.applyhome-api-enabled", havingValue = "true")
    public SubscriptionProviderChain applyhomeChain(@ApplyhomeSource List<SubscriptionProvider> providers) {
        return new FallbackProviderChain(providers, "ApplyHome");
    }
}
