package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.externalapi.formatter.SlackMessageFormatter;
import com.yunbok.houseping.externalapi.formatter.TelegramMessageFormatter;
import com.yunbok.houseping.core.port.NotificationSubscriptionPersistencePort;
import com.yunbok.houseping.core.service.notification.DailyNotificationService;
import com.yunbok.houseping.support.dto.DailyNotificationReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 알림 관리 컨트롤러 (MASTER 전용)
 */
@Slf4j
@Controller
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final DailyNotificationService dailyNotificationService;
    private final SlackMessageFormatter slackMessageFormatter;
    private final TelegramMessageFormatter telegramMessageFormatter;
    private final NotificationSubscriptionPersistencePort notificationSubscriptionPort;

    /**
     * 일일 알림 미리보기 (발송 안함)
     */
    @GetMapping("/preview-daily-report")
    public String previewDailyReport(Model model) {
        try {
            log.info("[admin.notification] 일일 알림 미리보기 요청");
            DailyNotificationReport report = dailyNotificationService.generateReport();

            String slackMessage = slackMessageFormatter.formatDailyReport(report);
            String telegramMessage = telegramMessageFormatter.formatDailyReport(report);

            model.addAttribute("report", report);
            model.addAttribute("slackMessage", slackMessage);
            model.addAttribute("telegramMessage", telegramMessage);

            log.info("[admin.notification] 미리보기 생성 완료 - 신규: {}건, 내일 접수: {}건, 오늘 마감: {}건",
                    report.newSubscriptions().size(),
                    report.receiptStartTomorrow().size(),
                    report.receiptEndToday().size());

        } catch (Exception e) {
            log.error("[admin.notification] 미리보기 생성 실패", e);
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
            log.info("[admin.notification] 일일 알림 수동 발송 시작");
            dailyNotificationService.sendDailyReportManual();
            log.info("[admin.notification] 일일 알림 수동 발송 완료");
            redirectAttributes.addFlashAttribute("message", "일일 알림이 발송되었습니다.");
        } catch (Exception e) {
            log.error("[admin.notification] 일일 알림 발송 실패", e);
            redirectAttributes.addFlashAttribute("error", "알림 발송 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }

    /**
     * 알림 발송 상태 리셋 (테스트용)
     */
    @Transactional
    @PostMapping("/reset-notification/{id}")
    public String resetNotification(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            notificationSubscriptionPort.resetNotificationStatus(id);
            redirectAttributes.addFlashAttribute("message", "알림 상태가 리셋되었습니다. (ID: " + id + ")");
        } catch (Exception e) {
            log.error("[admin.notification] 알림 리셋 실패", e);
            redirectAttributes.addFlashAttribute("error", "리셋 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }

    /**
     * 모든 알림 발송 상태 리셋 (테스트용)
     */
    @Transactional
    @PostMapping("/reset-all-notifications")
    public String resetAllNotifications(RedirectAttributes redirectAttributes) {
        try {
            int count = notificationSubscriptionPort.resetAllNotificationStatuses();
            redirectAttributes.addFlashAttribute("message", "모든 알림 상태가 리셋되었습니다. (" + count + "건)");
        } catch (Exception e) {
            log.error("[admin.notification] 전체 알림 리셋 실패", e);
            redirectAttributes.addFlashAttribute("error", "리셋 실패: " + e.getMessage());
        }
        return "redirect:/admin/system";
    }
}
