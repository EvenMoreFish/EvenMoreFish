package com.oheers.fish.addons.internal.reward.message;

import org.jetbrains.annotations.NotNull;
import uk.firedev.messagelib.message.MessageType;

public class SubtitleRewardType extends MessageRewardBase {

    @Override
    public @NotNull MessageType getMessageType() {
        return MessageType.SUBTITLE;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "SUBTITLE";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FireML";
    }

}
