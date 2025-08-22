package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MessageRewardType extends RewardType {

    private final LegacyComponentSerializer legacyAmpersandSerializer = LegacyComponentSerializer.legacyAmpersand();

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        Component component = FishUtils.isLegacyString(value) ?
                legacyAmpersandSerializer.deserialize(value) :
                EMFSingleMessage.MINIMESSAGE.deserialize(value);
        EMFSingleMessage message = EMFSingleMessage.of(component);
        message.setPlayer(player);
        message.send(player);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "MESSAGE";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NotNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
