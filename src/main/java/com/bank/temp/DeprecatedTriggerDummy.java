package com.bank.temp;

public class DeprecatedTriggerDummy {

    /**
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    public void trigger() {
        Thread thread = new Thread(() -> {

        });
        thread.start();

        thread.stop();
    }
}
