package com.yunbok.houseping.adapter.out.api;

import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.infrastructure.config.SubscriptionProperties;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionPriceRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ApplyhomeApiAdapter - 청약Home API 어댑터")
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ApplyhomeApiAdapterTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private SubscriptionProperties properties;

    @Mock
    private SubscriptionProperties.ApiProperties apiProperties;

    @Mock
    private SubscriptionPriceRepository priceRepository;

    private ApplyhomeApiAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ApplyhomeApiAdapter(webClient, properties, priceRepository);
        ReflectionTestUtils.setField(adapter, "apiKey", "test-api-key");

        when(properties.getApi()).thenReturn(apiProperties);
        when(apiProperties.getPageSize()).thenReturn(100);
        when(apiProperties.getDefaultPage()).thenReturn(1);
    }

    @Nested
    @DisplayName("getSourceName() - 소스명 반환")
    class GetSourceName {

        @Test
        @DisplayName("ApplyHome을 반환한다")
        void returnsApplyhomeApi() {
            // when
            String sourceName = adapter.getSourceName();

            // then
            assertThat(sourceName).isEqualTo("ApplyHome");
        }
    }

    @Nested
    @DisplayName("fetch() - 특정 날짜 데이터 수집")
    class Fetch {

        @Test
        @DisplayName("정상 응답 시 청약 정보 목록을 반환한다")
        void returnsSubscriptionsOnSuccess() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            ApplyhomeAptResponse mockResponse = createMockAptResponse(targetDate);

            mockWebClientGetSuccess(mockResponse);

            // when
            List<SubscriptionInfo> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isNotNull();
            verify(webClient, atLeast(4)).get();
        }

        @Test
        @DisplayName("null 응답 시 빈 리스트를 반환한다")
        void returnsEmptyListOnNullResponse() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            mockWebClientGetSuccess(null);

            // when
            List<SubscriptionInfo> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("fetchAll() - 전체 데이터 수집")
    class FetchAll {

        @Test
        @DisplayName("정상 응답 시 전체 청약 정보를 반환한다")
        void returnsAllSubscriptionsOnSuccess() {
            // given
            ApplyhomeAptResponse mockResponse = createMockAptResponse(LocalDate.now());

            mockWebClientGetSuccess(mockResponse);

            // when
            List<SubscriptionInfo> result = adapter.fetchAll("서울");

            // then
            assertThat(result).isNotNull();
            verify(webClient, atLeast(4)).get();
        }

        @Test
        @DisplayName("API 호출 실패 시 빈 리스트를 반환한다")
        void returnsEmptyListOnException() {
            // given
            when(webClient.get()).thenThrow(new RuntimeException("API 오류"));

            // when
            List<SubscriptionInfo> result = adapter.fetchAll("서울");

            // then
            assertThat(result).isEmpty();
        }
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientGetSuccess(ApplyhomeAptResponse response) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ApplyhomeAptResponse.class)).thenReturn(Mono.justOrEmpty(response));
        when(responseSpec.bodyToMono(ApplyhomeRemainingResponse.class)).thenReturn(Mono.empty());
        when(responseSpec.bodyToMono(ApplyhomeArbitraryResponse.class)).thenReturn(Mono.empty());
    }

    private ApplyhomeAptResponse createMockAptResponse(LocalDate targetDate) {
        List<ApplyhomeApiItem> items = List.of(
                new ApplyhomeApiItem(
                        "2025010001", "2025-1", "힐스테이트 강남", "01", "서울",
                        "2025-01-10", targetDate.toString(), "2025-01-25", "2025-02-01",
                        100, "https://example.com", "https://example.com/detail", "02-1234-5678",
                        "서울특별시 강남구 테헤란로 123", "06134"
                )
        );
        return new ApplyhomeAptResponse(1, items, 1, 1, 100, 1);
    }
}
