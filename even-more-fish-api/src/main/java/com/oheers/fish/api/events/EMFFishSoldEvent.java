package com.oheers.fish.api.events;

import com.oheers.fish.api.economy.selling.SoldFish;
import com.oheers.fish.api.fishing.items.IFish;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;

/**
 * Fired when an EMF Fish is sold in the shop.
 */
public class EMFFishSoldEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final @NonNull IFish fish;
    private final double value;
    private final int quantity;
    private final @NonNull LocalDateTime sellTime;

    @ApiStatus.Internal
    public EMFFishSoldEvent(@NonNull Player player, @NonNull SoldFish fish) {
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
    public @NonNull HandlerList getHandlers() {
        return handlers;
    }

    public @NonNull IFish getFish() {
        return fish;
    }

    public double getValue() {
        return value;
    }

    public int getQuantity() {
        return quantity;
    }

    public @NonNull LocalDateTime getSellTime() {
        return sellTime;
    }

}
