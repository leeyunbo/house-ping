package com.yunbok.houseping.core.port;

import com.yunbok.houseping.core.domain.BlogPost;
import com.yunbok.houseping.entity.BlogCardImageEntity;
import com.yunbok.houseping.support.dto.BlogContentResult;

import java.util.List;
import java.util.Optional;

public interface BlogPostPersistencePort {

    BlogPost saveDraft(BlogContentResult content, int topN);

    void updateContentHtml(Long postId, String contentHtml);

    void unpublish(Long id);

    void delete(Long id);

    Optional<BlogPost> findById(Long id);

    List<BlogPost> findPublished();

    List<BlogPost> findAll();

    Optional<BlogCardImageEntity> findCardImage(Long postId, int rank);
}
