package com.yunbok.houseping.adapter.api;

import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
import com.yunbok.houseping.config.SubscriptionProperties;
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

@DisplayName("LhApiAdapter - LH API 어댑터")
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class LhApiAdapterTest {

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

    private LhApiAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LhApiAdapter(webClient, properties);
        ReflectionTestUtils.setField(adapter, "apiKey", "test-api-key");

        when(properties.getApi()).thenReturn(apiProperties);
        when(apiProperties.getPageSize()).thenReturn(100);
        when(apiProperties.getDefaultPage()).thenReturn(1);
    }

    @Nested
    @DisplayName("getSourceName() - 소스명 반환")
    class GetSourceName {

        @Test
        @DisplayName("LH를 반환한다")
        void returnsLhApi() {
            // when
            String sourceName = adapter.getSourceName();

            // then
            assertThat(sourceName).isEqualTo("LH");
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
            String jsonResponse = createMockLhJsonResponse();

            mockWebClientGetSuccess(jsonResponse);

            // when
            List<SubscriptionInfo> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isNotNull();
            verify(webClient, times(3)).get();
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
            String jsonResponse = createMockLhJsonResponse();

            mockWebClientGetSuccess(jsonResponse);

            // when
            List<SubscriptionInfo> result = adapter.fetchAll("서울");

            // then
            assertThat(result).isNotNull();
            verify(webClient, times(3)).get();
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
    private void mockWebClientGetSuccess(String jsonResponse) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.justOrEmpty(jsonResponse));
    }

    /**
     * LH API 응답은 배열 형태로, 두 번째 요소에 dsList가 포함됨
     */
    private String createMockLhJsonResponse() {
        return """
            [
                {"header": "info"},
                {
                    "dsList": [
                        {
                            "PAN_NM": "LH 행복주택 강남",
                            "CNP_CD_NM": "서울",
                            "UPP_AIS_TP_CD": "10",
                            "PAN_NT_ST_DT": "2025-01-10",
                            "CLSG_DT": "2025-01-25",
                            "PAN_SS": "접수중",
                            "DTL_URL": "https://lh.or.kr/detail"
                        },
                        {
                            "PAN_NM": "LH 신혼희망타운 판교",
                            "CNP_CD_NM": "경기",
                            "UPP_AIS_TP_CD": "20",
                            "PAN_NT_ST_DT": "2025-01-10",
                            "CLSG_DT": "2025-01-25",
                            "PAN_SS": "접수중",
                            "DTL_URL": "https://lh.or.kr/detail2"
                        }
                    ]
                }
            ]
            """;
    }
}
