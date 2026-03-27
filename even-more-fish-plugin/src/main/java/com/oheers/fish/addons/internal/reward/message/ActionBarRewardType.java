package com.oheers.fish.addons.internal.reward.message;

import org.jetbrains.annotations.NotNull;
import uk.firedev.messagelib.message.MessageType;

public class ActionBarRewardType extends MessageRewardBase {

    @Override
    public @NotNull MessageType getMessageType() {
        return MessageType.ACTION_BAR;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ACTIONBAR";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FireML";
    }

}
