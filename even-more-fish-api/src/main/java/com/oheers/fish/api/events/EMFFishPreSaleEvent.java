package com.oheers.fish.api.events;

import com.oheers.fish.api.economy.selling.SoldFish;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when an EMF Fish is sold in the shop.
 */
public class EMFFishPreSaleEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final SoldFish fish;

    private boolean cancel = false;

    @ApiStatus.Internal
    public EMFFishPreSaleEvent(@NotNull Player player, @NotNull SoldFish fish) {
        super(player);
        this.fish = fish;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    /**
     * @return The fish that the player is selling.
     */
    public @NotNull SoldFish getSoldFish() {
        return this.fish;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
