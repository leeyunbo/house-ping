package com.yunbok.houseping.adapter.out.notification;

import com.yunbok.houseping.domain.model.ApplyHomeSubscriptionInfo;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("TelegramNotificationAdapter - 텔레그램 알림 어댑터")
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class TelegramNotificationAdapterTest {

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

    @Mock
    private TelegramMessageFormatter messageFormatter;

    private TelegramNotificationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TelegramNotificationAdapter(webClient, messageFormatter);
        ReflectionTestUtils.setField(adapter, "chatIds", List.of("123456789"));
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
        @DisplayName("청약 정보가 있으면 배치 요약 메시지를 발송한다")
        void sendsBatchSummary() {
            // given
            List<SubscriptionInfo> subscriptions = List.of(createSubscription("테스트 아파트"));
            when(messageFormatter.formatBatchSummary(subscriptions)).thenReturn("요약 메시지");
            when(messageFormatter.formatSubscription(any())).thenReturn("상세 메시지");
            mockWebClientPost();

            // when
            adapter.sendNewSubscriptions(subscriptions);

            // then
            verify(messageFormatter).formatBatchSummary(subscriptions);
        }

        @Test
        @DisplayName("각 청약 정보에 대해 개별 메시지를 발송한다")
        void sendsEachSubscription() {
            // given
            List<SubscriptionInfo> subscriptions = List.of(
                    createSubscription("아파트1"),
                    createSubscription("아파트2")
            );
            when(messageFormatter.formatBatchSummary(any())).thenReturn("요약");
            when(messageFormatter.formatSubscription(any())).thenReturn("상세");
            mockWebClientPost();

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
            mockWebClientPost();

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
            mockWebClientPost();

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
            // given
            mockWebClientPost();

            // when
            adapter.sendNotification("테스트 메시지");

            // then
            verify(webClient).post();
        }

        @Test
        @DisplayName("전송 실패해도 예외가 발생하지 않는다")
        void handlesExceptionGracefully() {
            // given
            when(webClient.post()).thenThrow(new RuntimeException("전송 실패"));

            // when
            adapter.sendNotification("테스트 메시지");

            // then - 예외 처리됨
        }

        @Test
        @DisplayName("여러 chat ID에 메시지를 전송한다")
        void sendsToMultipleChatIds() {
            // given
            ReflectionTestUtils.setField(adapter, "chatIds", List.of("111", "222", "333"));
            mockWebClientPost();

            // when
            adapter.sendNotification("테스트 메시지");

            // then
            verify(webClient, times(3)).post();
        }
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientPost() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("ok", true)));
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
