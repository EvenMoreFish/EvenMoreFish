package com.oheers.fish.api;

import com.oheers.fish.api.plugin.EMFPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.TimeUnit;

@ApiStatus.Internal
public abstract class EMFTimer {

    private final TimeUnit unit;
    private final long interval;

    private ScheduledTask task;

    public EMFTimer(@NonNull TimeUnit unit, long interval) {
        this.unit = unit;
        this.interval = interval;
    }

    public void start() {
        if (task != null && !task.isCancelled()) {
            return;
        }
        // Use global region scheduler as this is a "main thread" job.
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(
            EMFPlugin.getInstance(),
            task -> run(),
            1,
            unit.toSeconds(interval) * 20
        );
    }

    public void stop() {
        if (task == null) {
            return;
        }
        if (!task.isCancelled()) {
            task.cancel();
        }
        task = null;
    }

    public abstract void run();

}
