package com.yunbok.houseping.adapter.formatter;

import com.yunbok.houseping.adapter.dto.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.support.dto.DailyNotificationReport;
import com.yunbok.houseping.support.dto.NotificationTarget;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
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

    @Nested
    @DisplayName("formatDailyReport() - 일일 알림 리포트 생성")
    class FormatDailyReport {

        @Test
        @DisplayName("모든 데이터가 있을 때 각 섹션을 명확히 구분한다")
        void formatsAllSectionsWhenDataExists() {
            // given
            NotificationTarget endTarget = createNotificationTarget("수지자이 에디시온", "경기", 258);
            NotificationTarget startTarget = createNotificationTarget("래미안 원펜타스", "서울", 200);
            SubscriptionInfo newSub = createSubscriptionInfo("안양역 센트럴", "경기");

            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(endTarget),
                    List.of(startTarget),
                    List.of(newSub)
            );

            // when
            String message = formatter.formatDailyReport(report);

            // then
            assertThat(message).contains(":bell:");
            assertThat(message).contains("청약 알림");
            assertThat(message).contains(":pushpin:");
            assertThat(message).contains("내 관심 청약");
            assertThat(message).contains(":red_circle:");
            assertThat(message).contains("오늘 마감");
            assertThat(message).contains(":large_green_circle:");
            assertThat(message).contains("내일 접수 시작");
            assertThat(message).contains(":newspaper:");
            assertThat(message).contains("오늘의 신규 청약");
            assertThat(message).contains("수지자이 에디시온");
            assertThat(message).contains("래미안 원펜타스");
            assertThat(message).contains("안양역 센트럴");
        }

        @Test
        @DisplayName("등록된 알림이 없으면 '등록된 알림 없음'을 표시한다")
        void showsNoRegisteredAlertWhenEmpty() {
            // given
            SubscriptionInfo newSub = createSubscriptionInfo("힐스테이트 송도", "인천");
            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(),
                    List.of(),
                    List.of(newSub)
            );

            // when
            String message = formatter.formatDailyReport(report);

            // then
            assertThat(message).contains("등록된 알림 없음");
            assertThat(message).contains("힐스테이트 송도");
        }

        @Test
        @DisplayName("신규 청약이 없으면 '신규 청약 없음'을 표시한다")
        void showsNoNewSubscriptionsWhenEmpty() {
            // given
            NotificationTarget endTarget = createNotificationTarget("테스트 아파트", "서울", 100);
            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(endTarget),
                    List.of(),
                    List.of()
            );

            // when
            String message = formatter.formatDailyReport(report);

            // then
            assertThat(message).contains("신규 청약 없음");
            assertThat(message).contains("테스트 아파트");
        }

        @Test
        @DisplayName("모든 데이터가 없어도 전체 구조를 표시한다")
        void showsStructureEvenWhenAllEmpty() {
            // given
            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(),
                    List.of(),
                    List.of()
            );

            // when
            String message = formatter.formatDailyReport(report);

            // then
            assertThat(message).contains("내 관심 청약");
            assertThat(message).contains("등록된 알림 없음");
            assertThat(message).contains("오늘의 신규 청약");
            assertThat(message).contains("신규 청약 없음");
            assertThat(message).contains("오늘 마감 *0*건");
        }

        @Test
        @DisplayName("신규 청약이 5개 초과면 5개만 표시하고 나머지는 '외 N건'으로 표시한다")
        void limitsNewSubscriptionsToFive() {
            // given
            List<SubscriptionInfo> newSubs = List.of(
                    createSubscriptionInfo("아파트1", "서울"),
                    createSubscriptionInfo("아파트2", "서울"),
                    createSubscriptionInfo("아파트3", "서울"),
                    createSubscriptionInfo("아파트4", "서울"),
                    createSubscriptionInfo("아파트5", "서울"),
                    createSubscriptionInfo("아파트6", "서울"),
                    createSubscriptionInfo("아파트7", "서울")
            );
            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(),
                    List.of(),
                    newSubs
            );

            // when
            String message = formatter.formatDailyReport(report);

            // then
            assertThat(message).contains("아파트1");
            assertThat(message).contains("아파트5");
            assertThat(message).doesNotContain("아파트6");
            assertThat(message).contains("외 2건");
        }

        @Test
        @DisplayName("하단 요약에 각 섹션 건수를 표시한다")
        void showsSummaryCountsAtBottom() {
            // given
            DailyNotificationReport report = new DailyNotificationReport(
                    List.of(createNotificationTarget("마감1", "서울", 100), createNotificationTarget("마감2", "경기", 200)),
                    List.of(createNotificationTarget("내일1", "서울", 150)),
                    List.of(createSubscriptionInfo("신규1", "서울"), createSubscriptionInfo("신규2", "경기"), createSubscriptionInfo("신규3", "인천"))
            );

            // when
            String message = formatter.formatDailyReport(report);

            // then
            assertThat(message).contains("오늘 마감 *2*건");
            assertThat(message).contains("내일 접수 *1*건");
            assertThat(message).contains("신규 *3*건");
        }
    }

    private SubscriptionInfo createSubscriptionInfo(String houseName, String area) {
        return ApplyHomeSubscriptionInfo.builder()
                .houseName(houseName)
                .area(area)
                .build();
    }

    private NotificationTarget createNotificationTarget(String houseName, String area, int supplyCount) {
        return new NotificationTarget(
                1L, 100L, houseName, area,
                LocalDate.now().minusDays(5), LocalDate.now(),
                supplyCount, "https://example.com"
        );
    }
}
