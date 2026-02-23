package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSubscriptionPriceEntity is a Querydsl query type for SubscriptionPriceEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscriptionPriceEntity extends EntityPathBase<SubscriptionPriceEntity> {

    private static final long serialVersionUID = -658878859L;

    public static final QSubscriptionPriceEntity subscriptionPriceEntity = new QSubscriptionPriceEntity("subscriptionPriceEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath houseManageNo = createString("houseManageNo");

    public final StringPath houseType = createString("houseType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath modelNo = createString("modelNo");

    public final StringPath pblancNo = createString("pblancNo");

    public final NumberPath<Long> pricePerPyeong = createNumber("pricePerPyeong", Long.class);

    public final NumberPath<Integer> specialSupplyCount = createNumber("specialSupplyCount", Integer.class);

    public final NumberPath<java.math.BigDecimal> supplyArea = createNumber("supplyArea", java.math.BigDecimal.class);

    public final NumberPath<Integer> supplyCount = createNumber("supplyCount", Integer.class);

    public final NumberPath<Long> topAmount = createNumber("topAmount", Long.class);

    public QSubscriptionPriceEntity(String variable) {
        super(SubscriptionPriceEntity.class, forVariable(variable));
    }

    public QSubscriptionPriceEntity(Path<? extends SubscriptionPriceEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSubscriptionPriceEntity(PathMetadata metadata) {
        super(SubscriptionPriceEntity.class, metadata);
    }

}

