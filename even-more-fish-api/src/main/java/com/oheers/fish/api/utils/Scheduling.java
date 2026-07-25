package com.oheers.fish.api.utils;

import com.oheers.fish.api.plugin.EMFPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Internal scheduling utility class.
 * <p>
 * While this likely won't change much, it is not recommended to use in external plugins.
 */
@ApiStatus.Internal
public class Scheduling {

    private static final Scheduling INSTANCE = new Scheduling();

    private static final EMFPlugin PLUGIN = EMFPlugin.getInstance();

    private Scheduling() {}

    public static @NotNull Scheduling getInstance() {
        return INSTANCE;
    }

    public @NonNull ScheduledTask runTask(@NonNull Runnable runnable) {
        return Bukkit.getGlobalRegionScheduler().run(
            PLUGIN,
            task -> runnable.run()
        );
    }

    public @NonNull ScheduledTask runTaskLater(@NonNull Runnable runnable, long delayTicks) {
        return Bukkit.getGlobalRegionScheduler().runDelayed(
            PLUGIN,
            task -> runnable.run(),
            calculateDelay(delayTicks)
        );
    }

    public @NonNull ScheduledTask runTaskTimer(@NonNull Runnable runnable, long delayTicks, long periodTicks) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(
            PLUGIN,
            task -> runnable.run(),
            calculateDelay(delayTicks),
            periodTicks
        );
    }

    public @NonNull ScheduledTask runTask(@NonNull Location location, @NonNull Runnable runnable) {
        return Bukkit.getRegionScheduler().run(
            PLUGIN,
            location,
            task -> runnable.run()
        );
    }

    public @NonNull ScheduledTask runTaskLater(@NonNull Location location, @NonNull Runnable runnable, long delayTicks) {
        return Bukkit.getRegionScheduler().runDelayed(
            PLUGIN,
            location,
            task -> runnable.run(),
            calculateDelay(delayTicks)
        );
    }

    public @NonNull ScheduledTask runTaskTimer(@NonNull Location location, @NonNull Runnable runnable, long delayTicks, long periodTicks) {
        return Bukkit.getRegionScheduler().runAtFixedRate(
            PLUGIN,
            location,
            task -> runnable.run(),
            calculateDelay(delayTicks),
            periodTicks
        );
    }

    public @Nullable ScheduledTask runTask(@NonNull Entity entity, @NonNull Runnable runnable) {
        return entity.getScheduler().run(
            PLUGIN,
            task -> runnable.run(),
            null
        );
    }

    public @Nullable ScheduledTask runTaskLater(@NonNull Entity entity, @NonNull Runnable runnable, long delayTicks) {
        return entity.getScheduler().runDelayed(
            PLUGIN,
            task -> runnable.run(),
            null,
            calculateDelay(delayTicks)
        );
    }

    public @Nullable ScheduledTask runTaskTimer(@NonNull Entity entity, @NonNull Runnable runnable, long delayTicks, long periodTicks) {
        return entity.getScheduler().runAtFixedRate(
            PLUGIN,
            task -> runnable.run(),
            null,
            calculateDelay(delayTicks),
            periodTicks
        );
    }

    private long calculateDelay(long initialDelay) {
        // Folia requires the initial delay to be 1 or above.
        if (PLUGIN.isRunningOnFolia()) {
            return Math.max(1, initialDelay);
        }
        return initialDelay;
    }

}
