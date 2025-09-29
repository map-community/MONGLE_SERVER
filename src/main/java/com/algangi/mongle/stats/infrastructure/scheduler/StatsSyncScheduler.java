package com.algangi.mongle.stats.infrastructure.scheduler;

import com.algangi.mongle.stats.application.service.StatsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsSyncScheduler {

    private final StatsSyncService statsSyncService;

    @Scheduled(cron = "0 */10 * * * *")
    @SchedulerLock(name = "syncStatsToRdb", lockAtLeastFor = "PT1M", lockAtMostFor = "PT5M")
    public void syncStatsToRdb() {
        log.info("ShedLock으로 보호된 통계 동기화 작업을 시작합니다.");

        statsSyncService.syncCounts("views::post::*", "post", "view_count");
        statsSyncService.syncCounts("comments::post::*", "post", "comment_count");
        statsSyncService.syncCounts("likes::post::*", "post", "like_count");
        statsSyncService.syncCounts("dislikes::post::*", "post", "dislike_count");
        statsSyncService.syncCounts("likes::comment::*", "comment", "like_count");
        statsSyncService.syncCounts("dislikes::comment::*", "comment", "dislike_count");

        log.info("통계 동기화 작업을 완료했습니다.");
    }
}