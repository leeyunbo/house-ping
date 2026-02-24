package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.support.dto.BlogContentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NaverHtmlBuilder - 네이버 HTML 생성")
class NaverHtmlBuilderTest {

    private NaverHtmlBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new NaverHtmlBuilder();
    }

    @Nested
    @DisplayName("build() - HTML 생성")
    class Build {

        @Test
        @DisplayName("카드 엔트리 없이 기본 HTML을 생성한다")
        void buildsBasicHtml() {
            // given
            BlogContentResult content = BlogContentResult.builder()
                    .title("주간 청약 TOP 5")
                    .blogText("블로그 본문")
                    .generatedDate(LocalDate.now())
                    .entries(List.of())
                    .build();

            // when
            String html = builder.build(content, 1L, "http://localhost");

            // then
            assertThat(html).contains("주간 청약 TOP 5");
            assertThat(html).contains("house-ping.com");
            assertThat(html).contains("하우스핑");
            assertThat(html).doesNotContain("<img");
        }

        @Test
        @DisplayName("카드 엔트리를 포함한 HTML을 생성한다")
        void buildsHtmlWithEntries() {
            // given
            BlogContentResult.BlogCardEntry entry = BlogContentResult.BlogCardEntry.createWithImage(
                    1L, "테스트아파트", 1, "[카드 이미지]\n내러티브 텍스트", new byte[]{1, 2, 3});
            BlogContentResult content = BlogContentResult.builder()
                    .title("주간 청약 TOP 3")
                    .blogText("블로그 본문")
                    .generatedDate(LocalDate.now())
                    .entries(List.of(entry))
                    .build();

            // when
            String html = builder.build(content, 10L, "http://localhost");

            // then
            assertThat(html).contains("테스트아파트");
            assertThat(html).contains("/home/blog/10/card/1.png");
            assertThat(html).contains("내러티브 텍스트");
            assertThat(html).doesNotContain("[카드 이미지]");
        }

        @Test
        @DisplayName("HTML 특수 문자를 이스케이프한다")
        void escapesHtmlSpecialChars() {
            // given
            BlogContentResult content = BlogContentResult.builder()
                    .title("제목 <script>alert('xss')</script>")
                    .blogText("본문")
                    .generatedDate(LocalDate.now())
                    .entries(List.of())
                    .build();

            // when
            String html = builder.build(content, 1L, "http://localhost");

            // then
            assertThat(html).doesNotContain("<script>");
            assertThat(html).contains("&lt;script&gt;");
        }
    }
}
