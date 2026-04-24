package com.bank.service;

import com.bank.config.DeprecatedReminderConfig;
import com.bank.entity.DeprecatedIssueDeadline;
import com.bank.repository.DeprecatedIssueDeadlineRepository;
import com.bank.util.DeprecatedIssueRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@ApplicationScoped
public class DeprecatedDeadlineService {

    private final DeprecatedIssueDeadlineRepository repo;
    private final DeprecatedReminderConfig config;
    private final IssueOwnerResolver ownerResolver;

    public DeprecatedDeadlineService(DeprecatedIssueDeadlineRepository repo,
                                     DeprecatedReminderConfig config,
                                     IssueOwnerResolver ownerResolver) {
        this.repo = repo;
        this.config = config;
        this.ownerResolver = ownerResolver;
    }

    @Transactional
    public void upsertFromOpenIssues(String projectKey, List<DeprecatedIssueRecord> openIssues) {
        Instant now = Instant.now();
        Set<String> seen = new HashSet<>();

        for (DeprecatedIssueRecord i : openIssues) {
            String key = stableKey(i);
            seen.add(key);

            DeprecatedIssueDeadline row = repo.findByIssueKey(key).orElse(null);
            if (row == null) {
                row = new DeprecatedIssueDeadline();
                row.issueKey = key;
                row.projectKey = projectKey;
                row.rule = i.rule;
                row.severity = i.severity;
                row.filePath = i.file;
                row.lineNo = i.line;
                row.message = i.message;
                row.ownerEmail = ownerResolver.resolveOwnerEmail(i.file, config.defaultRecipients());
                row.firstSeenAt = now;
                row.lastSeenAt = now;
                row.dueAt = now.plus(slaDays(i.severity), ChronoUnit.DAYS);
                row.status = "OPEN";
                repo.persist(row);
            } else {
                row.lastSeenAt = now;
                if (!"OPEN".equals(row.status) && !"OVERDUE".equals(row.status)) {
                    row.status = "OPEN";
                    row.resolvedAt = null;
                }
            }
        }

        // Mark missing as resolved
        List<DeprecatedIssueDeadline> currentlyOpen = repo.findOpenByProject(projectKey);
        for (DeprecatedIssueDeadline row : currentlyOpen) {
            if (!seen.contains(row.issueKey)) {
                row.status = "RESOLVED";
                row.resolvedAt = now;
            }
        }

        // Update overdue status
        for (DeprecatedIssueDeadline row : repo.findOpenByProject(projectKey)) {
            if (row.dueAt.isBefore(now)) {
                row.status = "OVERDUE";
            }
        }
    }

    private int slaDays(String severity) {
        if (severity == null) return config.slaInfoDays();
        return switch (severity.toUpperCase(Locale.ROOT)) {
            case "CRITICAL", "BLOCKER" -> config.slaCriticalDays();
            case "MAJOR" -> config.slaMajorDays();
            case "MINOR" -> config.slaMinorDays();
            default -> config.slaInfoDays();
        };
    }

    private String stableKey(DeprecatedIssueRecord i) {
        // Prefer explicit Sonar issue key if present
        if (i.issueKey != null && !i.issueKey.isBlank()) return i.issueKey;
        return String.join("|",
                safe(i.file),
                String.valueOf(i.line == null ? -1 : i.line),
                safe(i.rule),
                safe(i.message));
    }

    private String safe(String s) { return s == null ? "" : s; }
}
