package com.oheers.fish.api;

import com.oheers.fish.selling.SoldFish;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Called when EMF Fish are sold
 */
public class EMFFishSellEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final List<SoldFish> soldFish;
    private final Player player;
    private final double worth;
    private final LocalDateTime soldTime;
    private boolean cancel;

    @ApiStatus.Internal
    public EMFFishSellEvent(@NotNull List<SoldFish> soldFish, @NotNull Player player, double worth, @NotNull LocalDateTime soldTime) {
        this.soldFish = soldFish;
        this.player = player;
        this.worth = worth;
        this.soldTime = soldTime;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    /**
     * @return The fish that the player is selling
     */
    public @NotNull List<SoldFish> getSoldFish() {
        return soldFish;
    }

    /**
     * @return The player that sold the fish
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * @return The total worth of fish sold
     */
    public double getWorth() {
        return worth;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public LocalDateTime getSoldTime() {
        return soldTime;
    }
}