package com.yunbok.houseping.controller.api;

import com.yunbok.houseping.support.dto.ApiResponse;
import com.yunbok.houseping.core.service.competition.CompetitionRateCollectorService;
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
        havingValue = "true"
)
public class CompetitionRateController {

    private final CompetitionRateCollectorService collectorUseCase;

    /**
     * 경쟁률 수집 수동 실행
     */
    @PostMapping("/collect")
    public ApiResponse<Map<String, Integer>> collect() {
        int count = collectorUseCase.collect();
        return ApiResponse.success("Competition rate collection completed",
                Map.of("collectedCount", count));
    }
}
