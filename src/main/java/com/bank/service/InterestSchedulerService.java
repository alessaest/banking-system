package com.bank.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InterestSchedulerService {

    @Inject
    AccountService accountService;

    /**
     * TEST SCHEDULER - Runs every 15 minutes during development
     */
    @Scheduled(every = "15m")
    public void testInterestJob() {
        System.out.println("[TEST] Running interest job every 15 minutes...");
        try {
            accountService.applyMonthlyInterestToAllSavings();
        } catch (Exception e) {
            System.err.println("Error in interest scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //cron format
    // @Scheduled(cron = "0 0 2 1 * ?")
    // public void monthlyInterestJob() {
    //     System.out.println("Monthly Interest Job Started at 2:00 AM on the 1st of the month...");
    //     try {
    //         accountService.applyMonthlyInterestToAllSavings();
    //         System.out.println("✓ Monthly Interest Job Completed");
    //     } catch (Exception e) {
    //         System.err.println("Error in monthly interest job: " + e.getMessage());
    //         e.printStackTrace();
    //     }
    // }
}
