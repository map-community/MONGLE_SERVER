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
    @SchedulerLock(name = "runScheduledStatsSync", lockAtLeastFor = "PT5M", lockAtMostFor = "PT15M")
    public void runScheduledStatsSync() {
        log.info("ShedLock으로 보호된 통계 동기화 작업을 시작합니다.");

        statsSyncService.syncPostCommentCountsToDb();
        statsSyncService.syncPostViewCountsToRedis();
        statsSyncService.syncReactionCountsToRedis();

        log.info("통계 동기화 작업을 완료했습니다.");
    }
}