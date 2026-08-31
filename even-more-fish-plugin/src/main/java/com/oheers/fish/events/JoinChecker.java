package com.oheers.fish.events;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.utils.Scheduling;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NonNull;

public class JoinChecker implements Listener {

    // Gives the player the active fishing bar if there's a fishing event cracking off
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        checkCompetitionJoin(event.getPlayer());
        checkRewardsJoin(event.getPlayer());
    }

    private void checkCompetitionJoin(@NonNull Player player) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null) {
            return;
        }

        activeComp.getStatusBar().addPlayer(player);
        EMFMessage message = activeComp.format(ConfigMessage.COMPETITION_JOIN);
        Scheduling.getInstance().runTaskLater(() -> message.send(player), 60L);
    }

    public void checkRewardsJoin(@NonNull Player player) {
        Scheduling.getInstance().runTaskLater(() -> Reward.checkCache(player.getUniqueId()), 60L);
    }

    // Removes the player from the bar list if they leave the server
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        checkCompetitionLeave(event.getPlayer());
    }

    private void checkCompetitionLeave(@NonNull Player player) {
        final Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp != null) {
            activeComp.getStatusBar().removePlayer(player);
        }

        if (!DatabaseUtil.isDatabaseOnline()) {
            return;
        }

        EvenMoreFish.getInstance().getPluginDataManager().getUserReportDataManager().flush();
        EvenMoreFish.getInstance().getPluginDataManager().getUserFishStatsDataManager().flush();
    }

}