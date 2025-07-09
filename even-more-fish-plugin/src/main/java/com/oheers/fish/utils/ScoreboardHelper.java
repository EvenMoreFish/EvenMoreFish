package com.oheers.fish.utils;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

// Carried over from EMFPiñata as an easy way to handle glow colors.
public class ScoreboardHelper {

    private static final Map<NamedTextColor, Team> loadedTeams = new HashMap<>();

    private ScoreboardHelper() {}

    public static void addToTeam(@NotNull Entity entity, @Nullable String color) {
        if (color == null || color.isEmpty()) {
            return;
        }
        color = color.toLowerCase();
        NamedTextColor namedTextColor = NamedTextColor.NAMES.value(color);
        if (namedTextColor == null) {
            Logging.warn("Invalid piñata glow color: " + color + ". Not setting it.");
            return;
        }
        String teamName = "EMF_" + color;
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Team team = loadedTeams.computeIfAbsent(namedTextColor, key -> {
            Team existingTeam = manager.getMainScoreboard().getTeam(teamName);
            if (existingTeam != null) {
                return existingTeam;
            }
            Team newTeam = manager.getMainScoreboard().registerNewTeam(teamName);
            newTeam.color(namedTextColor);
            return newTeam;
        });
        team.addEntry(entity.getUniqueId().toString());
    }

}
