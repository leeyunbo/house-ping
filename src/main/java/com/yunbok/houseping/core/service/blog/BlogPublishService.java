package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.adapter.persistence.BlogPostStore;
import com.yunbok.houseping.core.domain.BlogPost;
import com.yunbok.houseping.entity.BlogCardImageEntity;
import com.yunbok.houseping.support.dto.BlogContentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogPublishService {

    private final WeeklyBlogContentService weeklyBlogContentService;
    private final AiBlogContentService aiBlogContentService;
    private final BlogPostStore blogPostStore;
    private final NaverHtmlBuilder naverHtmlBuilder;

    @Transactional
    public BlogPost publishWeekly(int topN, String baseUrl) {
        BlogContentResult content = weeklyBlogContentService.generateWeeklyContent(topN);
        BlogPost draft = blogPostStore.saveDraft(content, topN);

        String naverHtml = naverHtmlBuilder.build(content, draft.getId(), baseUrl);
        blogPostStore.updateContentHtml(draft.getId(), naverHtml);

        log.info("블로그 포스트 발행: id={}, title={}", draft.getId(), draft.getTitle());
        return blogPostStore.findById(draft.getId()).orElseThrow();
    }

    @Transactional
    public BlogPost saveDraftWithAi(int topN) {
        BlogContentResult content = aiBlogContentService.generateAiBlogContent(topN);
        BlogPost draft = blogPostStore.saveDraft(content, topN);
        log.info("[AI 블로그] DRAFT 저장 완료: id={}, title={}", draft.getId(), draft.getTitle());
        return draft;
    }

    public List<BlogPost> findPublished() {
        return blogPostStore.findPublished();
    }

    public List<BlogPost> findAll() {
        return blogPostStore.findAll();
    }

    public BlogPost findById(Long id) {
        return blogPostStore.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("포스트를 찾을 수 없습니다: " + id));
    }

    public List<BlogCardImageEntity> findCardImages(Long postId) {
        return blogPostStore.findCardImages(postId);
    }

    public BlogCardImageEntity findCardImage(Long postId, int rank) {
        return blogPostStore.findCardImage(postId, rank)
                .orElseThrow(() -> new IllegalArgumentException(
                        "카드 이미지를 찾을 수 없습니다: postId=" + postId + ", rank=" + rank));
    }

    @Transactional
    public void unpublish(Long id) {
        blogPostStore.unpublish(id);
    }

    @Transactional
    public void delete(Long id) {
        blogPostStore.delete(id);
    }

    public String getNaverHtml(Long postId) {
        BlogPost post = findById(postId);
        return post.getContentHtml();
    }
}
