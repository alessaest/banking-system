package com.bank.temp;

public class DeprecatedTriggerDummy {

    @Deprecated(forRemoval = true)
    public void trigger() {
        Thread thread = new Thread(() -> {

        });
        thread.start();

        thread.stop();
    }
}
