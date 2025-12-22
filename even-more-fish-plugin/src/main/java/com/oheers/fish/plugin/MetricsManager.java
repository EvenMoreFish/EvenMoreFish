package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

public class MetricsManager {
    private final EvenMoreFish plugin;
    private int fishCaught = 0;
    private int baitsUsed = 0;
    private int baitsApplied = 0;

    public MetricsManager(EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    public void setupMetrics() {
        if (MainConfig.getInstance().shouldDebug()) {
            return;
        }
        Metrics metrics = new Metrics(plugin, 11054);

        metrics.addCustomChart(new SingleLineChart("fish_caught", this::getAndResetFishCaught));
        metrics.addCustomChart(new SingleLineChart("baits_applied", this::getAndResetBaitsApplied));
        metrics.addCustomChart(new SingleLineChart("baits_used", this::getAndResetBaitsUsed));
        metrics.addCustomChart(new SimplePie("database", () ->
                MainConfig.getInstance().databaseEnabled() ? "true" : "false"));
    }

    public void incrementFishCaught(int amount) {
        fishCaught += amount;
    }

    public void incrementBaitsUsed(int amount) {
        baitsUsed += amount;
    }

    public void incrementBaitsApplied(int amount) {
        baitsApplied += amount;
    }

    private int getAndResetFishCaught() {
        int value = fishCaught;
        fishCaught = 0;
        return value;
    }

    private int getAndResetBaitsUsed() {
        int value = baitsUsed;
        baitsUsed = 0;
        return value;
    }

    private int getAndResetBaitsApplied() {
        int value = baitsApplied;
        baitsApplied = 0;
        return value;
    }
}