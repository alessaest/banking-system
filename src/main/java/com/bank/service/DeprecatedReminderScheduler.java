package com.bank.service;

import com.bank.config.DeprecatedReminderConfig;
import com.bank.entity.DeprecatedIssueDeadline;
import com.bank.repository.DeprecatedIssueDeadlineRepository;
import com.bank.util.DeprecatedIssueRecord;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class DeprecatedReminderScheduler {

    private final DeprecatedReminderConfig config;
    private final DeprecatedCodeCollectorService collectorService;
    private final DeprecatedDeadlineService deadlineService;
    private final DeprecatedIssueDeadlineRepository repo;
    private final DeprecatedReminderEmailService emailService;

    @ConfigProperty(name = "sonar.url") String sonarUrl;
    @ConfigProperty(name = "sonar.organization") String organization;
    @ConfigProperty(name = "sonar.project-key") String projectKey;
    @ConfigProperty(name = "sonar.token") String sonarToken;
    @ConfigProperty(name = "sonar.rules", defaultValue = "java:S1874") String rules;
    @ConfigProperty(name = "sonar.severities", defaultValue = "INFO") String severities;
    @ConfigProperty(name = "sonar.types", defaultValue = "CODE_SMELL") String types;

    public DeprecatedReminderScheduler(DeprecatedReminderConfig config,
                                       DeprecatedCodeCollectorService collectorService,
                                       DeprecatedDeadlineService deadlineService,
                                       DeprecatedIssueDeadlineRepository repo,
                                       DeprecatedReminderEmailService emailService) {
        this.config = config;
        this.collectorService = collectorService;
        this.deadlineService = deadlineService;
        this.repo = repo;
        this.emailService = emailService;
    }

    @Scheduled(cron = "{deprecated.reminder.sync-cron}")
    @Transactional
    public void syncOpenIssues() throws Exception {
        if (!config.enabled()) return;

        List<DeprecatedIssueRecord> openIssues = collectorService.fetchOpenForTracking(sonarUrl, organization, projectKey, sonarToken, rules, severities, types);

        deadlineService.upsertFromOpenIssues(projectKey, openIssues);
    }

    @Scheduled(cron = "{deprecated.reminder.email-cron}")
    @Transactional
    public void sendReminders() {
        if (!config.enabled()) return;

        Instant now = Instant.now();
        for (DeprecatedIssueDeadline i : repo.findReminderCandidates(now)) {
            String stage = computeStage(i, now);
            if (stage == null) continue;
            if (stage.equals(i.lastReminderStage)) continue;

            emailService.sendReminder(i, stage);
            i.lastReminderStage = stage;
            i.lastReminderSentAt = now;
        }
    }

    private String computeStage(DeprecatedIssueDeadline i, Instant now) {
        long daysToDue = ChronoUnit.DAYS.between(now.truncatedTo(ChronoUnit.DAYS), i.dueAt.truncatedTo(ChronoUnit.DAYS));
        if (daysToDue == 7) return "T_MINUS_7";
        if (daysToDue == 1) return "T_MINUS_1";
        if (daysToDue == 0) return "T_MINUS_0";
        if (daysToDue < 0 && Math.abs(daysToDue) % 3 == 0) return "OVERDUE_" + Math.abs((daysToDue));
        return null;
    }
}
