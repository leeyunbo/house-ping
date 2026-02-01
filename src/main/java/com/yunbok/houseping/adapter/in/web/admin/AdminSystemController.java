package com.yunbok.houseping.adapter.in.web.admin;

import com.yunbok.houseping.adapter.out.api.ApplyhomeApiAdapter;
import com.yunbok.houseping.adapter.out.notification.SlackMessageFormatter;
import com.yunbok.houseping.adapter.out.notification.TelegramMessageFormatter;
import com.yunbok.houseping.domain.model.DailyNotificationReport;
import com.yunbok.houseping.domain.service.DailyNotificationService;
import com.yunbok.houseping.infrastructure.persistence.NotificationHistoryEntity;
import com.yunbok.houseping.infrastructure.persistence.NotificationHistoryRepository;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionEntity;
import com.yunbok.houseping.infrastructure.persistence.SubscriptionPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 시스템 관리 컨트롤러 (MASTER 전용)
 */
@Slf4j
@Controller
@RequestMapping("/admin/system")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MASTER')")
public class AdminSystemController {

    private final DailyNotificationService dailyNotificationService;
    private final SlackMessageFormatter slackMessageFormatter;
    private final TelegramMessageFormatter telegramMessageFormatter;
    private final com.yunbok.houseping.infrastructure.persistence.NotificationSubscriptionRepository notificationSubscriptionRepository;
    private final com.yunbok.houseping.infrastructure.persistence.SubscriptionRepository subscriptionRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final ApplyhomeApiAdapter applyhomeApiAdapter;
    private final SubscriptionPriceRepository subscriptionPriceRepository;

    @GetMapping
    public String systemPage(Model model) {
        // 디버깅용 정보
        var allNotifications = notificationSubscriptionRepository.findAll();
        var pendingStart = notificationSubscriptionRepository.findPendingReceiptStartNotifications();
        var pendingEnd = notificationSubscriptionRepository.findPendingReceiptEndNotifications();

        // 청약 정보와 조인
        var subscriptionIds = allNotifications.stream()
                .map(n -> n.getSubscriptionId())
                .toList();
        var subscriptions = subscriptionRepository.findAllById(subscriptionIds);
        var subscriptionMap = subscriptions.stream()
                .collect(java.util.stream.Collectors.toMap(
                        s -> s.getId(),
                        s -> s
                ));

        // DTO로 변환
        var notificationDetails = allNotifications.stream()
                .map(n -> {
                    var sub = subscriptionMap.get(n.getSubscriptionId());
                    return new NotificationDetailDto(
                            n.getId(),
                            n.getSubscriptionId(),
                            sub != null ? sub.getHouseName() : "삭제됨",
                            sub != null ? sub.getArea() : "-",
                            sub != null ? sub.getReceiptStartDate() : null,
                            sub != null ? sub.getReceiptEndDate() : null,
                            n.isEnabled(),
                            n.isReceiptStartNotified(),
                            n.isReceiptEndNotified()
                    );
                })
                .toList();

        model.addAttribute("totalNotificationCount", allNotifications.size());
        model.addAttribute("pendingStartCount", pendingStart.size());
        model.addAttribute("pendingEndCount", pendingEnd.size());
        model.addAttribute("notifications", notificationDetails);

        // 발송 이력 조회 (최근 50건)
        List<NotificationHistoryEntity> history = notificationHistoryRepository.findTop50ByOrderBySentAtDesc();
        model.addAttribute("notificationHistory", history);

        return "admin/system/index";
    }

    public record NotificationDetailDto(
            Long id,
            Long subscriptionId,
            String houseName,
            String area,
            java.time.LocalDate receiptStartDate,
            java.time.LocalDate receiptEndDate,
            boolean enabled,
            boolean receiptStartNotified,
            boolean receiptEndNotified
    ) {}

    /**
     * 일일 알림 미리보기 (발송 안함)
     */
    @GetMapping("/preview-daily-report")
    public String previewDailyReport(Model model) {
        try {
            log.info("[시스템 관리] 일일 알림 미리보기 요청");
            DailyNotificationReport report = dailyNotificationService.generateReport();

            String slackMessage = slackMessageFormatter.formatDailyReport(report);
            String telegramMessage = telegramMessageFormatter.formatDailyReport(report);

            model.addAttribute("report", report);
            model.addAttribute("slackMessage", slackMessage);
            model.addAttribute("telegramMessage", telegramMessage);

            log.info("[시스템 관리] 미리보기 생성 완료 - 신규: {}건, 내일 접수: {}건, 오늘 마감: {}건",
                    report.newSubscriptions().size(),
                    report.receiptStartTomorrow().size(),
                    report.receiptEndToday().size());

        } catch (Exception e) {
            log.error("[시스템 관리] 미리보기 생성 실패", e);
            model.addAttribute("error", "미리보기 생성 실패: " + e.getMessage());
        }
        return "admin/system/preview";
    }

    /**
     * 일일 알림 실제 발송
     */
    @PostMapping("/send-daily-report")
    public String sendDailyReport(RedirectAttributes redirectAttributes) {
        try {
            log.info("[시스템 관리] 일일 알림 수동 발송 시작");
            dailyNotificationService.sendDailyReportManual();
            log.info("[시스템 관리] 일일 알림 수동 발송 완료");
            redirectAttributes.addFlashAttribute("message", "일일 알림이 발송되었습니다.");
        } catch (Exception e) {
            log.error("[시스템 관리] 일일 알림 발송 실패", e);
            redirectAttributes.addFlashAttribute("error", "알림 발송 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }

    /**
     * 알림 발송 상태 리셋 (테스트용)
     */
    @PostMapping("/reset-notification/{id}")
    public String resetNotification(@org.springframework.web.bind.annotation.PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            var notification = notificationSubscriptionRepository.findById(id);
            if (notification.isPresent()) {
                var entity = notification.get();
                entity.resetNotificationStatus();
                notificationSubscriptionRepository.save(entity);
                redirectAttributes.addFlashAttribute("message", "알림 상태가 리셋되었습니다. (ID: " + id + ")");
            } else {
                redirectAttributes.addFlashAttribute("error", "알림을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("[시스템 관리] 알림 리셋 실패", e);
            redirectAttributes.addFlashAttribute("error", "리셋 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }

    /**
     * 모든 알림 발송 상태 리셋 (테스트용)
     */
    @PostMapping("/reset-all-notifications")
    public String resetAllNotifications(RedirectAttributes redirectAttributes) {
        try {
            var all = notificationSubscriptionRepository.findAll();
            for (var entity : all) {
                entity.resetNotificationStatus();
            }
            notificationSubscriptionRepository.saveAll(all);
            redirectAttributes.addFlashAttribute("message", "모든 알림 상태가 리셋되었습니다. (" + all.size() + "건)");
        } catch (Exception e) {
            log.error("[시스템 관리] 전체 알림 리셋 실패", e);
            redirectAttributes.addFlashAttribute("error", "리셋 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }

    /**
     * 2025-2026 ApplyHome 청약 분양가 수집
     */
    @PostMapping("/collect-price-data")
    public String collectPriceData(RedirectAttributes redirectAttributes) {
        try {
            log.info("[시스템 관리] 분양가 데이터 수집 시작");

            // 2025-01-01 이후 ApplyHome 청약 조회
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            List<SubscriptionEntity> subscriptions = subscriptionRepository.findAll().stream()
                    .filter(s -> "ApplyHome".equals(s.getSource()))
                    .filter(s -> s.getHouseManageNo() != null && !s.getHouseManageNo().isEmpty())
                    .filter(s -> s.getReceiptStartDate() != null && !s.getReceiptStartDate().isBefore(startDate))
                    .filter(s -> !subscriptionPriceRepository.existsByHouseManageNo(s.getHouseManageNo()))
                    .toList();

            log.info("[시스템 관리] 분양가 수집 대상: {}건", subscriptions.size());

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            for (SubscriptionEntity subscription : subscriptions) {
                try {
                    applyhomeApiAdapter.fetchAndSavePriceDetails(
                            subscription.getHouseManageNo(),
                            subscription.getPblancNo(),
                            subscription.getHouseType()
                    );
                    successCount.incrementAndGet();

                    // API 과부하 방지
                    Thread.sleep(100);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.warn("[분양가] {} 수집 실패: {}", subscription.getHouseName(), e.getMessage());
                }
            }

            String message = String.format("분양가 수집 완료 - 성공: %d건, 실패: %d건", successCount.get(), failCount.get());
            log.info("[시스템 관리] {}", message);
            redirectAttributes.addFlashAttribute("message", message);

        } catch (Exception e) {
            log.error("[시스템 관리] 분양가 수집 실패", e);
            redirectAttributes.addFlashAttribute("error", "분양가 수집 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }
}
