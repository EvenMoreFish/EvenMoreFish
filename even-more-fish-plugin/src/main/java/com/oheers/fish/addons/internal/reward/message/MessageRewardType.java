package com.oheers.fish.addons.internal.reward.message;

import org.jetbrains.annotations.NotNull;
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
