package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.DailyNotificationReport;
import com.yunbok.houseping.domain.model.NotificationTarget;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.out.NotificationSubscriptionPersistencePort;
import com.yunbok.houseping.domain.port.out.notification.NotificationSender;
import com.yunbok.houseping.infrastructure.persistence.NotificationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("DailyNotificationService - 일일 종합 알림 서비스")
@ExtendWith(MockitoExtension.class)
class DailyNotificationServiceTest {

    @Mock
    private SubscriptionCollector subscriptionCollector;

    @Mock
    private NotificationSubscriptionPersistencePort persistencePort;

    @Mock
    private NotificationSender notificationSender;

    @Mock
    private NotificationHistoryRepository historyRepository;

    private DailyNotificationService service;

    @BeforeEach
    void setUp() {
        service = new DailyNotificationService(
                subscriptionCollector,
                persistencePort,
                Optional.of(notificationSender),
                historyRepository
        );
    }

    @Nested
    @DisplayName("sendDailyReport() - 일일 종합 알림 발송")
    class SendDailyReport {

        @Test
        @DisplayName("모든 섹션에 데이터가 있을 때 리포트를 발송한다")
        void sendsReportWithAllSections() {
            // given
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            NotificationTarget endTarget = new NotificationTarget(
                    1L, 100L, "힐스테이트 송도", "인천",
                    today.minusDays(5), today, 150, "https://example.com/1"
            );

            NotificationTarget startTarget = new NotificationTarget(
                    2L, 200L, "래미안 원펜타스", "서울",
                    tomorrow, tomorrow.plusDays(5), 200, "https://example.com/2"
            );

            SubscriptionInfo newSubscription = mock(SubscriptionInfo.class);

            when(persistencePort.findPendingReceiptEndTargets(today))
                    .thenReturn(List.of(endTarget));
            when(persistencePort.findPendingReceiptStartTargets(tomorrow))
                    .thenReturn(List.of(startTarget));
            when(subscriptionCollector.collectFromAllAreas(today))
                    .thenReturn(List.of(newSubscription));

            // when
            service.sendDailyReport();

            // then
            ArgumentCaptor<DailyNotificationReport> reportCaptor =
                    ArgumentCaptor.forClass(DailyNotificationReport.class);
            verify(notificationSender).sendDailyReport(reportCaptor.capture());

            DailyNotificationReport report = reportCaptor.getValue();
            assertThat(report.receiptEndToday()).hasSize(1);
            assertThat(report.receiptStartTomorrow()).hasSize(1);
            assertThat(report.newSubscriptions()).hasSize(1);
            assertThat(report.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("데이터가 없어도 빈 리포트를 발송한다")
        void sendsEmptyReport() {
            // given
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            when(persistencePort.findPendingReceiptEndTargets(today))
                    .thenReturn(List.of());
            when(persistencePort.findPendingReceiptStartTargets(tomorrow))
                    .thenReturn(List.of());
            when(subscriptionCollector.collectFromAllAreas(today))
                    .thenReturn(List.of());

            // when
            service.sendDailyReport();

            // then
            ArgumentCaptor<DailyNotificationReport> reportCaptor =
                    ArgumentCaptor.forClass(DailyNotificationReport.class);
            verify(notificationSender).sendDailyReport(reportCaptor.capture());

            DailyNotificationReport report = reportCaptor.getValue();
            assertThat(report.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("리포트 발송 후 알림 상태를 업데이트한다")
        void marksNotificationsAsSent() {
            // given
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            NotificationTarget endTarget = new NotificationTarget(
                    1L, 100L, "힐스테이트 송도", "인천",
                    today.minusDays(5), today, 150, "https://example.com/1"
            );

            NotificationTarget startTarget = new NotificationTarget(
                    2L, 200L, "래미안 원펜타스", "서울",
                    tomorrow, tomorrow.plusDays(5), 200, "https://example.com/2"
            );

            when(persistencePort.findPendingReceiptEndTargets(today))
                    .thenReturn(List.of(endTarget));
            when(persistencePort.findPendingReceiptStartTargets(tomorrow))
                    .thenReturn(List.of(startTarget));
            when(subscriptionCollector.collectFromAllAreas(today))
                    .thenReturn(List.of());

            // when
            service.sendDailyReport();

            // then
            verify(persistencePort).markReceiptEndNotified(1L);
            verify(persistencePort).markReceiptStartNotified(2L);
        }

        @Test
        @DisplayName("알림 발송기가 비활성화되어 있으면 아무 작업도 하지 않는다")
        void doesNothingWhenSenderDisabled() {
            // given
            DailyNotificationService serviceWithoutSender = new DailyNotificationService(
                    subscriptionCollector,
                    persistencePort,
                    Optional.empty(),
                    historyRepository
            );

            // when
            serviceWithoutSender.sendDailyReport();

            // then
            verify(subscriptionCollector, never()).collectFromAllAreas(any());
            verify(persistencePort, never()).findPendingReceiptEndTargets(any());
            verify(persistencePort, never()).findPendingReceiptStartTargets(any());
        }

        @Test
        @DisplayName("발송 실패 시 예외를 던지고 실패 이력을 저장한다")
        void savesFailureHistoryWhenSendingFails() {
            // given
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            when(persistencePort.findPendingReceiptEndTargets(today))
                    .thenReturn(List.of());
            when(persistencePort.findPendingReceiptStartTargets(tomorrow))
                    .thenReturn(List.of());
            when(subscriptionCollector.collectFromAllAreas(today))
                    .thenReturn(List.of());
            doThrow(new RuntimeException("발송 실패"))
                    .when(notificationSender).sendDailyReport(any());

            // when & then
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.sendDailyReport())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("발송 실패");

            verify(historyRepository).save(any());
        }
    }

    @Nested
    @DisplayName("sendDailyReportManual() - 수동 일일 알림 발송")
    class SendDailyReportManual {

        @Test
        @DisplayName("수동으로 일일 리포트를 발송한다")
        void sendsReportManually() {
            // given
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            when(persistencePort.findPendingReceiptEndTargets(today))
                    .thenReturn(List.of());
            when(persistencePort.findPendingReceiptStartTargets(tomorrow))
                    .thenReturn(List.of());
            when(subscriptionCollector.collectFromAllAreas(today))
                    .thenReturn(List.of());

            // when
            service.sendDailyReportManual();

            // then
            verify(notificationSender).sendDailyReport(any(DailyNotificationReport.class));
        }
    }
}
