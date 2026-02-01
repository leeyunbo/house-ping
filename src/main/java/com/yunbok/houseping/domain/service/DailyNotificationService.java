package com.yunbok.houseping.domain.service;

import com.yunbok.houseping.domain.model.DailyNotificationReport;
import com.yunbok.houseping.domain.model.NotificationTarget;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.out.NotificationSubscriptionPersistencePort;
import com.yunbok.houseping.domain.port.out.notification.NotificationSender;
import com.yunbok.houseping.infrastructure.persistence.NotificationHistoryEntity;
import com.yunbok.houseping.infrastructure.persistence.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 일일 종합 알림 서비스
 * 신규 청약, 접수 시작, 접수 마감 알림을 하나의 리포트로 통합 발송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyNotificationService {

    private final SubscriptionCollector subscriptionCollector;
    private final NotificationSubscriptionPersistencePort persistencePort;
    private final Optional<NotificationSender> notificationSender;
    private final NotificationHistoryRepository historyRepository;

    /**
     * 일일 종합 알림 리포트 생성 (미리보기용, 발송 안함)
     */
    @Transactional(readOnly = true)
    public DailyNotificationReport generateReport() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<NotificationTarget> receiptEndToday = persistencePort.findPendingReceiptEndTargets(today);
        List<NotificationTarget> receiptStartTomorrow = persistencePort.findPendingReceiptStartTargets(tomorrow);
        List<SubscriptionInfo> newSubscriptions = subscriptionCollector.collectFromAllAreas(today);

        return new DailyNotificationReport(
                receiptEndToday,
                receiptStartTomorrow,
                newSubscriptions
        );
    }

    /**
     * 일일 종합 알림 리포트 발송 (스케줄러용)
     */
    @Transactional
    public void sendDailyReport() {
        sendDailyReport("SCHEDULER");
    }

    /**
     * 일일 종합 알림 리포트 발송 (수동 발송용)
     */
    @Transactional
    public void sendDailyReportManual() {
        sendDailyReport("MANUAL");
    }

    /**
     * 일일 종합 알림 리포트 발송
     */
    private void sendDailyReport(String triggeredBy) {
        if (notificationSender.isEmpty()) {
            log.debug("[일일 알림 서비스] 알림 발송기가 비활성화 상태입니다.");
            saveHistory("DAILY_REPORT", "ALL", false,
                    "알림 발송기 비활성화", null, "알림 발송기가 비활성화 상태입니다.", triggeredBy);
            return;
        }

        DailyNotificationReport report = generateReport();

        log.info("[일일 알림 서비스] 리포트 생성 완료 - 신규: {}건, 내일 접수: {}건, 오늘 마감: {}건",
                report.newSubscriptions().size(),
                report.receiptStartTomorrow().size(),
                report.receiptEndToday().size());

        String summary = String.format("신규 %d건, 내일접수 %d건, 오늘마감 %d건",
                report.newSubscriptions().size(),
                report.receiptStartTomorrow().size(),
                report.receiptEndToday().size());

        String detail = buildDetailJson(report);

        try {
            // 리포트 발송
            notificationSender.get().sendDailyReport(report);

            // 발송 완료 처리
            markNotificationsAsSent(report.receiptEndToday(), report.receiptStartTomorrow());

            // 성공 이력 저장
            saveHistory("DAILY_REPORT", "ALL", true, summary, detail, null, triggeredBy);

            log.info("[일일 알림 서비스] 일일 리포트 발송 완료");
        } catch (Exception e) {
            log.error("[일일 알림 서비스] 일일 리포트 발송 실패", e);
            // 실패 이력 저장
            saveHistory("DAILY_REPORT", "ALL", false, summary, detail, e.getMessage(), triggeredBy);
            throw e;
        }
    }

    private void markNotificationsAsSent(
            List<NotificationTarget> receiptEndTargets,
            List<NotificationTarget> receiptStartTargets) {

        for (NotificationTarget target : receiptEndTargets) {
            persistencePort.markReceiptEndNotified(target.notificationId());
        }

        for (NotificationTarget target : receiptStartTargets) {
            persistencePort.markReceiptStartNotified(target.notificationId());
        }
    }

    private void saveHistory(String type, String channel, boolean success,
                             String summary, String detail, String errorMessage, String triggeredBy) {
        NotificationHistoryEntity history = NotificationHistoryEntity.builder()
                .notificationType(type)
                .channel(channel)
                .success(success)
                .summary(summary)
                .detail(detail)
                .errorMessage(errorMessage)
                .triggeredBy(triggeredBy)
                .build();
        historyRepository.save(history);
    }

    private String buildDetailJson(DailyNotificationReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // 오늘 마감
        sb.append("\"receiptEndToday\":[");
        sb.append(report.receiptEndToday().stream()
                .map(t -> String.format("{\"id\":%d,\"name\":\"%s\",\"area\":\"%s\"}",
                        t.subscriptionId(), escape(t.houseName()), escape(t.area())))
                .collect(Collectors.joining(",")));
        sb.append("],");

        // 내일 시작
        sb.append("\"receiptStartTomorrow\":[");
        sb.append(report.receiptStartTomorrow().stream()
                .map(t -> String.format("{\"id\":%d,\"name\":\"%s\",\"area\":\"%s\"}",
                        t.subscriptionId(), escape(t.houseName()), escape(t.area())))
                .collect(Collectors.joining(",")));
        sb.append("],");

        // 신규 청약
        sb.append("\"newSubscriptions\":[");
        sb.append(report.newSubscriptions().stream()
                .map(s -> String.format("{\"name\":\"%s\",\"area\":\"%s\"}",
                        escape(s.getHouseName()), escape(s.getArea())))
                .collect(Collectors.joining(",")));
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private String escape(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
