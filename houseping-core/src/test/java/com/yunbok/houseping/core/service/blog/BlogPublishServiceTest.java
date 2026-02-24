package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.BlogPost;
import com.yunbok.houseping.core.domain.BlogPostStatus;
import com.yunbok.houseping.core.port.BlogPostPersistencePort;
import com.yunbok.houseping.entity.BlogCardImageEntity;
import com.yunbok.houseping.support.dto.BlogContentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("BlogPublishService - 블로그 발행 서비스")
@ExtendWith(MockitoExtension.class)
class BlogPublishServiceTest {

    @Mock
    private WeeklyBlogContentService weeklyBlogContentService;

    @Mock
    private AiBlogContentService aiBlogContentService;

    @Mock
    private BlogPostPersistencePort blogPostStore;

    @Mock
    private NaverHtmlBuilder naverHtmlBuilder;

    private BlogPublishService service;

    @BeforeEach
    void setUp() {
        service = new BlogPublishService(weeklyBlogContentService, aiBlogContentService, blogPostStore, naverHtmlBuilder);
    }

    @Nested
    @DisplayName("publishWeekly() - 주간 블로그 발행")
    class PublishWeekly {

        @Test
        @DisplayName("정상적으로 주간 블로그를 발행한다")
        void publishesWeeklyBlog() {
            // given
            BlogContentResult content = BlogContentResult.builder()
                    .title("주간 청약 TOP 5")
                    .blogText("블로그 본문")
                    .generatedDate(LocalDate.now())
                    .entries(List.of())
                    .build();
            BlogPost draft = createBlogPost(1L, "주간 청약 TOP 5", BlogPostStatus.DRAFT);
            BlogPost published = createBlogPost(1L, "주간 청약 TOP 5", BlogPostStatus.PUBLISHED);

            when(weeklyBlogContentService.generateWeeklyContent(5)).thenReturn(content);
            when(blogPostStore.saveDraft(content, 5)).thenReturn(draft);
            when(naverHtmlBuilder.build(content, 1L, "http://localhost")).thenReturn("<html>...</html>");
            when(blogPostStore.findById(1L)).thenReturn(Optional.of(published));

            // when
            BlogPost result = service.publishWeekly(5, "http://localhost");

            // then
            assertThat(result.getTitle()).isEqualTo("주간 청약 TOP 5");
            verify(blogPostStore).updateContentHtml(1L, "<html>...</html>");
        }

        @Test
        @DisplayName("저장 후 조회 실패 시 예외가 발생한다")
        void throwsWhenNotFoundAfterSave() {
            // given
            BlogContentResult content = BlogContentResult.builder()
                    .title("테스트").blogText("본문").generatedDate(LocalDate.now()).entries(List.of()).build();
            BlogPost draft = createBlogPost(1L, "테스트", BlogPostStatus.DRAFT);

            when(weeklyBlogContentService.generateWeeklyContent(5)).thenReturn(content);
            when(blogPostStore.saveDraft(content, 5)).thenReturn(draft);
            when(naverHtmlBuilder.build(any(), eq(1L), anyString())).thenReturn("<html/>");
            when(blogPostStore.findById(1L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.publishWeekly(5, "http://localhost"))
                    .isInstanceOf(java.util.NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("saveDraftWithAi() - AI 블로그 초안 저장")
    class SaveDraftWithAi {

        @Test
        @DisplayName("AI 콘텐츠를 생성하고 DRAFT로 저장한다")
        void savesAiDraft() {
            // given
            BlogContentResult content = BlogContentResult.builder()
                    .title("AI 블로그").blogText("AI 본문").generatedDate(LocalDate.now()).entries(List.of()).build();
            BlogPost draft = createBlogPost(1L, "AI 블로그", BlogPostStatus.DRAFT);

            when(aiBlogContentService.generateAiBlogContent(3)).thenReturn(content);
            when(blogPostStore.saveDraft(content, 3)).thenReturn(draft);

            // when
            BlogPost result = service.saveDraftWithAi(3);

            // then
            assertThat(result.getTitle()).isEqualTo("AI 블로그");
            assertThat(result.getStatus()).isEqualTo(BlogPostStatus.DRAFT);
        }
    }

    @Nested
    @DisplayName("findPublished() - 발행된 블로그 조회")
    class FindPublished {

        @Test
        @DisplayName("발행된 블로그 목록을 반환한다")
        void returnsPublished() {
            // given
            when(blogPostStore.findPublished()).thenReturn(List.of(
                    createBlogPost(1L, "블로그1", BlogPostStatus.PUBLISHED)
            ));

            // when
            List<BlogPost> result = service.findPublished();

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findAll() - 전체 블로그 조회")
    class FindAll {

        @Test
        @DisplayName("전체 블로그 목록을 반환한다")
        void returnsAll() {
            // given
            when(blogPostStore.findAll()).thenReturn(List.of(
                    createBlogPost(1L, "블로그1", BlogPostStatus.DRAFT),
                    createBlogPost(2L, "블로그2", BlogPostStatus.PUBLISHED)
            ));

            // when
            List<BlogPost> result = service.findAll();

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findById() - ID로 블로그 조회")
    class FindById {

        @Test
        @DisplayName("존재하는 블로그를 반환한다")
        void returnsBlogPost() {
            // given
            BlogPost post = createBlogPost(1L, "테스트 블로그", BlogPostStatus.PUBLISHED);
            when(blogPostStore.findById(1L)).thenReturn(Optional.of(post));

            // when
            BlogPost result = service.findById(1L);

            // then
            assertThat(result.getTitle()).isEqualTo("테스트 블로그");
        }

        @Test
        @DisplayName("존재하지 않는 블로그 조회 시 예외가 발생한다")
        void throwsWhenNotFound() {
            // given
            when(blogPostStore.findById(99L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.findById(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("포스트를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("findCardImage() - 카드 이미지 조회")
    class FindCardImage {

        @Test
        @DisplayName("존재하는 카드 이미지를 반환한다")
        void returnsCardImage() {
            // given
            BlogCardImageEntity entity = BlogCardImageEntity.builder()
                    .id(1L).blogPostId(1L).rank(1).houseName("테스트아파트").build();
            when(blogPostStore.findCardImage(1L, 1)).thenReturn(Optional.of(entity));

            // when
            BlogCardImageEntity result = service.findCardImage(1L, 1);

            // then
            assertThat(result.getHouseName()).isEqualTo("테스트아파트");
        }

        @Test
        @DisplayName("존재하지 않는 카드 이미지 조회 시 예외가 발생한다")
        void throwsWhenNotFound() {
            // given
            when(blogPostStore.findCardImage(99L, 1)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.findCardImage(99L, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("카드 이미지를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("unpublish() - 블로그 발행 취소")
    class Unpublish {

        @Test
        @DisplayName("블로그 발행을 취소한다")
        void unpublishesBlog() {
            // when
            service.unpublish(1L);

            // then
            verify(blogPostStore).unpublish(1L);
        }
    }

    @Nested
    @DisplayName("delete() - 블로그 삭제")
    class Delete {

        @Test
        @DisplayName("블로그를 삭제한다")
        void deletesBlog() {
            // when
            service.delete(1L);

            // then
            verify(blogPostStore).delete(1L);
        }
    }

    private BlogPost createBlogPost(Long id, String title, BlogPostStatus status) {
        return BlogPost.builder()
                .id(id)
                .title(title)
                .contentHtml("<p>내용</p>")
                .contentText("내용")
                .status(status)
                .topN(5)
                .weekStartDate(LocalDate.now())
                .weekEndDate(LocalDate.now().plusDays(6))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
