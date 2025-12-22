package com.oheers.fish.api.events;

import com.oheers.fish.api.fishing.items.IFish;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class EMFFishCaughtEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final IFish fish;
    private final Player player;
    private final LocalDateTime catchTime;
    private boolean cancel;

    @ApiStatus.Internal
    public EMFFishCaughtEvent(@NotNull IFish fish, @NotNull Player player, final LocalDateTime catchTime) {
        this.fish = fish;
        this.player = player;
        this.catchTime = catchTime;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    /**
     * @return The fish that the player is receiving
     */
    public @NotNull IFish getFish() {
        return fish;
    }

    /**
     * @return The player that has fished the fish
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }


    public LocalDateTime getCatchTime() {
        return catchTime;
    }
}

