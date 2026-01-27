package com.yunbok.houseping.adapter.in.web;

import com.yunbok.houseping.adapter.in.web.dto.ApiResponse;
import com.yunbok.houseping.domain.service.CompetitionRateCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 경쟁률 수집 컨트롤러
 */
@RestController
@RequestMapping("/api/competition-rate")
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "feature.subscription.applyhome-competition-enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class CompetitionRateController {

    private final CompetitionRateCollectorService collectorService;

    /**
     * 경쟁률 수집 수동 실행
     */
    @PostMapping("/collect")
    public ApiResponse<Map<String, Integer>> collect() {
        int count = collectorService.collect();
        return ApiResponse.success("Competition rate collection completed",
                Map.of("collectedCount", count));
    }
}
