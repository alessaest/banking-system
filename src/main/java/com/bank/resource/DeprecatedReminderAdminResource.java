package com.bank.resource;

import com.bank.config.DeprecatedReminderConfig;
import com.bank.entity.DeprecatedIssueDeadline;
import com.bank.repository.DeprecatedIssueDeadlineRepository;
import com.bank.service.DeprecatedCodeCollectorService;
import com.bank.service.DeprecatedDeadlineService;
import com.bank.service.DeprecatedReminderEmailService;
import com.bank.util.DeprecatedIssueRecord;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/admin/deprecated-reminder")
@Produces(MediaType.APPLICATION_JSON)
public class DeprecatedReminderAdminResource {

    private final DeprecatedCodeCollectorService collectorService;
    private final DeprecatedDeadlineService deadlineService;
    private final DeprecatedIssueDeadlineRepository repo;
    private final DeprecatedReminderEmailService emailService;
    private final DeprecatedReminderConfig reminderConfig;

    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String profile;

    @ConfigProperty(name = "sonar.url")
    String sonarUrl;

    @ConfigProperty(name = "sonar.organization")
    String organization;

    @ConfigProperty(name = "sonar.project-key")
    String projectKey;

    @ConfigProperty(name = "sonar.token")
    String sonarToken;

    @ConfigProperty(name = "sonar.rules", defaultValue = "")
    String rules;

    @ConfigProperty(name = "sonar.severities", defaultValue = "INFO")
    String severities;

    @ConfigProperty(name = "sonar.types", defaultValue = "CODE_SMELL")
    String types;

    public DeprecatedReminderAdminResource(
            DeprecatedCodeCollectorService collectorService,
            DeprecatedDeadlineService deadlineService,
            DeprecatedIssueDeadlineRepository repo,
            DeprecatedReminderEmailService emailService,
            DeprecatedReminderConfig reminderConfig
    ) {
        this.collectorService = collectorService;
        this.deadlineService = deadlineService;
        this.repo = repo;
        this.emailService = emailService;
        this.reminderConfig = reminderConfig;
    }

    @POST
    @Path("/sync-now")
    public Response syncNow() {
        guardNonDev();

        try {
            List<DeprecatedIssueRecord> openIssues = collectorService.fetchOpenForTracking(
                    sonarUrl, organization, projectKey, sonarToken, rules, severities, types
            );

            deadlineService.upsertFromOpenIssues(projectKey, openIssues);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("syncedOpenIssues", openIssues.size());
            out.put("projectKey", projectKey);
            out.put("rules", rules);
            out.put("severities", severities);
            out.put("types", types);
            return Response.ok(out).build();
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", e.getMessage());
            return Response.serverError().entity(err).build();
        }
    }

    @POST
    @Path("/send-reminders-now")
    public Response sendRemindersNow(
            @QueryParam("dryRun") boolean dryRun,
            @QueryParam("includeNotDue") boolean includeNotDue
    ) {
        guardNonDev();

        Instant now = Instant.now();
        List<DeprecatedIssueDeadline> candidates = includeNotDue
                ? repo.list("status in ('OPEN','OVERDUE')")
                : repo.findReminderCandidates(now);

        int sent = 0;
        for (DeprecatedIssueDeadline i : candidates) {
            String stage = computeStage(i, now, includeNotDue);
            if (stage == null) {
                continue;
            }

            if (!dryRun) {
                emailService.sendReminder(i, stage);
                i.lastReminderSentAt = now;
                i.lastReminderStage = stage;
            }
            sent++;
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("dryRun", dryRun);
        out.put("includeNotDue", includeNotDue);
        out.put("candidates", candidates.size());
        out.put("sentOrWouldSend", sent);
        return Response.ok(out).build();
    }

    @GET
    @Path("/open")
    public Response open() {
        guardNonDev();
        List<DeprecatedIssueDeadline> rows = repo.list("status in ('OPEN','OVERDUE')");
        return Response.ok(rows).build();
    }

    private void guardNonDev() {
        // Allow only dev/test for safety
        if (!"dev".equalsIgnoreCase(profile) && !"test".equalsIgnoreCase(profile)) {
            throw new IllegalStateException("Temporary admin endpoint is disabled outside dev/test profile");
        }
        if (!reminderConfig.enabled()) {
            throw new IllegalStateException("deprecated.reminder.enabled=false");
        }
    }

    private String computeStage(DeprecatedIssueDeadline i, Instant now, boolean includeNotDue) {
        long daysToDue = ChronoUnit.DAYS.between(
                now.truncatedTo(ChronoUnit.DAYS),
                i.dueAt.truncatedTo(ChronoUnit.DAYS)
        );

        if (daysToDue == 7) return "T_MINUS_7";
        if (daysToDue == 1) return "T_MINUS_1";
        if (daysToDue == 0) return "DUE";
        if (daysToDue < 0 && Math.abs(daysToDue) % 3 == 0) return "OVERDUE_" + Math.abs(daysToDue);

        // useful for forced testing
        if (includeNotDue) return "MANUAL_TEST";
        return null;
    }
}