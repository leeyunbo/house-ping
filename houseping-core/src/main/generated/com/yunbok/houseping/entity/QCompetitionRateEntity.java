package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCompetitionRateEntity is a Querydsl query type for CompetitionRateEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompetitionRateEntity extends EntityPathBase<CompetitionRateEntity> {

    private static final long serialVersionUID = -32680248L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCompetitionRateEntity competitionRateEntity = new QCompetitionRateEntity("competitionRateEntity");

    public final DateTimePath<java.time.LocalDateTime> collectedAt = createDateTime("collectedAt", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> competitionRate = createNumber("competitionRate", java.math.BigDecimal.class);

    public final StringPath houseManageNo = createString("houseManageNo");

    public final StringPath houseType = createString("houseType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath pblancNo = createString("pblancNo");

    public final NumberPath<Integer> rank = createNumber("rank", Integer.class);

    public final NumberPath<Integer> requestCount = createNumber("requestCount", Integer.class);

    public final StringPath residenceArea = createString("residenceArea");

    public final QSubscriptionEntity subscription;

    public final NumberPath<Integer> supplyCount = createNumber("supplyCount", Integer.class);

    public QCompetitionRateEntity(String variable) {
        this(CompetitionRateEntity.class, forVariable(variable), INITS);
    }

    public QCompetitionRateEntity(Path<? extends CompetitionRateEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCompetitionRateEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCompetitionRateEntity(PathMetadata metadata, PathInits inits) {
        this(CompetitionRateEntity.class, metadata, inits);
    }

    public QCompetitionRateEntity(Class<? extends CompetitionRateEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.subscription = inits.isInitialized("subscription") ? new QSubscriptionEntity(forProperty("subscription")) : null;
    }

}

