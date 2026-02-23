package com.yunbok.houseping.entity;

import com.yunbok.houseping.core.domain.BlogPost;
import com.yunbok.houseping.core.domain.BlogPostStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blog_post",
       indexes = {
           @Index(name = "idx_blog_post_status", columnList = "status"),
           @Index(name = "idx_blog_post_published_at", columnList = "published_at")
       })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BlogPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BlogPostStatus status;

    @Column(name = "top_n", nullable = false)
    private int topN;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BlogPost toDomain() {
        return BlogPost.builder()
                .id(id)
                .title(title)
                .contentHtml(contentHtml)
                .contentText(contentText)
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .status(status)
                .topN(topN)
                .publishedAt(publishedAt)
                .createdAt(createdAt)
                .build();
    }

    public void publish(String contentHtml, String contentText) {
        this.contentHtml = contentHtml;
        this.contentText = contentText;
        this.status = BlogPostStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void unpublish() {
        this.status = BlogPostStatus.DRAFT;
    }

    public static BlogPostEntity createDraft(String title, LocalDate weekStart, LocalDate weekEnd, int topN) {
        return BlogPostEntity.builder()
                .title(title)
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .topN(topN)
                .status(BlogPostStatus.DRAFT)
                .build();
    }
}
