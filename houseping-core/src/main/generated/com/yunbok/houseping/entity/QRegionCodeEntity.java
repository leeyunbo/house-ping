package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRegionCodeEntity is a Querydsl query type for RegionCodeEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRegionCodeEntity extends EntityPathBase<RegionCodeEntity> {

    private static final long serialVersionUID = -1372258370L;

    public static final QRegionCodeEntity regionCodeEntity = new QRegionCodeEntity("regionCodeEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lawdCd = createString("lawdCd");

    public final StringPath regionName = createString("regionName");

    public final StringPath sidoName = createString("sidoName");

    public final StringPath sigunguName = createString("sigunguName");

    public QRegionCodeEntity(String variable) {
        super(RegionCodeEntity.class, forVariable(variable));
    }

    public QRegionCodeEntity(Path<? extends RegionCodeEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRegionCodeEntity(PathMetadata metadata) {
        super(RegionCodeEntity.class, metadata);
    }

}

