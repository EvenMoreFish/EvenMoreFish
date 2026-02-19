package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.bossbar.BossBar.bossBar;

public class Bar {

    BossBar bar;
    private boolean shouldShow = true;

    EMFMessage prefix;

    public Bar() {
        createBar();
    }

    public void setShouldShow(boolean shouldShow) {
        this.shouldShow = shouldShow;
    }

    public void timerUpdate(long timeLeft, long totalTime) {
        setTitle(timeLeft);
        setProgress(timeLeft, totalTime);
    }

    public void setProgress(long timeLeft, long totalTime) {
        float progress = (float) (timeLeft) / (float) (totalTime);

        if (progress < 0) {
            progress = 0.0F;
        } else if (progress > 1) {
            progress = 1.0F;
        }
        bar.progress(progress);
    }

    public void setPrefix(EMFMessage prefix, CompetitionType type) {
        prefix.setVariable("{type}", type.getBarPrefix());
        this.prefix = prefix;
    }

    public void setColour(BossBar.Color colour) {
        this.bar.color(colour);
    }

    public void setTitle(long timeLeft) {
        EMFMessage layoutMessage = ConfigMessage.BAR_LAYOUT.getMessage();
        layoutMessage.setVariable("{prefix}", prefix);
        layoutMessage.setVariable("{time-formatted}", FishUtils.timeFormat(timeLeft));
        layoutMessage.setVariable("{remaining}", ConfigMessage.BAR_REMAINING.getMessage());
        bar.name(layoutMessage.getComponentMessage());
    }

    public void show() {
        if (!shouldShow) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(this::addPlayer);
    }

    public void hide() {
        Bukkit.getOnlinePlayers().forEach(this::removePlayer);
    }

    public void createBar() {
        BossBar.Overlay barStyle = MainConfig.getInstance().getBarStyle();
        bar = bossBar(
            Component.text("EMF BossBar"),
            1.0F,
            BossBar.Color.WHITE,
            barStyle
        );
    }

    public void removeAllPlayers() {
        bar.viewers().forEach(viewer -> {
            if (viewer instanceof Audience audience) {
                bar.removeViewer(audience);
            }
        });
    }

    private void addAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(bar::addViewer);
    }

    public void addPlayer(@NotNull Player player) {
        if (!shouldShow || EvenMoreFish.getInstance().getToggle().isBossBarDisabled(player)) {
            return;
        }
        bar.addViewer(player);
    }

    public void removePlayer(@NotNull Player player) {
        bar.removeViewer(player);
    }

}
