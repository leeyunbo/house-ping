package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSubscriptionEntity is a Querydsl query type for SubscriptionEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscriptionEntity extends EntityPathBase<SubscriptionEntity> {

    private static final long serialVersionUID = 208875098L;

    public static final QSubscriptionEntity subscriptionEntity = new QSubscriptionEntity("subscriptionEntity");

    public final StringPath address = createString("address");

    public final DatePath<java.time.LocalDate> announceDate = createDate("announceDate", java.time.LocalDate.class);

    public final StringPath area = createString("area");

    public final DateTimePath<java.time.LocalDateTime> collectedAt = createDateTime("collectedAt", java.time.LocalDateTime.class);

    public final StringPath contact = createString("contact");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath detailUrl = createString("detailUrl");

    public final StringPath homepageUrl = createString("homepageUrl");

    public final StringPath houseManageNo = createString("houseManageNo");

    public final StringPath houseName = createString("houseName");

    public final StringPath houseType = createString("houseType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath pblancNo = createString("pblancNo");

    public final DatePath<java.time.LocalDate> receiptEndDate = createDate("receiptEndDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> receiptStartDate = createDate("receiptStartDate", java.time.LocalDate.class);

    public final StringPath source = createString("source");

    public final NumberPath<Integer> totalSupplyCount = createNumber("totalSupplyCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> winnerAnnounceDate = createDate("winnerAnnounceDate", java.time.LocalDate.class);

    public final StringPath zipCode = createString("zipCode");

    public QSubscriptionEntity(String variable) {
        super(SubscriptionEntity.class, forVariable(variable));
    }

    public QSubscriptionEntity(Path<? extends SubscriptionEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSubscriptionEntity(PathMetadata metadata) {
        super(SubscriptionEntity.class, metadata);
    }

}

