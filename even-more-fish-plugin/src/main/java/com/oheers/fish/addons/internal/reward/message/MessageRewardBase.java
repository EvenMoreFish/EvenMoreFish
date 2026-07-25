package com.oheers.fish.addons.internal.reward.message;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import uk.firedev.messagelib.message.ComponentMessage;
import uk.firedev.messagelib.message.MessageType;

/**
 * Base for all Message-based reward types.
 * Handles {@link #doReward(Player, String, String, Location)} and {@link #getPlugin()}
 */
abstract class MessageRewardBase extends RewardType {

    @Override
    public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
        ComponentMessage.componentMessage(value, getMessageType())
            .replace("{player}", player.name())
            .parsePlaceholderAPI(player)
            .send(player);
    }

    @Override
    public @NonNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

    public abstract @NonNull MessageType getMessageType();

}
