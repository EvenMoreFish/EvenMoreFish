package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.reward.Reward;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface IFish {

    @NotNull ItemStack give(int randomIndex);

    @NotNull ItemStack give();

    double getWorthMultiplier();

    boolean hasEatRewards();

    boolean hasFishRewards();

    boolean hasSellRewards();

    boolean hasIntRewards();

    void init();

    void checkSilent();

    @NotNull IFish createCopy();

    boolean hasFishermanDisabled();

    @Nullable UUID getFisherman();

    void setFisherman(@Nullable UUID uuid);

    boolean isCompExemptFish();

    void setCompExemptFish(boolean compExemptFish);

    double getSetWorth();

    @NotNull String getName();

    @NotNull IRarity getRarity();

    float getLength();

    void setLength(@Nullable Float length);

    @NotNull List<Reward> getActionRewards();

    @NotNull List<Reward> getFishRewards();

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

}
