package com.oheers.fish.selling;

import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class WorthNBT {

    private WorthNBT() {
        throw new UnsupportedOperationException();
    }

    public static void setNBT(@NotNull ItemStack fishItem, @NotNull Fish fish) {
        if (NbtUtils.isInvalidItem(fishItem)) {
            return;
        }
        NBT.modify(fishItem, nbt -> {
            ReadWriteNBT emfCompound = nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND);

            // Set Length
            if (fish.getLength() > 0) {
                emfCompound.setFloat(NbtKeys.EMF_FISH_LENGTH, fish.getLength());
            }

            // Set Fisherman
            if (!fish.hasFishermanDisabled() && fish.getFisherman() != null) {
                emfCompound.setString(NbtKeys.EMF_FISH_PLAYER, fish.getFisherman().toString());
            }

            // Set Fish Name
            emfCompound.setString(NbtKeys.EMF_FISH_NAME, fish.getName());

            // Set Rarity
            emfCompound.setString(NbtKeys.EMF_FISH_RARITY, fish.getRarity().getId());

            // Set Random Index
            emfCompound.setInteger(NbtKeys.EMF_FISH_RANDOM_INDEX, fish.getFactory().getRandomIndex());
        });
    }

    public static void setNBT(@NotNull Skull skull, @NotNull Fish fish) {
        NamespacedKey lengthKey = NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_LENGTH);
        NamespacedKey playerKey = NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_PLAYER);
        NamespacedKey rarityKey = NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_RARITY);
        NamespacedKey fishNameKey = NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_NAME);
        NamespacedKey randomIndexKey = NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_RANDOM_INDEX);

        PersistentDataContainer pdc = skull.getPersistentDataContainer();

        // Set Length
        float length = fish.getLength();
        if (length > 0) {
            pdc.set(lengthKey, PersistentDataType.FLOAT, length);
        }

        // Set Fisherman
        if (!fish.hasFishermanDisabled()) {
            UUID fisherman = fish.getFisherman();
            if (fisherman != null) {
                pdc.set(playerKey, PersistentDataType.STRING, fisherman.toString());
            }
        }

        // Set Random Index
        pdc.set(randomIndexKey, PersistentDataType.INTEGER, fish.getFactory().getRandomIndex());

        // Set Rarity
        pdc.set(rarityKey, PersistentDataType.STRING, fish.getRarity().getId());

        // Set Fish Name
        pdc.set(fishNameKey, PersistentDataType.STRING, fish.getName());
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
