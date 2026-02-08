package com.yunbok.houseping.adapter.out.api;

import com.yunbok.houseping.adapter.api.RealTransactionApiAdapter;
import com.yunbok.houseping.support.external.RealTransactionApiResponse;
import com.yunbok.houseping.entity.RealTransactionCacheEntity;
import com.yunbok.houseping.repository.RealTransactionCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("RealTransactionApiAdapter - 실거래가 API 어댑터")
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class RealTransactionApiAdapterTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private RealTransactionCacheRepository cacheRepository;

    private RealTransactionApiAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RealTransactionApiAdapter(webClient, cacheRepository);
        ReflectionTestUtils.setField(adapter, "apiKey", "test-api-key");
    }

    @Nested
    @DisplayName("fetchTransactions() - 실거래가 조회")
    class FetchTransactions {

        @Test
        @DisplayName("캐시가 있고 유효하면 캐시를 반환한다")
        void returnsCacheWhenValid() {
            // given
            String lawdCd = "11680";
            String dealYmd = "202501";
            List<RealTransactionCacheEntity> cachedData = List.of(
                    createCacheEntity(lawdCd, dealYmd, "테스트아파트", 100000L)
            );

            when(cacheRepository.findByLawdCdAndDealYmd(lawdCd, dealYmd)).thenReturn(cachedData);

            // when
            List<RealTransactionCacheEntity> result = adapter.fetchTransactions(lawdCd, dealYmd);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAptName()).isEqualTo("테스트아파트");
            verify(webClient, never()).get();
        }

        @Test
        @DisplayName("캐시가 없으면 API를 호출한다")
        void callsApiWhenNoCacheExists() {
            // given
            String lawdCd = "11680";
            String dealYmd = "202501";

            when(cacheRepository.findByLawdCdAndDealYmd(lawdCd, dealYmd)).thenReturn(Collections.emptyList());
            mockWebClientGetSuccess(createMockApiResponse());

            // when
            List<RealTransactionCacheEntity> result = adapter.fetchTransactions(lawdCd, dealYmd);

            // then
            verify(webClient).get();
        }

        @Test
        @DisplayName("API 키가 없으면 빈 리스트를 반환한다")
        void returnsEmptyWhenNoApiKey() {
            // given
            ReflectionTestUtils.setField(adapter, "apiKey", "");

            // when
            List<RealTransactionCacheEntity> result = adapter.fetchTransactions("11680", "202501");

            // then
            assertThat(result).isEmpty();
            verify(webClient, never()).get();
        }

        @Test
        @DisplayName("API 키가 null이면 빈 리스트를 반환한다")
        void returnsEmptyWhenApiKeyIsNull() {
            // given
            ReflectionTestUtils.setField(adapter, "apiKey", null);

            // when
            List<RealTransactionCacheEntity> result = adapter.fetchTransactions("11680", "202501");

            // then
            assertThat(result).isEmpty();
            verify(webClient, never()).get();
        }

        @Test
        @DisplayName("캐시가 만료되면 API를 호출한다")
        void callsApiWhenCacheExpired() {
            // given
            String lawdCd = "11680";
            String dealYmd = "202501";
            List<RealTransactionCacheEntity> expiredCache = List.of(
                    createCacheEntityWithTime(lawdCd, dealYmd, "테스트아파트", 100000L,
                            LocalDateTime.now().minusDays(2))
            );

            when(cacheRepository.findByLawdCdAndDealYmd(lawdCd, dealYmd)).thenReturn(expiredCache);
            mockWebClientGetSuccess(createMockApiResponse());

            // when
            adapter.fetchTransactions(lawdCd, dealYmd);

            // then
            verify(webClient).get();
        }

        @Test
        @DisplayName("API 호출 실패 시 빈 리스트를 반환한다")
        void returnsEmptyWhenApiCallFails() {
            // given
            String lawdCd = "11680";
            String dealYmd = "202501";

            when(cacheRepository.findByLawdCdAndDealYmd(lawdCd, dealYmd)).thenReturn(Collections.emptyList());
            when(webClient.get()).thenThrow(new RuntimeException("API Error"));

            // when
            List<RealTransactionCacheEntity> result = adapter.fetchTransactions(lawdCd, dealYmd);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("API 응답이 null이면 빈 리스트를 반환한다")
        void returnsEmptyWhenResponseIsNull() {
            // given
            String lawdCd = "11680";
            String dealYmd = "202501";

            when(cacheRepository.findByLawdCdAndDealYmd(lawdCd, dealYmd)).thenReturn(Collections.emptyList());
            mockWebClientGetSuccess(null);

            // when
            List<RealTransactionCacheEntity> result = adapter.fetchTransactions(lawdCd, dealYmd);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("fetchRecentTransactions() - 최근 N개월 실거래가 조회")
    class FetchRecentTransactions {

        @Test
        @DisplayName("최근 3개월 데이터를 조회한다")
        void fetchesLastThreeMonths() {
            // given
            String lawdCd = "11680";
            when(cacheRepository.findByLawdCdAndDealYmd(anyString(), anyString())).thenReturn(Collections.emptyList());
            when(cacheRepository.findRecentByLawdCd(lawdCd)).thenReturn(List.of(
                    createCacheEntity(lawdCd, "202501", "아파트1", 100000L),
                    createCacheEntity(lawdCd, "202412", "아파트2", 95000L)
            ));
            mockWebClientGetSuccess(createMockApiResponse());

            // when
            List<RealTransactionCacheEntity> result = adapter.fetchRecentTransactions(lawdCd, 3);

            // then
            assertThat(result).hasSize(2);
            verify(cacheRepository).findRecentByLawdCd(lawdCd);
        }
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientGetSuccess(RealTransactionApiResponse response) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RealTransactionApiResponse.class)).thenReturn(Mono.justOrEmpty(response));
    }

    private RealTransactionApiResponse createMockApiResponse() {
        return new RealTransactionApiResponse();
    }

    private RealTransactionCacheEntity createCacheEntity(String lawdCd, String dealYmd, String aptName, Long amount) {
        return createCacheEntityWithTime(lawdCd, dealYmd, aptName, amount, LocalDateTime.now());
    }

    private RealTransactionCacheEntity createCacheEntityWithTime(String lawdCd, String dealYmd, String aptName, Long amount, LocalDateTime cachedAt) {
        return RealTransactionCacheEntity.builder()
                .lawdCd(lawdCd)
                .dealYmd(dealYmd)
                .aptName(aptName)
                .dealAmount(amount)
                .excluUseAr(BigDecimal.valueOf(84.5))
                .floor(10)
                .cachedAt(cachedAt)
                .build();
    }
}
