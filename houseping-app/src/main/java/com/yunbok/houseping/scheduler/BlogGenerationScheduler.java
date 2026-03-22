package com.yunbok.houseping.scheduler;

import com.yunbok.houseping.core.service.blog.BlogPublishService;
import com.yunbok.houseping.support.annotation.SchedulerMonitor;
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
        havingValue = "true"
)
public class BlogGenerationScheduler {

    private final BlogPublishService blogPublishService;

    @SchedulerMonitor("AI 주간 블로그 생성")
    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Seoul")
    public void generateWeeklyBlog() {
        blogPublishService.saveDraftWithAi(5);
    }
}
