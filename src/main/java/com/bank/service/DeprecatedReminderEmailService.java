package com.bank.service;

import com.bank.entity.DeprecatedIssueDeadline;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.reactive.ReactiveMailer;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeprecatedReminderEmailService {

    private final ReactiveMailer reactiveMailer;

    public DeprecatedReminderEmailService(ReactiveMailer reactiveMailer) {
        this.reactiveMailer = reactiveMailer;
    }

    public void sendReminder(DeprecatedIssueDeadline issue, String stage) {

        String subject = "[Deprecated Reminder][" + stage + "] " + issue.projectKey + " - " + issue.rule;
        String body = """
                Deprecated code issue reminder
                
                Project: %s
                Rule: %s
                Severity: %s
                File: %s:%s
                Message: %s
                
                First seen: %s
                Due at: %s
                Current status: %s
                """.formatted(issue.projectKey, issue.rule, issue.severity, issue.filePath, issue.lineNo, issue.message, issue.firstSeenAt, issue.dueAt, issue.status);

        reactiveMailer.send(Mail.withText(issue.ownerEmail, subject, body))
                .await().indefinitely(); // force SMTP result/exception now
        System.out.println("MAIL_SEND_CONFIRMED to " + issue.ownerEmail);
    }
}
