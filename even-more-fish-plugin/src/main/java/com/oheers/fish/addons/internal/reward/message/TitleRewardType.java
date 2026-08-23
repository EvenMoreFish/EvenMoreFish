package com.oheers.fish.addons.internal.reward.message;

import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.message.MessageType;

public class TitleRewardType extends MessageRewardBase {

    @Override
    public @NonNull MessageType getMessageType() {
        return MessageType.TITLE;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "TITLE";
    }

    @Override
    public @NonNull String getAuthor() {
        return "FireML";
    }

}
