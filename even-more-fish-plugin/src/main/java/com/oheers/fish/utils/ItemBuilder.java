package com.oheers.fish.utils;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.ConfigUtils;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NbtApiException;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

public class ItemBuilder {

    private final @NotNull Section configuration;
    private boolean rawItem = false;
    private UUID relevantPlayer = null;

    private ItemBuilder(@NotNull Section configuration, @Nullable String configLocation) {
        if (configLocation == null) {
            this.configuration = configuration;
        } else {
            this.configuration = ConfigUtils.getOrCreateSection(configuration, configLocation);
        }
    }

    /**
     * Creates a new ItemBuilder instance with the given configuration.
     * @param configuration The configuration to use.
     * @return A new ItemBuilder instance.
     */
    public static ItemBuilder create(@NotNull Section configuration) {
        return new ItemBuilder(configuration, null);
    }

    /**
     * Creates a new ItemBuilder instance with the given configuration and config location.
     * @param configuration The configuration to use.
     * @param configLocation The config location to use.
     * @return A new ItemBuilder instance.
     */
    public static ItemBuilder create(@NotNull Section configuration, @NotNull String configLocation) {
        return new ItemBuilder(configuration, configLocation);
    }

    public @NotNull ItemStack createItem(@NotNull UUID relevantPlayer) {
        this.relevantPlayer = relevantPlayer;
        return getItem();
    }

    public @NotNull ItemStack createItem() {
        return getItem();
    }

    private @NotNull ItemStack getItem() {
        // item.raw-nbt
        ItemStack rawNbt = checkRawNbt();
        if (rawNbt != null) {
            rawItem = true;
            return rawNbt;
        }
        // item.material
        ItemStack material = checkMaterial();
        if (material != null) {
            return material;
        }
        // item.raw-material
        ItemStack rawMaterial = checkRawMaterial();
        if (rawMaterial != null) {
            rawItem = true;
            return rawMaterial;
        }
        // item.materials
        ItemStack randomMaterial = checkRandomMaterial();
        if (randomMaterial != null) {
            return randomMaterial;
        }
        // item.headdb
        ItemStack headDB = checkHeadDB();
        if (headDB != null) {
            return headDB;
        }
        // item.multiple-headdb
        ItemStack randomHeadDB = checkRandomHeadDB();
        if (randomHeadDB != null) {
            return randomHeadDB;
        }
        // item.head-64
        ItemStack head64 = checkHead64();
        if (head64 != null) {
            return head64;
        }
        // item.multiple-head-64
        ItemStack randomHead64 = checkRandomHead64();
        if (randomHead64 != null) {
            return randomHead64;
        }
        // item.head-uuid
        ItemStack headUUID = checkHeadUUID();
        if (headUUID != null) {
            return headUUID;
        }
        // item.multiple-head-uuid
        ItemStack randomHeadUUID = checkRandomHeadUUID();
        if (randomHeadUUID != null) {
            return randomHeadUUID;
        }
        // item.own-head
        ItemStack ownHead = checkOwnHead();
        if (ownHead != null) {
            return ownHead;
        }
        // Default item if no checks pass
        // This should ALWAYS be last
        EvenMoreFish.getInstance().debug(configuration.getRouteAsString() + " has no valid item, returning default.");
        return new ItemStack(Material.COD);
    }

    // Base Item Methods //

    // Raw NBT
    private @Nullable ItemStack checkRawNbt() {
        String rawValue = configuration.getString("item.raw-nbt");
        if (rawValue == null) {
            return null;
        }
        ItemStack item = null;
        try {
            item = NBT.itemStackFromNBT(NBT.parseNBT(rawValue));
        } catch (NbtApiException exception) {
            EvenMoreFish.getInstance().getLogger().severe(configuration.getRouteAsString() + " has invalid raw NBT: " + rawValue);
        }
        if (item == null) {
            return null;
        }
        rawItem = true;
        return item;
    }


    // Material
    private @Nullable ItemStack getItemFromMaterialString(@NotNull String materialString) {
        Material material = ItemUtils.getMaterial(materialString);
        if (material != null) {
            return new ItemStack(material);
        }
        EvenMoreFish.getInstance().debug(materialString + " is not a valid material, checking for custom item.");

        ItemStack customItem = FishUtils.getItem(materialString);
        if (customItem != null) {
            return customItem;
        }

        EvenMoreFish.getInstance().getLogger().severe("Could not find material or custom item for: " + materialString);
        return null;
    }

    private @Nullable ItemStack checkMaterial() {
        String materialStr = configuration.getString("item.material");
        if (materialStr == null) {
            return null;
        }
        return getItemFromMaterialString(materialStr);
    }

    private @Nullable ItemStack checkRandomMaterial() {
        ArrayList<String> materialStrs = new ArrayList<>(configuration.getStringList("item.materials"));
        if (materialStrs.isEmpty()) {
            return null;
        }
        // If there's only one material, skip randomization
        if (materialStrs.size() == 1) {
            return getItemFromMaterialString(materialStrs.get(0));
        }
        return getRandomItem(materialStrs, this::getItemFromMaterialString);
    }

    private @Nullable ItemStack checkRawMaterial() {
        String materialStr = configuration.getString("item.raw-material");
        if (materialStr == null) {
            return null;
        }
        return getItemFromMaterialString(materialStr);
    }

    // HeadDB

    private @Nullable ItemStack checkHeadDB() {
        if (!EvenMoreFish.getInstance().isUsingHeadsDB()) {
            return null;
        }
        String materialStr = configuration.getString("item.headdb");
        if (materialStr == null) {
            return null;
        }
        ItemStack item = EvenMoreFish.getInstance().getHDBapi().getItemHead(materialStr);
        if (item == null) {
            EvenMoreFish.getInstance().debug(configuration.getRouteAsString() + " has invalid headdb: " + materialStr);
            return null;
        }
        return item;
    }

    private @Nullable ItemStack checkRandomHeadDB() {
        if (!EvenMoreFish.getInstance().isUsingHeadsDB()) {
            return null;
        }
        ArrayList<String> materialStrs = new ArrayList<>(configuration.getStringList("item.multiple-headdb"));
        if (materialStrs.isEmpty()) {
            return null;
        }
        if (materialStrs.size() == 1) {
            return EvenMoreFish.getInstance().getHDBapi().getItemHead(materialStrs.get(0));
        }
        return getRandomItem(materialStrs, EvenMoreFish.getInstance().getHDBapi()::getItemHead);
    }

    // Head 64

    private @Nullable ItemStack checkHead64() {
        String materialStr = configuration.getString("item.head-64");
        if (materialStr == null) {
            return null;
        }
        return FishUtils.getSkullFromBase64(materialStr);
    }

    private @Nullable ItemStack checkRandomHead64() {
        ArrayList<String> materialStrs = new ArrayList<>(configuration.getStringList("item.multiple-head-64"));
        if (materialStrs.isEmpty()) {
            return null;
        }
        if (materialStrs.size() == 1) {
            return FishUtils.getSkullFromBase64(materialStrs.get(0));
        }
        return getRandomItem(materialStrs, FishUtils::getSkullFromBase64);
    }

    // Head UUID

    private @Nullable ItemStack checkHeadUUID() {
        String materialStr = configuration.getString("item.head-uuid");
        if (materialStr == null) {
            return null;
        }
        return FishUtils.getSkullFromUUIDString(materialStr);
    }

    private @Nullable ItemStack checkRandomHeadUUID() {
        ArrayList<String> materialStrs = new ArrayList<>(configuration.getStringList("item.multiple-head-uuid"));
        if (materialStrs.isEmpty()) {
            return null;
        }
        if (materialStrs.size() == 1) {
            return FishUtils.getSkullFromUUIDString(materialStrs.get(0));
        }
        return getRandomItem(materialStrs, FishUtils::getSkullFromUUIDString);
    }

    // Own Head

    private @Nullable ItemStack checkOwnHead() {
        if (relevantPlayer == null) {
            return null;
        }
        String materialStr = configuration.getString("item.own-head");
        if (materialStr == null) {
            return null;
        }
        return FishUtils.getSkullFromUUID(relevantPlayer);
    }

    private @Nullable ItemStack getRandomItem(@NotNull ArrayList<String> strings, @NotNull Function<String, ItemStack> function) {
        final Random random = EvenMoreFish.getInstance().getRandom();
        int randomIndex = random.nextInt(strings.size());

        // Get the item from the random string and check if it's valid
        String randomStr = strings.remove(randomIndex);
        ItemStack randomItem = getItemFromMaterialString(randomStr);
        if (randomItem != null) {
            return randomItem;
        }
        EvenMoreFish.getInstance().debug(
            configuration.getRouteAsString() + " has an invalid name in its list."
        );

        // Keep trying until we find a valid item
        for (String materialStr : strings) {
            ItemStack item = function.apply(materialStr);
            if (item != null) {
                return item;
            }
        }

        EvenMoreFish.getInstance().debug(
            configuration.getRouteAsString() + " has no valid items in its list."
        );
        return null;
    }

}
