package com.oheers.fish.exceptions;

import com.oheers.fish.baits.model.ApplicationResult;
import com.oheers.fish.messages.ConfigMessage;
import org.jspecify.annotations.NonNull;

public class MaxBaitsReachedException extends ConfigMessageException {
    //BAITS_MAXED_ON_ROD
    private final ApplicationResult recoveryResult;

    public MaxBaitsReachedException(@NonNull String errorMessage, @NonNull ApplicationResult recoveryResult) {
        super(errorMessage, ConfigMessage.BAITS_MAXED_ON_ROD);
        this.recoveryResult = recoveryResult;
    }

    public @NonNull ApplicationResult getRecoveryResult() {
        return recoveryResult;
    }
}
