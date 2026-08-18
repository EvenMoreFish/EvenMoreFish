package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.items.AbstractItemFactory;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.sort.Sortable;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
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

    void init();

    void checkSilent();

    @NonNull IFish createCopy();

    boolean hasFishermanDisabled();

    @NonNull Optional<Double> getSetSize();

    double getMinSize();

    double getMaxSize();

    @Nullable UUID getFishermanUUID();

    void setFisherman(@Nullable UUID uuid);

    void setFisherman(@Nullable OfflinePlayer fisherman);

    double getSetWorth();

    @NonNull String getId();

    @NonNull IRarity getRarity();

    float getLength();

    default boolean isLengthless() {
        return getLength() <= 0;
    }

    void setLength(@Nullable Float length);

    double getWeight();

    @NonNull Component getDisplayName();

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

    // Rewards

    default boolean hasEatRewards() {
        return !getEatRewards().isEmpty();
    }

    default boolean hasCatchRewards() {
        return !getCatchRewards().isEmpty();
    }

    default boolean hasSellRewards() {
        return !getSellRewards().isEmpty();
    }

    default boolean hasInteractRewards() {
        return getInteractRewards().isEmpty();
    }

    @NonNull List<Reward> getInteractRewards();

    @NonNull List<Reward> getEatRewards();

    @NonNull List<Reward> getCatchRewards();

    @NonNull List<Reward> getSellRewards();

    // Deprecated - Do not remove.

    /**
     * @deprecated Use {@link #getId()} instead.
     */
    @Deprecated(since = "2.4.5")
    default @NonNull String getName() {
        return getId();
    }

    /**
     * @deprecated Use {@link #getFishermanUUID()} instead.
     */
    @Deprecated(forRemoval = true)
    default @Nullable UUID getFisherman() {
        return getFishermanUUID();
    }

    /**
     * @deprecated This was never functional and is redundant to check.
     */
    @Deprecated(forRemoval = true, since = "2.4.5")
    default boolean isCompExemptFish() {
        return false;
    }

    /**
     * @deprecated This was never functional and is redundant to use.
     */
    @Deprecated(forRemoval = true, since = "2.4.5")
    default void setCompExemptFish(boolean compExemptFish) {}

    /**
     * @deprecated Use {@link #getCatchRewards()} instead.
     */
    @Deprecated(since = "2.3.5")
    default @NonNull List<Reward> getFishRewards() {
        return getCatchRewards();
    }

    /**
     * @deprecated Use {@link #hasCatchRewards()} instead.
     */
    @Deprecated(since = "2.3.5")
    default boolean hasFishRewards() {
        return hasCatchRewards();
    }

    /**
     * @deprecated Use {@link #hasInteractRewards()} instead.
     */
    @Deprecated(since = "2.4.5")
    default boolean hasIntRewards() {
        return getInteractRewards().isEmpty();
    }

    /**
     * @deprecated Use {@link #getEatRewards()} or {@link #getInteractRewards()} instead.
     */
    @Deprecated(since = "2.4.5")
    default @NonNull List<Reward> getActionRewards() {
        if (hasInteractRewards()) {
            return getInteractRewards();
        }
        return getEatRewards();
    }

}
