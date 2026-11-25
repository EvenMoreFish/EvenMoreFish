package com.oheers.fish.competition;

import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Serializes score acceptance with the transition to a final result. */
final class CompetitionScoreGate {

    private enum State { CREATED, RUNNING, ENDING, ENDED }

    private State state = State.CREATED;

    synchronized void start() {
        if (state != State.CREATED) {
            throw new IllegalStateException("Competition cannot start from " + state);
        }
        state = State.RUNNING;
    }

    synchronized boolean acceptScore(BooleanSupplier isActive, Runnable score) {
        if (state != State.RUNNING || !isActive.getAsBoolean()) {
            return false;
        }
        score.run();
        return true;
    }

    synchronized <T> @Nullable T close(Supplier<T> snapshot) {
        if (state == State.ENDING || state == State.ENDED) {
            return null;
        }
        state = State.ENDING;
        return snapshot.get();
    }

    synchronized void markEnded() {
        state = State.ENDED;
    }

}
