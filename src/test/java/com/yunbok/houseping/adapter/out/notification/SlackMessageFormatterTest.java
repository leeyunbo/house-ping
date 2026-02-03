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

@DisplayName("SlackMessageFormatter - Slack 메시지 포맷터")
class SlackMessageFormatterTest {

    private SlackMessageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new SlackMessageFormatter();
    }

    @Nested
    @DisplayName("formatBatchSummary() - 배치 요약 메시지 생성")
    class FormatBatchSummary {

        @Test
        @DisplayName("청약 정보가 없으면 '신규 정보 없음' 메시지를 반환한다")
        void returnsNoDataMessageWhenEmpty() {
            // given
            List<SubscriptionInfo> emptyList = List.of();

            // when
            String message = formatter.formatBatchSummary(emptyList);

            // then
            assertThat(message).contains("오늘은 신규 청약 정보가 없습니다");
        }

        @Test
        @DisplayName("청약 정보가 있으면 개수와 목록을 포함한 요약을 반환한다")
        void returnsSummaryWithCountAndList() {
            // given
            List<SubscriptionInfo> subscriptions = List.of(
                    createSubscriptionInfo("힐스테이트 강남", "서울"),
                    createSubscriptionInfo("래미안 판교", "경기")
            );

            // when
            String message = formatter.formatBatchSummary(subscriptions);

            // then
            assertThat(message).contains(":tada:");
            assertThat(message).contains("2개");
            assertThat(message).contains("1.");
            assertThat(message).contains("2.");
            assertThat(message).contains("힐스테이트 강남");
            assertThat(message).contains("래미안 판교");
        }

        @Test
        @DisplayName("Slack mrkdwn 형식으로 굵게 표시한다")
        void usesSlackMarkdownBold() {
            // given
            List<SubscriptionInfo> subscriptions = List.of(
                    createSubscriptionInfo("테스트아파트", "서울")
            );

            // when
            String message = formatter.formatBatchSummary(subscriptions);

            // then
            assertThat(message).contains("*오늘의 신규 청약 정보");
        }
    }

    @Nested
    @DisplayName("formatSubscription() - 개별 청약 메시지 생성")
    class FormatSubscription {

        @Test
        @DisplayName("청약 정보의 상세 표시 메시지를 반환한다")
        void returnsDetailedMessage() {
            // given
            SubscriptionInfo subscription = ApplyHomeSubscriptionInfo.builder()
                    .houseName("힐스테이트 강남")
                    .area("서울")
                    .receiptStartDate(LocalDate.of(2025, 1, 5))
                    .receiptEndDate(LocalDate.of(2025, 1, 10))
                    .winnerAnnounceDate(LocalDate.of(2025, 1, 20))
                    .totalSupplyCount(500)
                    .detailUrl("https://example.com")
                    .build();

            // when
            String message = formatter.formatSubscription(subscription);

            // then
            assertThat(message).contains("힐스테이트 강남");
            assertThat(message).contains("서울");
        }
    }

    @Nested
    @DisplayName("formatErrorMessage() - 에러 메시지 생성")
    class FormatErrorMessage {

        @Test
        @DisplayName("에러 메시지에 Slack 이모지와 굵은 제목을 포함한다")
        void includesEmojiAndBoldTitle() {
            // given
            String errorMessage = "API 호출 실패";

            // when
            String formatted = formatter.formatErrorMessage(errorMessage);

            // then
            assertThat(formatted).contains(":rotating_light:");
            assertThat(formatted).contains("*청약 알리미 오류 발생*");
            assertThat(formatted).contains("API 호출 실패");
        }
    }

    @Nested
    @DisplayName("formatNoDataMessage() - 데이터 없음 메시지")
    class FormatNoDataMessage {

        @Test
        @DisplayName("Slack 이모지를 포함한 메시지를 반환한다")
        void returnsMessageWithEmoji() {
            // when
            String message = formatter.formatNoDataMessage();

            // then
            assertThat(message).contains(":mailbox_with_no_mail:");
            assertThat(message).contains("오늘은 신규 청약 정보가 없습니다");
        }
    }

    private SubscriptionInfo createSubscriptionInfo(String houseName, String area) {
        return ApplyHomeSubscriptionInfo.builder()
                .houseName(houseName)
                .area(area)
                .build();
    }
}
