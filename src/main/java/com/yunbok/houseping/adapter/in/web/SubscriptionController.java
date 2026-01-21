package com.yunbok.houseping.adapter.in.web;

import com.yunbok.houseping.adapter.in.web.dto.*;
import com.yunbok.houseping.domain.model.SubscriptionInfo;
import com.yunbok.houseping.domain.port.in.SubscriptionManagementUseCase;
import com.yunbok.houseping.domain.port.in.SubscriptionUseCase;
import com.yunbok.houseping.domain.port.out.SubscriptionMessageFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionUseCase subscriptionUseCase;
    private final SubscriptionManagementUseCase managementUseCase;
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
