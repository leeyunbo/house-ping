package com.yunbok.houseping.core.port;

public interface NotificationHistoryPersistencePort {

    void save(String notificationType, String channel, boolean success,
              String summary, String detail, String errorMessage, String triggeredBy);
}
