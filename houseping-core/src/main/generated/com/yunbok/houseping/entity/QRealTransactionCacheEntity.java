package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRealTransactionCacheEntity is a Querydsl query type for RealTransactionCacheEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRealTransactionCacheEntity extends EntityPathBase<RealTransactionCacheEntity> {

    private static final long serialVersionUID = -1442732929L;

    public static final QRealTransactionCacheEntity realTransactionCacheEntity = new QRealTransactionCacheEntity("realTransactionCacheEntity");

    public final StringPath aptName = createString("aptName");

    public final NumberPath<Integer> buildYear = createNumber("buildYear", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> cachedAt = createDateTime("cachedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> dealAmount = createNumber("dealAmount", Long.class);

    public final DatePath<java.time.LocalDate> dealDate = createDate("dealDate", java.time.LocalDate.class);

    public final NumberPath<Integer> dealDay = createNumber("dealDay", Integer.class);

    public final StringPath dealYmd = createString("dealYmd");

    public final NumberPath<java.math.BigDecimal> excluUseAr = createNumber("excluUseAr", java.math.BigDecimal.class);

    public final NumberPath<Integer> floor = createNumber("floor", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jibun = createString("jibun");

    public final StringPath lawdCd = createString("lawdCd");

    public final StringPath umdNm = createString("umdNm");

    public QRealTransactionCacheEntity(String variable) {
        super(RealTransactionCacheEntity.class, forVariable(variable));
    }

    public QRealTransactionCacheEntity(Path<? extends RealTransactionCacheEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRealTransactionCacheEntity(PathMetadata metadata) {
        super(RealTransactionCacheEntity.class, metadata);
    }

}

