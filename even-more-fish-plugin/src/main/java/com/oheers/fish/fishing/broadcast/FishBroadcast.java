package com.oheers.fish.fishing.broadcast;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public record FishBroadcast(@NotNull EMFMessage message, @NotNull Player player, @NotNull Fish fish) {

    public void broadcast() {
        if (message.isEmpty()) {
            return;
        }
        Stream<? extends Player> players = Bukkit.getOnlinePlayers().stream();
        // Process rarity settings.
        players = filterRarity(players);
        // Process competition settings.
        players = filterCompetition(players);
        players.forEach(message::send);
    }

    private Stream<? extends Player> filterRarity(@NotNull Stream<? extends Player> players) {
        Rarity rarity = fish.getRarity();
        if (rarity.getBroadcastOnlyRods()) {
            players = players.filter(this::isHoldingRod);
        }
        int rangeSquared = rarity.getBroadcastRange();
        if (rangeSquared > 0) {
            players = players.filter(player -> isWithinRange(this.player, player, rangeSquared));
        }
        return players;
    }

    private Stream<? extends Player> filterCompetition(@NotNull Stream<? extends Player> players) {
        Competition active = Competition.getCurrentlyActive();
        if (active == null) {
            return players;
        }
        CompetitionFile file = active.getCompetitionFile();
        // Avoid processing again if we already know the player is holding a rod.
        if (!fish.getRarity().getBroadcastOnlyRods() && file.shouldBroadcastOnlyRods()) {
            players = players.filter(this::isHoldingRod);
        }
        int rangeSquared = file.getBroadcastRange();
        if (rangeSquared > 0) {
            players = players.filter(player -> isWithinRange(this.player, player, rangeSquared));
        }
        return players;
    }

    private boolean isHoldingRod(@NotNull Player player) {
        Material rodMaterial = Material.FISHING_ROD;
        return player.getInventory().getItemInMainHand().getType().equals(rodMaterial)
            || player.getInventory().getItemInOffHand().getType().equals(rodMaterial);
    }

    private boolean isWithinRange(@NotNull Player sourcePlayer, @NotNull Player targetPlayer, int rangeSquared) {
        return sourcePlayer.getWorld().equals(targetPlayer.getWorld())
            && sourcePlayer.getLocation().distanceSquared(targetPlayer.getLocation()) <= rangeSquared;
    }

}
