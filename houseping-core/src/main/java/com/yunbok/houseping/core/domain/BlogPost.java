package com.yunbok.houseping.core.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class BlogPost {
    private final Long id;
    private final String title;
    private final String contentHtml;
    private final String contentText;
    private final LocalDate weekStartDate;
    private final LocalDate weekEndDate;
    private final BlogPostStatus status;
    private final int topN;
    private final LocalDateTime publishedAt;
    private final LocalDateTime createdAt;
}
