package com.oheers.fish.addons.internal.reward.message;

import org.jspecify.annotations.NonNull;
import uk.firedev.messagelib.message.MessageType;

public class ActionBarRewardType extends MessageRewardBase {

    @Override
    public @NonNull MessageType getMessageType() {
        return MessageType.ACTION_BAR;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "ACTIONBAR";
    }

    @Override
    public @NonNull String getAuthor() {
        return "FireML";
    }

}
