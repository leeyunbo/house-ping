package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.blog.BlogPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "feature.blog.ai-generation-enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class BlogGenerationScheduler {

    private final BlogPublishService blogPublishService;

    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Seoul")
    public void generateWeeklyBlog() {
        log.info("[블로그 스케줄러] AI 블로그 DRAFT 생성 시작");
        try {
            blogPublishService.saveDraftWithAi(5);
            log.info("[블로그 스케줄러] AI 블로그 DRAFT 생성 완료");
        } catch (Exception e) {
            log.error("[블로그 스케줄러] AI 블로그 DRAFT 생성 실패", e);
        }
    }
}
