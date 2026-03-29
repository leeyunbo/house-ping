package com.yunbok.houseping.persistence;

import com.yunbok.houseping.core.port.NotificationHistoryPersistencePort;
import com.yunbok.houseping.entity.NotificationHistoryEntity;
import com.yunbok.houseping.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationHistoryDbStore implements NotificationHistoryPersistencePort {

    private final NotificationHistoryRepository repository;

    @Override
    public void save(String notificationType, String channel, boolean success,
                     String summary, String detail, String errorMessage, String triggeredBy) {
        NotificationHistoryEntity entity = NotificationHistoryEntity.builder()
                .notificationType(notificationType)
                .channel(channel)
                .success(success)
                .summary(summary)
                .detail(detail)
                .errorMessage(errorMessage)
                .triggeredBy(triggeredBy)
                .build();
        repository.save(entity);
    }
}
