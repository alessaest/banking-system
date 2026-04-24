package com.bank.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "deprecated_issue_deadline")
public class DeprecatedIssueDeadline extends PanacheEntityBase {
    @Id
    @Column(length = 200, nullable = false)
    public String issueKey;

    @Column(length = 200, nullable = false)
    public String projectKey;

    @Column(length = 100, nullable = false)
    public String rule;

    @Column(length = 20, nullable = false)
    public String severity;

    @Column(length = 1024, nullable = false)
    public String filePath;

    public Integer lineNo;

    @Column(length = 2048)
    public String message;

    @Column(length = 320)
    public String ownerEmail;

    @Column(nullable = false)
    public Instant firstSeenAt;

    @Column(nullable = false)
    public Instant lastSeenAt;

    @Column(nullable = false)
    public Instant dueAt;

    @Column(length = 20, nullable = false)
    public String status;

    public Instant resolvedAt;
    public Instant lastReminderSentAt;

    @Column(length = 30)
    public String lastReminderStage;

}
