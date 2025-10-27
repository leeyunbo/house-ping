package com.yunbok.houseping.infrastructure.adapter.inbound;

import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.CollectSubscriptionUseCase;
import com.yunbok.houseping.domain.service.SubscriptionCollectorService;
import com.yunbok.houseping.domain.service.SubscriptionSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 청약 정보 수집을 위한 REST API (인바운드 어댑터)
 */
@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final CollectSubscriptionUseCase collectSubscriptionUseCase;
    private final SubscriptionCollectorService subscriptionCollectorService;
    private final SubscriptionSyncService subscriptionSyncService;

    /**
     * 수동으로 청약 정보 수집 실행
     */
    @PostMapping("/collect")
    public ResponseEntity<Map<String, Object>> collectSubscriptions() {
        try {
            log.info("🔧 [수동] 청약 정보 수집을 시작합니다.");
            var newSubscriptions = collectSubscriptionUseCase.collectAndNotifyTodaySubscriptions();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "청약 정보 수집이 완료되었습니다.",
                "newSubscriptionsCount", newSubscriptions.size()
            ));
        } catch (Exception e) {
            log.error("수동 실행 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "청약 정보 수집 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 테스트용: 특정 날짜의 청약 정보 수집 (Slack 발송 안함)
     *
     * @param date 조회할 날짜 (YYYY-MM-DD 형식, 예: 2025-10-26)
     * @return 청약 정보와 Slack 메시지 미리보기
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testCollectSubscriptions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.info("🧪 [테스트] {} 날짜의 청약 정보 수집을 시작합니다.", date);

            List<SubscriptionInfo> subscriptions = subscriptionCollectorService.collectSubscriptionsForDate(date);

            // Slack 메시지 미리보기 생성
            List<String> slackMessages = generateSlackMessagePreview(subscriptions);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "date", date.toString(),
                "subscriptionsCount", subscriptions.size(),
                "subscriptions", subscriptions,
                "slackMessagePreview", slackMessages
            ));
        } catch (Exception e) {
            log.error("테스트 실행 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "청약 정보 수집 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 초기 데이터 동기화 (향후 12개월 데이터 로드)
     */
    @PostMapping("/sync/initial")
    public ResponseEntity<Map<String, Object>> syncInitialData() {
        try {
            log.info("🔧 [수동] 초기 데이터 동기화를 시작합니다.");
            SubscriptionSyncService.SyncResult result = subscriptionSyncService.syncInitialData();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "초기 데이터 동기화가 완료되었습니다.",
                "inserted", result.inserted,
                "updated", result.updated,
                "skipped", result.skipped,
                "total", result.total()
            ));
        } catch (Exception e) {
            log.error("초기 데이터 동기화 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "초기 데이터 동기화 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 오래된 데이터 정리
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldData() {
        try {
            log.info("🔧 [수동] 오래된 데이터 정리를 시작합니다.");
            int deletedCount = subscriptionSyncService.cleanupOldData();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "오래된 데이터 정리가 완료되었습니다.",
                "deletedCount", deletedCount
            ));
        } catch (Exception e) {
            log.error("오래된 데이터 정리 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "오래된 데이터 정리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * Slack으로 발송될 메시지 미리보기 생성
     */
    private List<String> generateSlackMessagePreview(List<SubscriptionInfo> subscriptions) {
        List<String> messages = new ArrayList<>();

        if (subscriptions.isEmpty()) {
            messages.add("📭 오늘은 신규 청약 정보가 없습니다.");
            return messages;
        }

        // 1. 요약 메시지
        StringBuilder summary = new StringBuilder();
        summary.append(":tada: *오늘의 신규 청약 정보 ")
               .append(subscriptions.size())
               .append("개*\n\n");

        for (int i = 0; i < subscriptions.size(); i++) {
            SubscriptionInfo sub = subscriptions.get(i);
            summary.append(i + 1)
                   .append(". ")
                   .append(sub.getSimpleDisplayMessage());
        }
        messages.add(summary.toString());

        // 2. 개별 상세 메시지
        for (SubscriptionInfo subscription : subscriptions) {
            messages.add(subscription.getDisplayMessage());
        }

        return messages;
    }
}
