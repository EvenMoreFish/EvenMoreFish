package com.oheers.fish.api;

import com.oheers.fish.competition.Competition;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

public class EMFCompetitionEndEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Competition competition;

    @ApiStatus.Internal
    public EMFCompetitionEndEvent(@NonNull Competition competition) {
        this.competition = competition;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return handlers;
    }

    /**
     * @return the Competition object that has been terminated
     */
    public @NonNull Competition getCompetition() {
        return this.competition;
    }
}
