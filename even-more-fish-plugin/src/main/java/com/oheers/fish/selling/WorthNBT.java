package com.oheers.fish.selling;

import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import com.oheers.fish.items.nbt.NbtKeys;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class WorthNBT {

    private WorthNBT() {
        throw new UnsupportedOperationException();
    }

    public static void setNBT(@NotNull ItemStack fishItem, @NotNull Fish fish) {
        if (fishItem.isEmpty()) {
            return;
        }
        NBTHolder<ItemStack> holder = NBTHolder.itemStack(fishItem);
        setNBT(holder, fish);
    }

    public static void setNBT(@NotNull Skull skull, @NotNull Fish fish) {
        NBTHolder<Skull> holder = NBTHolder.skull(skull);
        setNBT(holder, fish);
    }

    public static void setNBT(@NotNull NBTHolder<?> holder, @NotNull Fish fish) {
        holder.setAutoSave(false);

        float length = fish.getLength();
        if (length > 0) {
            holder.setFloat(NbtKeys.EMF_FISH_LENGTH.get(), length);
        }

        UUID fisherman = fish.getFishermanUUID();
        if (!fish.hasFishermanDisabled() && fisherman != null) {
            holder.setString(NbtKeys.EMF_FISH_PLAYER.get(), fisherman.toString());
        }

        holder.setString(NbtKeys.EMF_FISH_NAME.get(), fish.getName());
        holder.setString(NbtKeys.EMF_FISH_RARITY.get(), fish.getRarity().getId());
        holder.setInteger(NbtKeys.EMF_FISH_RANDOM_INDEX.get(), fish.getFactory().getRandomIndex());

        holder.save();
    }

    public static @NotNull Optional<Double> getValue(@NotNull Fish fish) {
        double setWorth = fish.getSetWorth();
        float length = fish.getLength();
        if (setWorth > 0) {
            return Optional.of(setWorth);
        } else if (length > 0.0D) {
            return getMultipliedValue(length, fish);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Double> getMultipliedValue(float length, @NotNull Fish fish) {
        double multiplier = fish.getWorthMultiplier();
        if (multiplier <= 0.0D) {
            return Optional.empty();
        }
        return Optional.of(multiplier * length);
    }

}
