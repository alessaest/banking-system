package com.bank.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "deprecated.reminder")
public interface DeprecatedReminderConfig {
    boolean enabled();
    int slaInfoDays();
    int slaMinorDays();
    int slaMajorDays();
    int slaCriticalDays();
    String syncCron();
    String emailCron();
    String defaultRecipients();
}