package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.items.AbstractItemFactory;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.sort.Sortable;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Internal implementation only. Extending this interface WILL cause issues.
 */
public interface IFish extends Sortable {

    @NonNull ItemStack give(int randomIndex);

    @NonNull ItemStack give();

    double getWorthMultiplier();

    boolean hasEatRewards();

    @Deprecated(forRemoval = true, since = "2.3.5")
    default boolean hasFishRewards() {
        return hasCatchRewards();
    }

    boolean hasCatchRewards();

    boolean hasSellRewards();

    boolean hasIntRewards();

    void init();

    void checkSilent();

    @NonNull IFish createCopy();

    boolean hasFishermanDisabled();

    @NonNull Optional<Double> getSetSize();

    double getMinSize();

    double getMaxSize();

    /**
     * @deprecated Use {@link #getFishermanUUID()} instead.
     */
    @Deprecated(forRemoval = true)
    default @Nullable UUID getFisherman() {
        return getFishermanUUID();
    }

    @Nullable UUID getFishermanUUID();

    void setFisherman(@Nullable UUID uuid);

    void setFisherman(@Nullable OfflinePlayer fisherman);

    boolean isCompExemptFish();

    void setCompExemptFish(boolean compExemptFish);

    double getSetWorth();

    @NonNull String getName();

    @NonNull IRarity getRarity();

    float getLength();

    void setLength(@Nullable Float length);

    @NonNull List<Reward> getActionRewards();

    @Deprecated(forRemoval = true, since = "2.3.5")
    default @NonNull List<Reward> getFishRewards() {
        return getCatchRewards();
    }

    @NonNull List<Reward> getCatchRewards();

    @NonNull List<Reward> getSellRewards();

    double getWeight();

    /**
     * Internal use only. Will be removed in the future.
     */
    @ApiStatus.Internal
    @NonNull Component getDisplayNameComponent();

    int getCatchLimit();

    void setWeight(double weight);

    @NonNull AbstractItemFactory getFactory();

    @NonNull Requirement getRequirement();

    void setRequirement(@NonNull Requirement requirement);

    boolean isWasBaited();

    void setWasBaited(boolean wasBaited);

    boolean isSilent();

    void setSilent(boolean silent);

    @NonNull CatchType getCatchType();

    boolean getShowInJournal();

    void setShowInJournal(boolean showInJournal);

    default RarityKey getRarityKey() {
        return RarityKey.of(this);
    }

}
