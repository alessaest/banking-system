package com.bank.temp;

public class DeprecatedTriggerDummy {
    public void trigger() {
        Thread thread = new Thread(() -> {

        });
        thread.start();

        thread.stop();
    }
}
