package com.yunbok.houseping.core.service.subscription;

import com.yunbok.houseping.adapter.dto.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
import com.yunbok.houseping.core.port.SubscriptionProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("FallbackProviderChain - Fallback 체인")
@ExtendWith(MockitoExtension.class)
class FallbackProviderChainTest {

    @Mock
    private SubscriptionProvider primaryProvider;

    @Mock
    private SubscriptionProvider fallbackProvider;

    @Nested
    @DisplayName("execute() - 체인 실행")
    class Execute {

        @Test
        @DisplayName("첫 번째 Provider가 성공하면 그 결과를 반환한다")
        void returnsFirstSuccessfulResult() {
            // given
            List<SubscriptionInfo> expected = List.of(createSubscription("아파트1"));
            when(primaryProvider.fetch(anyString(), any())).thenReturn(expected);
            when(primaryProvider.getSourceName()).thenReturn("Primary");

            FallbackProviderChain chain = new FallbackProviderChain(
                    List.of(primaryProvider, fallbackProvider), "TestChain"
            );

            // when
            List<SubscriptionInfo> result = chain.execute("서울", LocalDate.now());

            // then
            assertThat(result).hasSize(1);
            verify(primaryProvider).fetch(anyString(), any());
            verify(fallbackProvider, never()).fetch(anyString(), any());
        }

        @Test
        @DisplayName("첫 번째 Provider가 실패하면 두 번째 Provider를 시도한다")
        void fallsBackToSecondProvider() {
            // given
            when(primaryProvider.fetch(anyString(), any())).thenThrow(new RuntimeException("실패"));
            when(primaryProvider.getSourceName()).thenReturn("Primary");

            List<SubscriptionInfo> expected = List.of(createSubscription("아파트1"));
            when(fallbackProvider.fetch(anyString(), any())).thenReturn(expected);
            when(fallbackProvider.getSourceName()).thenReturn("Fallback");

            FallbackProviderChain chain = new FallbackProviderChain(
                    List.of(primaryProvider, fallbackProvider), "TestChain"
            );

            // when
            List<SubscriptionInfo> result = chain.execute("서울", LocalDate.now());

            // then
            assertThat(result).hasSize(1);
            verify(primaryProvider).fetch(anyString(), any());
            verify(fallbackProvider).fetch(anyString(), any());
        }

        @Test
        @DisplayName("모든 Provider가 실패하면 빈 리스트를 반환한다")
        void returnsEmptyWhenAllFail() {
            // given
            when(primaryProvider.fetch(anyString(), any())).thenThrow(new RuntimeException("실패"));
            when(primaryProvider.getSourceName()).thenReturn("Primary");
            when(fallbackProvider.fetch(anyString(), any())).thenThrow(new RuntimeException("실패"));
            when(fallbackProvider.getSourceName()).thenReturn("Fallback");

            FallbackProviderChain chain = new FallbackProviderChain(
                    List.of(primaryProvider, fallbackProvider), "TestChain"
            );

            // when
            List<SubscriptionInfo> result = chain.execute("서울", LocalDate.now());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Provider가 null을 반환하면 다음 Provider를 시도한다")
        void skipsNullResult() {
            // given
            when(primaryProvider.fetch(anyString(), any())).thenReturn(null);
            when(primaryProvider.getSourceName()).thenReturn("Primary");

            List<SubscriptionInfo> expected = List.of(createSubscription("아파트1"));
            when(fallbackProvider.fetch(anyString(), any())).thenReturn(expected);
            when(fallbackProvider.getSourceName()).thenReturn("Fallback");

            FallbackProviderChain chain = new FallbackProviderChain(
                    List.of(primaryProvider, fallbackProvider), "TestChain"
            );

            // when
            List<SubscriptionInfo> result = chain.execute("서울", LocalDate.now());

            // then
            assertThat(result).hasSize(1);
            verify(fallbackProvider).fetch(anyString(), any());
        }

        @Test
        @DisplayName("빈 리스트도 성공으로 처리한다")
        void treatsEmptyListAsSuccess() {
            // given
            when(primaryProvider.fetch(anyString(), any())).thenReturn(List.of());
            when(primaryProvider.getSourceName()).thenReturn("Primary");

            FallbackProviderChain chain = new FallbackProviderChain(
                    List.of(primaryProvider, fallbackProvider), "TestChain"
            );

            // when
            List<SubscriptionInfo> result = chain.execute("서울", LocalDate.now());

            // then
            assertThat(result).isEmpty();
            verify(fallbackProvider, never()).fetch(anyString(), any());
        }

        @Test
        @DisplayName("Provider가 없으면 빈 리스트를 반환한다")
        void returnsEmptyWhenNoProviders() {
            // given
            FallbackProviderChain chain = new FallbackProviderChain(List.of(), "TestChain");

            // when
            List<SubscriptionInfo> result = chain.execute("서울", LocalDate.now());

            // then
            assertThat(result).isEmpty();
        }
    }

    private SubscriptionInfo createSubscription(String name) {
        return ApplyHomeSubscriptionInfo.builder()
                .houseName(name)
                .area("서울")
                .houseType("APT")
                .receiptStartDate(LocalDate.now())
                .build();
    }
}
