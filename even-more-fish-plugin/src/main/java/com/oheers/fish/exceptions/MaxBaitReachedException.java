package com.oheers.fish.exceptions;

import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.model.ApplicationResult;
import com.oheers.fish.messages.ConfigMessage;
import org.jspecify.annotations.NonNull;

public class MaxBaitReachedException extends ConfigMessageException {
    private final ApplicationResult recoveryResult;

    public MaxBaitReachedException(@NonNull BaitHandler bait, @NonNull ApplicationResult recoveryResult) {
        super(bait.getId() + " has reached its maximum number of uses on the fishing rod.", ConfigMessage.BAITS_MAXED);
        this.recoveryResult = recoveryResult;
    }

    /**
     * @return The interrupted ApplicationResult object that would have been returned if it weren't for the error.
     */
    public @NonNull ApplicationResult getRecoveryResult() {
        return recoveryResult;
    }

}
