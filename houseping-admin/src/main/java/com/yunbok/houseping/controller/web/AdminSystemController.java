package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.entity.NotificationHistoryEntity;
import com.yunbok.houseping.repository.NotificationHistoryRepository;
import com.yunbok.houseping.repository.NotificationSubscriptionRepository;
import com.yunbok.houseping.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 시스템 관리 메인 페이지 컨트롤러 (MASTER 전용)
 * - 알림 관련: AdminNotificationController
 * - 데이터 수집 관련: AdminDataCollectionController
 */
@Slf4j
@Controller
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class AdminSystemController {

    private final NotificationSubscriptionRepository notificationSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;

    @GetMapping
    public String systemPage(Model model) {
        var allNotifications = notificationSubscriptionRepository.findAll();
        var pendingStart = notificationSubscriptionRepository.findPendingReceiptStartNotifications();
        var pendingEnd = notificationSubscriptionRepository.findPendingReceiptEndNotifications();

        var subscriptionIds = allNotifications.stream()
                .map(n -> n.getSubscriptionId())
                .toList();
        var subscriptions = subscriptionRepository.findAllById(subscriptionIds);
        var subscriptionMap = subscriptions.stream()
                .collect(Collectors.toMap(
                        s -> s.getId(),
                        s -> s
                ));

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

        List<NotificationHistoryEntity> history = notificationHistoryRepository.findTop50ByOrderBySentAtDesc();
        model.addAttribute("notificationHistory", history);

        return "admin/system/index";
    }

    public record NotificationDetailDto(
            Long id,
            Long subscriptionId,
            String houseName,
            String area,
            LocalDate receiptStartDate,
            LocalDate receiptEndDate,
            boolean enabled,
            boolean receiptStartNotified,
            boolean receiptEndNotified
    ) {}
}
