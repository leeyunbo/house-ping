package com.yunbok.houseping.adapter.persistence;

import com.yunbok.houseping.core.domain.BlogPost;
import com.yunbok.houseping.core.domain.BlogPostStatus;
import com.yunbok.houseping.entity.BlogCardImageEntity;
import com.yunbok.houseping.repository.BlogCardImageRepository;
import com.yunbok.houseping.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BlogPostQueryAdapter {

    private final BlogPostRepository blogPostRepository;
    private final BlogCardImageRepository blogCardImageRepository;

    public Optional<BlogPost> findById(Long id) {
        return blogPostRepository.findById(id)
                .map(e -> e.toDomain());
    }

    public List<BlogPost> findPublished() {
        return blogPostRepository.findByStatusOrderByPublishedAtDesc(BlogPostStatus.PUBLISHED)
                .stream()
                .map(e -> e.toDomain())
                .toList();
    }

    public List<BlogPost> findAll() {
        return blogPostRepository.findAll()
                .stream()
                .map(e -> e.toDomain())
                .toList();
    }

    public List<BlogCardImageEntity> findCardImages(Long postId) {
        return blogCardImageRepository.findByBlogPostIdOrderByRankAsc(postId);
    }

    public Optional<BlogCardImageEntity> findCardImage(Long postId, int rank) {
        return blogCardImageRepository.findByBlogPostIdAndRank(postId, rank);
    }
}
