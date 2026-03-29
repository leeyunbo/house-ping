package com.yunbok.houseping.core.formatter;

public interface SchedulerErrorFormatter {

    String formatSchedulerError(String schedulerName, String timestamp, String errorMessage, String stackTrace);

    String formatSchedulerErrorFallback(String schedulerName, String timestamp, String errorMessage);
}
