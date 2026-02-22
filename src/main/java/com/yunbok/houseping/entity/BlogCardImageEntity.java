package com.yunbok.houseping.entity;

import com.yunbok.houseping.support.dto.BlogContentResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blog_card_image",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_blog_card_post_rank",
           columnNames = {"blog_post_id", "card_rank"}
       ))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogCardImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blog_post_id", nullable = false)
    private Long blogPostId;

    @Column(name = "card_rank", nullable = false)
    private int rank;

    @Column(name = "house_name", length = 200)
    private String houseName;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "narrative_text", columnDefinition = "TEXT")
    private String narrativeText;

    @Lob
    @Column(name = "image_data")
    private byte[] imageData;

    public static BlogCardImageEntity from(Long blogPostId, BlogContentResult.BlogCardEntry entry) {
        return BlogCardImageEntity.builder()
                .blogPostId(blogPostId)
                .rank(entry.getRank())
                .houseName(entry.getHouseName())
                .subscriptionId(entry.getSubscriptionId())
                .narrativeText(entry.getNarrativeText())
                .imageData(entry.getCardImage())
                .build();
    }
}
