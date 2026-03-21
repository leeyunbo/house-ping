package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QApartmentLocationEntity is a Querydsl query type for ApartmentLocationEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApartmentLocationEntity extends EntityPathBase<ApartmentLocationEntity> {

    private static final long serialVersionUID = 1941501200L;

    public static final QApartmentLocationEntity apartmentLocationEntity = new QApartmentLocationEntity("apartmentLocationEntity");

    public final StringPath aptName = createString("aptName");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath dongName = createString("dongName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jibun = createString("jibun");

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final StringPath lawdCd = createString("lawdCd");

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public QApartmentLocationEntity(String variable) {
        super(ApartmentLocationEntity.class, forVariable(variable));
    }

    public QApartmentLocationEntity(Path<? extends ApartmentLocationEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QApartmentLocationEntity(PathMetadata metadata) {
        super(ApartmentLocationEntity.class, metadata);
    }

}

