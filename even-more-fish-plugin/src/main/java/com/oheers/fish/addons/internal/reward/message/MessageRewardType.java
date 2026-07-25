package com.oheers.fish.addons.internal.reward.message;

import org.jspecify.annotations.NonNull;
import uk.firedev.messagelib.message.MessageType;

public class MessageRewardType extends MessageRewardBase {

    @Override
    public @NonNull String getIdentifier() {
        return "MESSAGE";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NonNull MessageType getMessageType() {
        return MessageType.CHAT;
    }

}
