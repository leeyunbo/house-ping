package com.yunbok.houseping.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunbok.houseping.core.domain.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("LhWebScraperClient - LH 웹 캘린더 어댑터")
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class LhWebScraperClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private LhWebScraperClient adapter;

    @BeforeEach
    void setUp() {
        adapter = new LhWebScraperClient(webClient, new ObjectMapper());
    }

    @Nested
    @DisplayName("getSourceName() - 소스명 반환")
    class GetSourceName {

        @Test
        @DisplayName("LH를 반환한다")
        void returnsLh() {
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
            String jsonResponse = createMockJsonResponse(targetDate);
            mockWebClientPostSuccess(jsonResponse);

            // when
            List<Subscription> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isNotNull();
            verify(webClient).post();
        }

        @Test
        @DisplayName("null 응답 시 빈 리스트를 반환한다")
        void returnsEmptyListOnNullResponse() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            mockWebClientPostSuccess(null);

            // when
            List<Subscription> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("API 호출 실패 시 빈 리스트를 반환한다")
        void returnsEmptyListOnException() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            when(webClient.post()).thenThrow(new RuntimeException("API 오류"));

            // when
            List<Subscription> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("비주거용 공고는 필터링한다")
        void filtersNonResidentialAnnouncements() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            String jsonResponse = createNonResidentialJsonResponse(targetDate);
            mockWebClientPostSuccess(jsonResponse);

            // when
            List<Subscription> result = adapter.fetch("서울", targetDate);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("지역이 일치하지 않으면 필터링한다")
        void filtersNonMatchingArea() {
            // given
            LocalDate targetDate = LocalDate.of(2025, 1, 15);
            String jsonResponse = createMockJsonResponse(targetDate);
            mockWebClientPostSuccess(jsonResponse);

            // when
            List<Subscription> result = adapter.fetch("부산", targetDate);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("fetchAll() - 전체 데이터 수집")
    class FetchAll {

        @Test
        @DisplayName("fetch와 동일하게 동작한다")
        void delegatesToFetch() {
            // given
            mockWebClientPostSuccess(null);

            // when
            List<Subscription> result = adapter.fetchAll("서울");

            // then
            assertThat(result).isEmpty();
            verify(webClient).post();
        }
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientPostSuccess(String jsonResponse) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.justOrEmpty(jsonResponse));
    }

    private String createMockJsonResponse(LocalDate targetDate) {
        String dateStr = targetDate.toString().replace("-", "");
        return """
            {
                "panList": [
                    {
                        "panNm": "LH 행복주택 강남",
                        "cnpCdNm": "서울",
                        "uppAisTpCd": "05",
                        "panNtStDt": "20250110",
                        "acpStDttm": "%s",
                        "acpEdDttm": "20250125",
                        "panSs": "접수중",
                        "dtlUrl": "https://lh.or.kr/detail"
                    }
                ]
            }
            """.formatted(dateStr);
    }

    private String createNonResidentialJsonResponse(LocalDate targetDate) {
        String dateStr = targetDate.toString().replace("-", "");
        return """
            {
                "panList": [
                    {
                        "panNm": "강남역 상가 임대",
                        "cnpCdNm": "서울",
                        "uppAisTpCd": "05",
                        "panNtStDt": "20250110",
                        "acpStDttm": "%s",
                        "acpEdDttm": "20250125",
                        "panSs": "접수중",
                        "dtlUrl": "https://lh.or.kr/detail"
                    }
                ]
            }
            """.formatted(dateStr);
    }
}
