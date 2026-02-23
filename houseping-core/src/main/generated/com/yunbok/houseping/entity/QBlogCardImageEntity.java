package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBlogCardImageEntity is a Querydsl query type for BlogCardImageEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBlogCardImageEntity extends EntityPathBase<BlogCardImageEntity> {

    private static final long serialVersionUID = 1942047794L;

    public static final QBlogCardImageEntity blogCardImageEntity = new QBlogCardImageEntity("blogCardImageEntity");

    public final NumberPath<Long> blogPostId = createNumber("blogPostId", Long.class);

    public final StringPath houseName = createString("houseName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ArrayPath<byte[], Byte> imageData = createArray("imageData", byte[].class);

    public final StringPath narrativeText = createString("narrativeText");

    public final NumberPath<Integer> rank = createNumber("rank", Integer.class);

    public final NumberPath<Long> subscriptionId = createNumber("subscriptionId", Long.class);

    public QBlogCardImageEntity(String variable) {
        super(BlogCardImageEntity.class, forVariable(variable));
    }

    public QBlogCardImageEntity(Path<? extends BlogCardImageEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBlogCardImageEntity(PathMetadata metadata) {
        super(BlogCardImageEntity.class, metadata);
    }

}

