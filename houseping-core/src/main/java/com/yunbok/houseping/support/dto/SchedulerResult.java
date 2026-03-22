package com.yunbok.houseping.support.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SchedulerResult(
        int successCount,
        int failCount,
        List<String> failureDetails
) {

    public SchedulerResult {
        failureDetails = failureDetails != null
                ? Collections.unmodifiableList(failureDetails)
                : List.of();
    }

    public boolean hasFailed() {
        return failCount > 0;
    }

    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("성공: ").append(successCount).append("건");
        if (failCount > 0) {
            sb.append(", 실패: ").append(failCount).append("건");
        }
        if (!failureDetails.isEmpty()) {
            sb.append("\n").append(String.join("\n", failureDetails));
        }
        return sb.toString();
    }

    public static SchedulerResult success(int count) {
        return new SchedulerResult(count, 0, List.of());
    }

    public static SchedulerResult of(int successCount, int failCount) {
        return new SchedulerResult(successCount, failCount, List.of());
    }

    public static SchedulerResult of(int successCount, int failCount, List<String> failureDetails) {
        return new SchedulerResult(successCount, failCount, failureDetails);
    }

    public SchedulerResult merge(SchedulerResult other) {
        List<String> merged = new ArrayList<>(this.failureDetails);
        merged.addAll(other.failureDetails);
        return new SchedulerResult(
                this.successCount + other.successCount,
                this.failCount + other.failCount,
                merged
        );
    }
}
