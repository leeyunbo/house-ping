package com.yunbok.houseping.core.port;

/**
 * 스케줄러 에러/경고 알림 Port
 */
public interface SchedulerNotifier {

    void sendError(String taskName, Exception e);

    void sendWarning(String context, String message);
}
