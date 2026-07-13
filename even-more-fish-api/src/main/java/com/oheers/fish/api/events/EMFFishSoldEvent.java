package com.oheers.fish.api.events;

import com.oheers.fish.api.economy.selling.SoldFish;
import com.oheers.fish.api.fishing.items.IFish;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when an EMF Fish is sold in the shop.
 */
public class EMFFishSoldEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final SoldFish fish;

    @ApiStatus.Internal
    public EMFFishSoldEvent(@NotNull Player player, @NotNull SoldFish fish) {
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

}
