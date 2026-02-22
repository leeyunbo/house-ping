package com.yunbok.houseping.repository;

import com.yunbok.houseping.entity.BlogCardImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogCardImageRepository extends JpaRepository<BlogCardImageEntity, Long> {

    List<BlogCardImageEntity> findByBlogPostIdOrderByRankAsc(Long blogPostId);

    Optional<BlogCardImageEntity> findByBlogPostIdAndRank(Long blogPostId, int rank);

    void deleteByBlogPostId(Long blogPostId);
}
