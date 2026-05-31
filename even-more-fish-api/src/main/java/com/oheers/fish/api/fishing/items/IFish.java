package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.reward.Reward;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Internal implementation only. Extending this interface WILL cause issues.
 */
public interface IFish {

    @NotNull ItemStack give(int randomIndex);

    @NotNull ItemStack give();

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

    @NotNull IFish createCopy();

    boolean hasFishermanDisabled();

    @NotNull Optional<Double> getSetSize();

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

    @NotNull String getName();

    @NotNull IRarity getRarity();

    float getLength();

    void setLength(@Nullable Float length);

    @NotNull List<Reward> getActionRewards();

    @Deprecated(forRemoval = true, since = "2.3.5")
    default @NotNull List<Reward> getFishRewards() {
        return getCatchRewards();
    }

    @NotNull List<Reward> getCatchRewards();

    @NotNull List<Reward> getSellRewards();

    double getWeight();

    void setWeight(double weight);

    @NotNull Requirement getRequirement();

    void setRequirement(@NotNull Requirement requirement);

    boolean isWasBaited();

    void setWasBaited(boolean wasBaited);

    boolean isSilent();

    void setSilent(boolean silent);

    @NotNull CatchType getCatchType();

    boolean getShowInJournal();

    void setShowInJournal(boolean showInJournal);

    default RarityKey getRarityKey() {
        return RarityKey.of(this);
    }

}
