package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationHistoryEntity is a Querydsl query type for NotificationHistoryEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationHistoryEntity extends EntityPathBase<NotificationHistoryEntity> {

    private static final long serialVersionUID = 183088690L;

    public static final QNotificationHistoryEntity notificationHistoryEntity = new QNotificationHistoryEntity("notificationHistoryEntity");

    public final StringPath channel = createString("channel");

    public final StringPath detail = createString("detail");

    public final StringPath errorMessage = createString("errorMessage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath notificationType = createString("notificationType");

    public final DateTimePath<java.time.LocalDateTime> sentAt = createDateTime("sentAt", java.time.LocalDateTime.class);

    public final BooleanPath success = createBoolean("success");

    public final StringPath summary = createString("summary");

    public final StringPath triggeredBy = createString("triggeredBy");

    public QNotificationHistoryEntity(String variable) {
        super(NotificationHistoryEntity.class, forVariable(variable));
    }

    public QNotificationHistoryEntity(Path<? extends NotificationHistoryEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationHistoryEntity(PathMetadata metadata) {
        super(NotificationHistoryEntity.class, metadata);
    }

}

