package com.yunbok.houseping.config;

import com.yunbok.houseping.core.domain.SubscriptionConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public SubscriptionConfig subscriptionConfig(SubscriptionProperties properties) {
        return new SubscriptionConfig(properties.getTargetAreas());
    }
}
