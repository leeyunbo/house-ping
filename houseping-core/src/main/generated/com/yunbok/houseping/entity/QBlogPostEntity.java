package com.yunbok.houseping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBlogPostEntity is a Querydsl query type for BlogPostEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBlogPostEntity extends EntityPathBase<BlogPostEntity> {

    private static final long serialVersionUID = 809727455L;

    public static final QBlogPostEntity blogPostEntity = new QBlogPostEntity("blogPostEntity");

    public final StringPath contentHtml = createString("contentHtml");

    public final StringPath contentText = createString("contentText");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> publishedAt = createDateTime("publishedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.yunbok.houseping.core.domain.BlogPostStatus> status = createEnum("status", com.yunbok.houseping.core.domain.BlogPostStatus.class);

    public final StringPath title = createString("title");

    public final NumberPath<Integer> topN = createNumber("topN", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> weekEndDate = createDate("weekEndDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> weekStartDate = createDate("weekStartDate", java.time.LocalDate.class);

    public QBlogPostEntity(String variable) {
        super(BlogPostEntity.class, forVariable(variable));
    }

    public QBlogPostEntity(Path<? extends BlogPostEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBlogPostEntity(PathMetadata metadata) {
        super(BlogPostEntity.class, metadata);
    }

}

