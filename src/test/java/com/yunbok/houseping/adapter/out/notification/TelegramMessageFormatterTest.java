package com.yunbok.houseping.adapter.out.notification;

import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TelegramMessageFormatter - 텔레그램 메시지 포맷터")
class TelegramMessageFormatterTest {

    private TelegramMessageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new TelegramMessageFormatter();
    }

    @Nested
    @DisplayName("formatBatchSummary() - 배치 요약 메시지")
    class FormatBatchSummary {

        @Test
        @DisplayName("청약 정보가 있으면 개수와 목록을 포함한 메시지를 반환한다")
        void returnsMessageWithCountAndList() {
            // given
            List<SubscriptionInfo> subscriptions = List.of(
                    createSubscription("힐스테이트 강남"),
                    createSubscription("래미안 판교")
            );

            // when
            String result = formatter.formatBatchSummary(subscriptions);

            // then
            assertThat(result).contains("오늘의 신규 청약 정보 2개");
            assertThat(result).contains("1. ");
            assertThat(result).contains("2. ");
        }

        @Test
        @DisplayName("청약 정보가 비어있으면 없음 메시지를 반환한다")
        void returnsNoDataMessageWhenEmpty() {
            // when
            String result = formatter.formatBatchSummary(List.of());

            // then
            assertThat(result).isEqualTo("📭 오늘은 신규 청약 정보가 없습니다.");
        }

        @Test
        @DisplayName("단일 청약 정보도 올바르게 포맷팅한다")
        void formatsSingleSubscription() {
            // given
            List<SubscriptionInfo> subscriptions = List.of(
                    createSubscription("테스트 아파트")
            );

            // when
            String result = formatter.formatBatchSummary(subscriptions);

            // then
            assertThat(result).contains("오늘의 신규 청약 정보 1개");
            assertThat(result).contains("1. ");
        }
    }

    @Nested
    @DisplayName("formatSubscription() - 단일 청약 메시지")
    class FormatSubscription {

        @Test
        @DisplayName("청약 정보의 상세 메시지를 반환한다")
        void returnsDetailMessage() {
            // given
            SubscriptionInfo subscription = createSubscription("테스트 아파트");

            // when
            String result = formatter.formatSubscription(subscription);

            // then
            assertThat(result).isNotBlank();
        }
    }

    @Nested
    @DisplayName("formatErrorMessage() - 에러 메시지")
    class FormatErrorMessage {

        @Test
        @DisplayName("에러 메시지 앞에 제목을 붙인다")
        void prependsErrorTitle() {
            // given
            String errorMessage = "API 호출 실패";

            // when
            String result = formatter.formatErrorMessage(errorMessage);

            // then
            assertThat(result).startsWith("청약 알리미 오류 발생");
            assertThat(result).contains("API 호출 실패");
        }
    }

    @Nested
    @DisplayName("formatNoDataMessage() - 데이터 없음 메시지")
    class FormatNoDataMessage {

        @Test
        @DisplayName("없음 메시지를 반환한다")
        void returnsNoDataMessage() {
            // when
            String result = formatter.formatNoDataMessage();

            // then
            assertThat(result).isEqualTo("📭 오늘은 신규 청약 정보가 없습니다.");
        }
    }

    private SubscriptionInfo createSubscription(String houseName) {
        return ApplyHomeSubscriptionInfo.builder()
                .houseName(houseName)
                .area("서울")
                .houseType("APT")
                .receiptStartDate(LocalDate.now())
                .receiptEndDate(LocalDate.now().plusDays(7))
                .build();
    }
}
