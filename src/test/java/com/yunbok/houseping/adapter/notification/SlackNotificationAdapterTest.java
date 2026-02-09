package com.yunbok.houseping.adapter.notification;

import com.yunbok.houseping.adapter.formatter.SlackMessageFormatter;
import com.yunbok.houseping.adapter.dto.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("SlackNotificationAdapter - Slack 알림 어댑터")
@ExtendWith(MockitoExtension.class)
class SlackNotificationAdapterTest {

    @Mock
    private SlackMessageFormatter messageFormatter;

    private SlackNotificationAdapter adapter;

    @BeforeEach
    void setUp() {
        // 테스트용으로 유효하지 않은 URL 사용 (실제 발송 안됨)
        adapter = new SlackNotificationAdapter("https://hooks.slack.com/test", messageFormatter);
    }

    @Nested
    @DisplayName("sendNewSubscriptions() - 신규 청약 알림")
    class SendNewSubscriptions {

        @Test
        @DisplayName("빈 리스트면 아무 것도 보내지 않는다")
        void doesNothingWhenEmpty() {
            // when
            adapter.sendNewSubscriptions(List.of());

            // then
            verify(messageFormatter, never()).formatBatchSummary(any());
        }

        @Test
        @DisplayName("청약 정보가 있으면 배치 요약 메시지를 포맷팅한다")
        void formatsBatchSummary() {
            // given
            List<SubscriptionInfo> subscriptions = List.of(createSubscription("테스트 아파트"));
            when(messageFormatter.formatBatchSummary(subscriptions)).thenReturn("요약 메시지");
            when(messageFormatter.formatSubscription(any())).thenReturn("상세 메시지");

            // when
            adapter.sendNewSubscriptions(subscriptions);

            // then
            verify(messageFormatter).formatBatchSummary(subscriptions);
        }

        @Test
        @DisplayName("각 청약 정보에 대해 개별 메시지를 포맷팅한다")
        void formatsEachSubscription() {
            // given
            List<SubscriptionInfo> subscriptions = List.of(
                    createSubscription("아파트1"),
                    createSubscription("아파트2")
            );
            when(messageFormatter.formatBatchSummary(any())).thenReturn("요약");
            when(messageFormatter.formatSubscription(any())).thenReturn("상세");

            // when
            adapter.sendNewSubscriptions(subscriptions);

            // then
            verify(messageFormatter, times(2)).formatSubscription(any());
        }
    }

    @Nested
    @DisplayName("sendSubscription() - 단일 청약 알림")
    class SendSubscription {

        @Test
        @DisplayName("청약 정보를 포맷팅하여 전송한다")
        void formatsAndSends() {
            // given
            SubscriptionInfo subscription = createSubscription("테스트 아파트");
            when(messageFormatter.formatSubscription(subscription)).thenReturn("포맷된 메시지");

            // when
            adapter.sendSubscription(subscription);

            // then
            verify(messageFormatter).formatSubscription(subscription);
        }
    }

    @Nested
    @DisplayName("sendErrorNotification() - 에러 알림")
    class SendErrorNotification {

        @Test
        @DisplayName("에러 메시지를 포맷팅하여 전송한다")
        void formatsAndSendsError() {
            // given
            String errorMessage = "API 호출 실패";
            when(messageFormatter.formatErrorMessage(errorMessage)).thenReturn("포맷된 에러 메시지");

            // when
            adapter.sendErrorNotification(errorMessage);

            // then
            verify(messageFormatter).formatErrorMessage(errorMessage);
        }
    }

    @Nested
    @DisplayName("sendNotification() - 일반 알림")
    class SendNotification {

        @Test
        @DisplayName("메시지를 그대로 전송한다")
        void sendsMessageDirectly() {
            // when
            adapter.sendNotification("테스트 메시지");

            // then - WebClient 호출 확인은 통합 테스트에서 수행
            // 단위 테스트에서는 예외가 발생하지 않는지만 확인
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
