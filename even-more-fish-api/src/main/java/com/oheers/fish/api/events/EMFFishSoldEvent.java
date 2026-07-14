package com.oheers.fish.api.events;

import com.oheers.fish.api.economy.selling.SoldFish;
import com.oheers.fish.api.fishing.items.IFish;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * Fired when an EMF Fish is sold in the shop.
 */
public class EMFFishSoldEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final @NotNull IFish fish;
    private final double value;
    private final int quantity;
    private final @NotNull LocalDateTime sellTime;

    @ApiStatus.Internal
    public EMFFishSoldEvent(@NotNull Player player, @NotNull SoldFish fish) {
        super(player);
        this.value = fish.getFinalValue();
        this.fish = fish.getFish();
        this.quantity = fish.getQuantity();
        this.sellTime = fish.getSellTime();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public @NotNull IFish getFish() {
        return fish;
    }

    public double getValue() {
        return value;
    }

    public int getQuantity() {
        return quantity;
    }

    public @NotNull LocalDateTime getSellTime() {
        return sellTime;
    }

}
