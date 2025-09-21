package com.yunbok.houseping.infrastructure.adapter.inbound;

import com.yunbok.houseping.domain.port.CollectSubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
