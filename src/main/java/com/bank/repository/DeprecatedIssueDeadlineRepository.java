package com.bank.repository;

import com.bank.entity.DeprecatedIssueDeadline;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DeprecatedIssueDeadlineRepository implements PanacheRepository<DeprecatedIssueDeadline> {
    public Optional<DeprecatedIssueDeadline> findByIssueKey(String issueKey) {
        return find("issueKey", issueKey).firstResultOptional();
    }

    public List<DeprecatedIssueDeadline> findOpenByProject(String projectKey) {
        return list("projectKey = ?1 and status in ('OPEN', 'OVERDUE')", projectKey);
    }

    public List<DeprecatedIssueDeadline> findReminderCandidates(Instant now) {
        return list("status in ('OPEN','OVERDUE') and dueAt <= ?1", now);
    }
}
