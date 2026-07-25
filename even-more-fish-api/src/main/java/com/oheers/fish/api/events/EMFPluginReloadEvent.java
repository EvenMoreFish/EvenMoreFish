package com.oheers.fish.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

public class EMFPluginReloadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @ApiStatus.Internal
    public EMFPluginReloadEvent() {}

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return handlers;
    }

}
