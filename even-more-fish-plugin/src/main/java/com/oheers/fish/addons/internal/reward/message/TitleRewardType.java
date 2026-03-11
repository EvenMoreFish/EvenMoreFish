package com.oheers.fish.addons.internal.reward.message;

import org.jetbrains.annotations.NotNull;
import uk.firedev.messagelib.message.MessageType;

public class TitleRewardType extends MessageRewardBase {

    @Override
    public @NotNull MessageType getMessageType() {
        return MessageType.TITLE;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "TITLE";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FireML";
    }

}
