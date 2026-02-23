package com.yunbok.houseping.repository;

import com.yunbok.houseping.core.domain.BlogPostStatus;
import com.yunbok.houseping.entity.BlogPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPostEntity, Long> {

    List<BlogPostEntity> findByStatusOrderByPublishedAtDesc(BlogPostStatus status);

    Optional<BlogPostEntity> findByWeekStartDateAndWeekEndDate(LocalDate weekStartDate, LocalDate weekEndDate);
}
