package com.bank.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InterestSchedulerService {

    private static final Logger logger = Logger.getLogger(InterestSchedulerService.class);

    private final AccountService accountService;

    private InterestSchedulerService (AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * TEST SCHEDULER - Runs every 15 minutes during development
     */
    @Scheduled(every = "15m")
    public void testInterestJob() {
        logger.infof("[TEST] Running interest job every 15 minutes...");
        try {
            accountService.applyMonthlyInterestToAllSavings();
        } catch (Exception e) {
            logger.infof("Error in interest scheduler: " + e.getMessage());
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
