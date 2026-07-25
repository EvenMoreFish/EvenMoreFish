package com.oheers.fish.api;

import org.jspecify.annotations.NonNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public abstract class EMFTimer extends TimerTask {

    private final Timer timer = new Timer();
    private final TimeUnit unit;
    private final long interval;

    public EMFTimer(@NonNull TimeUnit unit, long interval) {
        this.unit = unit;
        this.interval = interval;
    }

    public void start() {
        timer.scheduleAtFixedRate(
            this,
            0,
            unit.toMillis(interval)
        );
    }

    public void stop() {
        cancel();
        timer.cancel();
    }

}
