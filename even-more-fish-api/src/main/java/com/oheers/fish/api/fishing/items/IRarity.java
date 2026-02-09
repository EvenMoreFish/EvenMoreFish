package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.requirement.Requirement;
import org.bukkit.entity.Fish;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Internal implementation only. Extending this interface WILL cause issues.
 */
public interface IRarity {

    @NotNull String getId();

    boolean isDisabled();

    double getWeight();

    boolean getAnnounce();

    boolean getUseConfigCasing();

    @Nullable String getPermission();

    @NotNull Requirement getRequirement();

    boolean isShouldDisableFisherman();

    double getMinSize();

    double getMaxSize();

    @NotNull List<? extends IFish> getOriginalFishList();

    @NotNull List<? extends IFish> getFishList();

    @Nullable IFish getEditableFish(@NotNull String name);

    @Nullable IFish getFish(@NotNull String name);

    double getWorthMultiplier();

    /**
     * @deprecated Use {@link #getJournalItem()} instead.
     */
    @Deprecated(forRemoval = true)
    default ItemStack getMaterial() {
        return getJournalItem();
    }

    /**
     * @return The item to use in the journal menu, before display and lore is changed.
     */
    @NotNull ItemStack getJournalItem();

    boolean getShowInJournal();

    void setShowInJournal(boolean showInJournal);

    boolean isFishWeighted();

    void setFishWeighted(boolean fishWeighted);

}
