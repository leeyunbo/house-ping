package com.yunbok.houseping.infrastructure.api;

import com.yunbok.houseping.support.external.CompetitionRateItem;
import com.yunbok.houseping.support.external.CompetitionRateResponse;
import com.yunbok.houseping.core.domain.CompetitionRate;
import com.yunbok.houseping.config.SubscriptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ApplyhomeCompetitionRateClient - 경쟁률 API 어댑터")
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ApplyhomeCompetitionRateClientTest {

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

    private ApplyhomeCompetitionRateClient adapter;

    @BeforeEach
    void setUp() {
        adapter = new ApplyhomeCompetitionRateClient(webClient, properties, "test-api-key");

        when(properties.getApi()).thenReturn(apiProperties);
        when(apiProperties.getPageSize()).thenReturn(100);
        when(apiProperties.getDefaultPage()).thenReturn(1);
    }

    @Nested
    @DisplayName("fetchAll() - 전체 경쟁률 수집")
    class FetchAll {

        @Test
        @DisplayName("정상 응답 시 경쟁률 목록을 반환한다")
        void returnsRatesOnSuccess() {
            // given
            CompetitionRateResponse mockResponse = createMockResponse();
            mockWebClientGetSuccess(mockResponse);

            // when
            List<CompetitionRate> result = adapter.fetchAll();

            // then
            assertThat(result).isNotEmpty();
            verify(webClient, atLeastOnce()).get();
        }

        @Test
        @DisplayName("null 응답 시 빈 리스트를 반환한다")
        void returnsEmptyListOnNullResponse() {
            // given
            mockWebClientGetSuccess(null);

            // when
            List<CompetitionRate> result = adapter.fetchAll();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("API 호출 실패 시 빈 리스트를 반환한다")
        void returnsEmptyListOnException() {
            // given
            when(webClient.get()).thenThrow(new RuntimeException("API 오류"));

            // when
            List<CompetitionRate> result = adapter.fetchAll();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("여러 페이지의 데이터를 수집한다")
        void collectsMultiplePages() {
            // given
            CompetitionRateResponse firstPage = new CompetitionRateResponse(
                    1,
                    100,
                    List.of(createMockItem())
            );
            mockWebClientGetSuccess(firstPage);

            // when
            List<CompetitionRate> result = adapter.fetchAll();

            // then
            assertThat(result).isNotEmpty();
        }
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientGetSuccess(CompetitionRateResponse response) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CompetitionRateResponse.class)).thenReturn(Mono.justOrEmpty(response));
    }

    private CompetitionRateResponse createMockResponse() {
        return new CompetitionRateResponse(
                1,
                1,
                List.of(createMockItem())
        );
    }

    private CompetitionRateItem createMockItem() {
        return new CompetitionRateItem(
                "2025010001",
                "2025-1",
                "084.9543T",
                100,
                "500",
                "5.0",
                "해당지역",
                1
        );
    }
}
