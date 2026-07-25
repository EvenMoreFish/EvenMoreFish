package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class CommandRewardType extends RewardType {

    @Override
    public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
        String inputCommand = value.replace("{player}", player.getName());
        if (EvenMoreFish.getInstance().getDependencyManager().isUsingPAPI()) inputCommand = PlaceholderAPI.setPlaceholders(player, inputCommand);
        if (hookLocation != null) {
            final String worldName = hookLocation.getWorld() == null ? "N/A" : hookLocation.getWorld().getName();
            inputCommand = inputCommand
                    .replace("{x}", Double.toString(hookLocation.getX()))
                    .replace("{y}", Double.toString(hookLocation.getY()))
                    .replace("{z}", Double.toString(hookLocation.getZ()))
                    .replace("{world}", worldName);
        }

        // running the command
        final String finalCommand = inputCommand;
        EvenMoreFish.getScheduler().runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand));
    }

    @Override
    public @NonNull String getIdentifier() {
        return "COMMAND";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NonNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
