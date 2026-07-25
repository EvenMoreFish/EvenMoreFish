package com.oheers.fish.api.baits;

import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.reward.Reward;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Internal implementation only. Extending this interface WILL cause issues.
 */
public interface IBait {

    @NonNull ItemStack create(@NonNull OfflinePlayer player);

    @NonNull ItemStack create();

    @NonNull List<? extends IRarity> getRarities();

    @NonNull IFish chooseFish(@NonNull Player player, @NonNull Location location);

    void handleFish(@NonNull Player player, @NonNull IFish fish, @NonNull ItemStack fishingRod);

    @NonNull String getId();

    // TODO Add format methods after EMFMessage is moved to API module.
    // @NonNull EMFSingleMessage getFormat();
    //@NonNull EMFSingleMessage format(@NonNull String name);

    @NonNull String getDisplayName();

    boolean isSilent();

    boolean hasCatchRewards();

    @NonNull List<Reward> getCatchRewards();

    double getPurchasePrice();

    int getPurchaseQuantity();

    @Nullable Economy getEconomy();

    boolean attemptPurchase(@NonNull Player player);

}
