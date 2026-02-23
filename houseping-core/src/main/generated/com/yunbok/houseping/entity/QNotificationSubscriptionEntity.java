package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationSubscriptionEntity is a Querydsl query type for NotificationSubscriptionEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationSubscriptionEntity extends EntityPathBase<NotificationSubscriptionEntity> {

    private static final long serialVersionUID = 1456900005L;

    public static final QNotificationSubscriptionEntity notificationSubscriptionEntity = new QNotificationSubscriptionEntity("notificationSubscriptionEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath enabled = createBoolean("enabled");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath receiptEndNotified = createBoolean("receiptEndNotified");

    public final BooleanPath receiptStartNotified = createBoolean("receiptStartNotified");

    public final NumberPath<Long> subscriptionId = createNumber("subscriptionId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QNotificationSubscriptionEntity(String variable) {
        super(NotificationSubscriptionEntity.class, forVariable(variable));
    }

    public QNotificationSubscriptionEntity(Path<? extends NotificationSubscriptionEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationSubscriptionEntity(PathMetadata metadata) {
        super(NotificationSubscriptionEntity.class, metadata);
    }

}

