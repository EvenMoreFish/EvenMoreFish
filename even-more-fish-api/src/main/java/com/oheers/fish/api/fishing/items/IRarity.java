package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.sort.Sortable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Internal implementation only. Extending this interface WILL cause issues.
 */
public interface IRarity extends Sortable {

    @NonNull String getId();

    boolean isDisabled();

    double getWeight();

    /**
     * Internal use only. Will be removed in the future.
     */
    @ApiStatus.Internal
    @NonNull Component getDisplayNameComponent();

    boolean getBroadcastEnabled();

    boolean getBroadcastOnlyRods();

    int getBroadcastRange();

    boolean getUseConfigCasing();

    @Nullable String getPermission();

    @NonNull Requirement getRequirement();

    boolean isShouldDisableFisherman();

    double getSetWorth();

    @NonNull Optional<Double> getSetSize();

    double getMinSize();

    double getMaxSize();

    @NonNull List<? extends IFish> getOriginalFishList();

    @NonNull List<? extends IFish> getFishList();

    @Nullable IFish getEditableFish(@NonNull String name);

    @Nullable IFish getFish(@NonNull String name);

    double getWorthMultiplier();

    /**
     * @return The item to use in the journal menu, before display and lore is changed.
     */
    @NonNull ItemStack getJournalItem();

    boolean getShowInJournal();

    void setShowInJournal(boolean showInJournal);

    boolean isFishWeighted();

    void setFishWeighted(boolean fishWeighted);

    // Deprecated - Do not remove.

    /**
     * @deprecated Use {@link #getBroadcastEnabled()} instead.
     */
    @Deprecated(forRemoval = true)
    default boolean getAnnounce() {
        return getBroadcastEnabled();
    }

    /**
     * @deprecated Use {@link #getJournalItem()} instead.
     */
    @Deprecated(forRemoval = true)
    default ItemStack getMaterial() {
        return getJournalItem();
    }

}
