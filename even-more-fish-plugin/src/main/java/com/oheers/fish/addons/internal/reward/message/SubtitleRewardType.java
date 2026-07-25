package com.oheers.fish.addons.internal.reward.message;

import org.jspecify.annotations.NonNull;
import uk.firedev.messagelib.message.MessageType;

public class SubtitleRewardType extends MessageRewardBase {

    @Override
    public @NonNull MessageType getMessageType() {
        return MessageType.SUBTITLE;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "SUBTITLE";
    }

    @Override
    public @NonNull String getAuthor() {
        return "FireML";
    }

}
