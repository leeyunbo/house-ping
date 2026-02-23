package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCompetitionRateEntity is a Querydsl query type for CompetitionRateEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompetitionRateEntity extends EntityPathBase<CompetitionRateEntity> {

    private static final long serialVersionUID = -32680248L;

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

    public final NumberPath<Integer> supplyCount = createNumber("supplyCount", Integer.class);

    public QCompetitionRateEntity(String variable) {
        super(CompetitionRateEntity.class, forVariable(variable));
    }

    public QCompetitionRateEntity(Path<? extends CompetitionRateEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCompetitionRateEntity(PathMetadata metadata) {
        super(CompetitionRateEntity.class, metadata);
    }

}

