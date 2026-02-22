package com.yunbok.houseping.adapter.persistence;

import com.yunbok.houseping.core.domain.BlogPost;
import com.yunbok.houseping.core.domain.BlogPostStatus;
import com.yunbok.houseping.entity.BlogCardImageEntity;
import com.yunbok.houseping.entity.BlogPostEntity;
import com.yunbok.houseping.repository.BlogCardImageRepository;
import com.yunbok.houseping.repository.BlogPostRepository;
import com.yunbok.houseping.support.dto.BlogContentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Component
@RequiredArgsConstructor
public class BlogPostPersistenceAdapter {

    private final BlogPostRepository blogPostRepository;
    private final BlogCardImageRepository blogCardImageRepository;

    @Transactional
    public BlogPost saveDraft(BlogContentResult content, int topN) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        BlogPostEntity entity = blogPostRepository
                .findByWeekStartDateAndWeekEndDate(weekStart, weekEnd)
                .orElse(BlogPostEntity.builder()
                        .title(content.getTitle())
                        .weekStartDate(weekStart)
                        .weekEndDate(weekEnd)
                        .topN(topN)
                        .status(BlogPostStatus.DRAFT)
                        .build());

        entity.publish(null, content.getBlogText());
        BlogPostEntity saved = blogPostRepository.save(entity);

        // 카드 이미지 삭제 후 재저장
        blogCardImageRepository.deleteByBlogPostId(saved.getId());
        blogCardImageRepository.flush();

        for (BlogContentResult.BlogCardEntry entry : content.getEntries()) {
            blogCardImageRepository.save(BlogCardImageEntity.builder()
                    .blogPostId(saved.getId())
                    .rank(entry.getRank())
                    .houseName(entry.getHouseName())
                    .subscriptionId(entry.getSubscriptionId())
                    .narrativeText(entry.getNarrativeText())
                    .imageData(entry.getCardImage())
                    .build());
        }

        return saved.toDomain();
    }

    @Transactional
    public void updateContentHtml(Long postId, String contentHtml) {
        blogPostRepository.findById(postId).ifPresent(entity -> {
            entity.publish(contentHtml, entity.getContentText());
        });
    }

    @Transactional
    public void unpublish(Long id) {
        blogPostRepository.findById(id).ifPresent(BlogPostEntity::unpublish);
    }

    @Transactional
    public void delete(Long id) {
        blogCardImageRepository.deleteByBlogPostId(id);
        blogPostRepository.deleteById(id);
    }
}
