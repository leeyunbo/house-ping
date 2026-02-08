package com.yunbok.houseping.controller.api;

import com.yunbok.houseping.support.dto.*;
import com.yunbok.houseping.adapter.dto.SubscriptionInfo;
import com.yunbok.houseping.core.service.subscription.SubscriptionManagementService;
import com.yunbok.houseping.core.service.subscription.SubscriptionService;
import com.yunbok.houseping.adapter.formatter.SubscriptionMessageFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionUseCase;
    private final SubscriptionManagementService managementUseCase;
    private final SubscriptionMessageFormatter messageFormatter;

    @PostMapping("/collect")
    public ApiResponse<CollectResponse> collect() {
        int count = subscriptionUseCase.collect(LocalDate.now(), true).size();
        return ApiResponse.success("Collection completed", new CollectResponse(count));
    }

    @GetMapping("/test")
    public ApiResponse<TestCollectResponse> test(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SubscriptionInfo> result = subscriptionUseCase.collect(date, false);
        return ApiResponse.success(new TestCollectResponse(
                date,
                result.size(),
                result.stream().map(SubscriptionDto::from).toList(),
                formatMessages(result)
        ));
    }

    @PostMapping("/sync/initial")
    public ApiResponse<SyncResponse> syncInitial() {
        return ApiResponse.success("Initial sync completed", SyncResponse.from(managementUseCase.sync()));
    }

    @PostMapping("/cleanup")
    public ApiResponse<CleanupResponse> cleanup() {
        return ApiResponse.success("Cleanup completed", new CleanupResponse(managementUseCase.cleanup()));
    }

    private List<String> formatMessages(List<SubscriptionInfo> subscriptions) {
        if (subscriptions.isEmpty()) {
            return List.of(messageFormatter.formatNoDataMessage());
        }
        List<String> messages = new java.util.ArrayList<>();
        messages.add(messageFormatter.formatBatchSummary(subscriptions));
        subscriptions.forEach(s -> messages.add(messageFormatter.formatSubscription(s)));
        return messages;
    }
}
