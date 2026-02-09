package com.oheers.fish.api.baits;

import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Internal implementation only. Extending this interface WILL cause issues.
 */
public interface IBait {

    @NotNull ItemStack create(@NotNull OfflinePlayer player);

    @NotNull List<? extends IRarity> getRarities();

    @NotNull IFish chooseFish(@NotNull Player player, @NotNull Location location);

    void handleFish(@NotNull Player player, @NotNull IFish fish, @NotNull ItemStack fishingRod);

    @NotNull String getId();

    // TODO Add format methods after EMFMessage is moved to API module.
    // @NotNull EMFSingleMessage getFormat();
    //@NotNull EMFSingleMessage format(@NotNull String name);

    @NotNull String getDisplayName();

    double getPurchasePrice();

    int getPurchaseQuantity();

    @NotNull Economy getEconomy();

    boolean attemptPurchase(@NotNull Player player);

}
