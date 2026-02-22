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
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BlogPostStore {

    private final BlogPostRepository blogPostRepository;
    private final BlogCardImageRepository blogCardImageRepository;

    // ── Command ──

    @Transactional
    public BlogPost saveDraft(BlogContentResult content, int topN) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        BlogPostEntity blogPost = blogPostRepository
                .findByWeekStartDateAndWeekEndDate(weekStart, weekEnd)
                .orElse(BlogPostEntity.createDraft(content.getTitle(), weekStart, weekEnd, topN));

        blogPost.publish(null, content.getBlogText());
        BlogPostEntity saved = blogPostRepository.save(blogPost);

        blogCardImageRepository.deleteByBlogPostId(saved.getId());
        blogCardImageRepository.flush(); // DELETE → INSERT 순서를 보장하기 위해 flush

        for (BlogContentResult.BlogCardEntry entry : content.getEntries()) {
            blogCardImageRepository.save(BlogCardImageEntity.from(saved.getId(), entry));
        }

        return saved.toDomain();
    }

    @Transactional
    public void updateContentHtml(Long postId, String contentHtml) {
        blogPostRepository.findById(postId).ifPresent(post -> {
            post.publish(contentHtml, post.getContentText());
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

    // ── Query ──

    public Optional<BlogPost> findById(Long id) {
        return blogPostRepository.findById(id)
                .map(BlogPostEntity::toDomain);
    }

    public List<BlogPost> findPublished() {
        return blogPostRepository.findByStatusOrderByPublishedAtDesc(BlogPostStatus.PUBLISHED)
                .stream()
                .map(BlogPostEntity::toDomain)
                .toList();
    }

    public List<BlogPost> findAll() {
        return blogPostRepository.findAll()
                .stream()
                .map(BlogPostEntity::toDomain)
                .toList();
    }

    public List<BlogCardImageEntity> findCardImages(Long postId) {
        return blogCardImageRepository.findByBlogPostIdOrderByRankAsc(postId);
    }

    public Optional<BlogCardImageEntity> findCardImage(Long postId, int rank) {
        return blogCardImageRepository.findByBlogPostIdAndRank(postId, rank);
    }
}
