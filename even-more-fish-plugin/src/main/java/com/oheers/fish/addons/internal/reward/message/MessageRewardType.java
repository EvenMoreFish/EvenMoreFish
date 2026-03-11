package com.oheers.fish.addons.internal.reward.message;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import uk.firedev.messagelib.message.ComponentMessage;
import uk.firedev.messagelib.message.MessageType;

public class MessageRewardType extends MessageRewardBase {

    @Override
    public @NotNull String getIdentifier() {
        return "MESSAGE";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NotNull MessageType getMessageType() {
        return MessageType.CHAT;
    }

}
