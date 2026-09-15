package com.oheers.fish.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.config.serializer.ItemSerializer;
import com.oheers.fish.api.items.AbstractItemFactory;
import com.oheers.fish.items.config.CustomModelDataItemConfig;
import com.oheers.fish.items.config.DisplayNameItemConfig;
import com.oheers.fish.items.config.DurabilityItemConfig;
import com.oheers.fish.items.config.DyeColorItemConfig;
import com.oheers.fish.items.config.EnchantmentsItemConfig;
import com.oheers.fish.items.config.FireResistantItemConfig;
import com.oheers.fish.items.config.GlowingItemConfig;
import com.oheers.fish.items.config.HideTooltipItemConfig;
import com.oheers.fish.items.config.ItemModelItemConfig;
import com.oheers.fish.items.config.ItemRarityItemConfig;
import com.oheers.fish.items.config.LoreItemConfig;
import com.oheers.fish.items.config.MaxStackSizeItemConfig;
import com.oheers.fish.items.config.PotionEffectItemConfig;
import com.oheers.fish.items.config.QuantityItemConfig;
import com.oheers.fish.items.config.TooltipStyleItemConfig;
import com.oheers.fish.items.config.UnbreakableItemConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.oheers.fish.items.config.ItemConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ItemFactory extends AbstractItemFactory {

    private final @NonNull Section configuration;

    private boolean rawItem = false;
    private UUID relevantPlayer = null;
    private int randomIndex = -1;
    private Consumer<ItemStack> finalChanges = null;
    private @NonNull ItemStack baseItem;
    private boolean usingItemAddon = false;
    private boolean usingFallbackBaseItem = false;

    @SuppressWarnings("rawtypes") // Safe to ignore.
    private final Map<Class<? extends ItemConfig>, ItemConfig<?>> configMap = new HashMap<>();

    private ItemFactory(@NonNull Section initialSection, @Nullable String configLocation, @Nullable String itemPath) {
        Section section = configLocation == null ? initialSection : initialSection.createSection(configLocation);

        // Internally updates the configuration to put everything in the correct place.
        // As of 2.3.1, this no longer overwrites the file to avoid conflicting with fish display names.
        if (itemPath != null) {
            new ItemFactoryConversion().performConversions(section);
        }

        this.configuration = itemPath == null ? section : section.createSection(itemPath);

        loadConfigMap();

        this.baseItem = getBaseItem();
    }

    @Override
    public @NonNull ItemFactory createCopy() {
        ItemFactory newFactory = new ItemFactory(this.configuration, null, null);
        newFactory.relevantPlayer = this.relevantPlayer;
        newFactory.randomIndex = this.randomIndex;
        newFactory.finalChanges = this.finalChanges;
        return newFactory;
    }

    /**
     * Creates a new ItemFactory instance with the given configuration.
     * @param configuration The configuration to use.
     * @return A new ItemFactory instance.
     */
    public static ItemFactory itemFactory(@NonNull Section configuration) {
        return itemFactory(configuration, null);
    }

    /**
     * Creates a new ItemFactory instance with the given configuration and config location.
     * @param configuration The configuration to use.
     * @param configLocation The config location to use.
     * @return A new ItemFactory instance.
     */
    public static ItemFactory itemFactory(@NonNull Section configuration, @Nullable String configLocation) {
        return itemFactory(configuration, configLocation, "item");
    }

    /**
     * Creates a new ItemFactory instance with the given configuration and config location.
     * @param configuration The configuration to use.
     * @param configLocation The config location to use.
     * @return A new ItemFactory instance.
     */
    public static ItemFactory itemFactory(@NonNull Section configuration, @Nullable String configLocation, @NonNull String itemPath) {
        return new ItemFactory(configuration, configLocation, itemPath);
    }

    @Override
    public @NonNull ItemStack createItem(@Nullable Map<String, ?> replacements) {
        ItemStack item = baseItem.clone();
        if (rawItem) {
            return item;
        }

        OfflinePlayer player = relevantPlayer == null ? null : Bukkit.getOfflinePlayer(relevantPlayer);

        for (ItemConfig<?> config : configMap.values()) {
            ItemConfigApplier.apply(item, player, replacements, config, this.usingItemAddon);
        }

        if (finalChanges != null) {
            finalChanges.accept(item);
        }

        return item;
    }

    @Override
    public @NonNull ItemStack createItem(@NonNull UUID relevantPlayer, @Nullable Map<String, ?> replacements) {
        this.relevantPlayer = relevantPlayer;
        return createItem(replacements);
    }

    public @NonNull ItemStack getBaseItem() {
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
        // item.raw-materials
        ItemStack randomRawMaterial = checkRandomRawMaterial();
        if (randomRawMaterial != null) {
            rawItem = true;
            return randomRawMaterial;
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
        Logging.debug(configuration.getRouteAsString() + " has no valid item, returning default.");
        this.usingFallbackBaseItem = true;
        return new ItemStack(Material.COD);
    }

    @SuppressWarnings("unchecked") // Safe to ignore.
    public <T extends ItemConfig<?>> @Nullable T getItemConfig(@NonNull Class<T> clazz) {
        return (T) configMap.get(clazz);
    }

    // Base Item Methods //

    // Raw NBT
    private @Nullable ItemStack checkRawNbt() {
        String rawValue = configuration.getString("raw-nbt");
        if (rawValue == null) {
            return null;
        }
        return EvenMoreFish.getInstance().getVersionProvider().deserializeItemStack(rawValue);
    }


    // Material
    private @Nullable ItemStack getItemFromMaterialString(@NonNull String materialString) {
        ItemStack material = ItemSerializer.get().deserializeMaterial(materialString);
        if (material != null) {
            return new ItemStack(material);
        }
        Logging.debug(materialString + " is not a valid material, checking for custom item.");

        ItemStack customItem = ItemSerializer.get().deserializeItemAddon(materialString);
        if (customItem != null) {
            Logging.debug(materialString + " was a valid ItemAddon.");
            this.usingItemAddon = true;
            return customItem;
        }

        EvenMoreFish.getInstance().getLogger().severe("Could not find material or custom item for: " + materialString);
        return null;
    }

    private @Nullable ItemStack checkMaterial() {
        String materialStr = configuration.getString("material");
        if (materialStr == null) {
            return null;
        }
        return getItemFromMaterialString(materialStr);
    }

    private @Nullable ItemStack checkRandomMaterial() {
        ArrayList<String> materialStrs = new ArrayList<>(configuration.getStringList("materials"));
        if (materialStrs.isEmpty()) {
            return null;
        }
        // If there's only one material, skip randomization
        if (materialStrs.size() == 1) {
            return getItemFromMaterialString(materialStrs.getFirst());
        }
        return getRandomItem(materialStrs, this::getItemFromMaterialString);
    }

    private @Nullable ItemStack checkRawMaterial() {
        String materialStr = configuration.getString("raw-material");
        if (materialStr == null) {
            return null;
        }
        return getItemFromMaterialString(materialStr);
    }

    private @Nullable ItemStack checkRandomRawMaterial() {
        ArrayList<String> materialStrs = new ArrayList<>(configuration.getStringList("raw-materials"));
        if (materialStrs.isEmpty()) {
            return null;
        }
        // If there's only one material, skip randomization
        if (materialStrs.size() == 1) {
            return getItemFromMaterialString(materialStrs.getFirst());
        }
        return getRandomItem(materialStrs, this::getItemFromMaterialString);
    }

    // HeadDB

    private @Nullable ItemStack checkHeadDB() {
        if (!EvenMoreFish.getInstance().getDependencyManager().isUsingHeadsDB()) {
            return null;
        }
        String materialStr = configuration.getString("headdb");
        if (materialStr == null) {
            return null;
        }
        HeadDatabaseAPI api = EvenMoreFish.getInstance().getDependencyManager().getHdbapi();
        if (api == null) {
            return null;
        }
        ItemStack item = api.getItemHead(materialStr);
        if (item == null) {
            Logging.debug(configuration.getRouteAsString() + " has invalid headdb: " + materialStr);
            return null;
        }
        return item;
    }

    private @Nullable ItemStack checkRandomHeadDB() {
        if (!EvenMoreFish.getInstance().getDependencyManager().isUsingHeadsDB()) {
            return null;
        }
        ArrayList<String> materialStrs = new ArrayList<>(configuration.getStringList("multiple-headdb"));
        if (materialStrs.isEmpty()) {
            return null;
        }
        HeadDatabaseAPI api = EvenMoreFish.getInstance().getDependencyManager().getHdbapi();
        if (api == null) {
            return null;
        }
        if (materialStrs.size() == 1) {
            return api.getItemHead(materialStrs.getFirst());
        }
        return getRandomItem(materialStrs, api::getItemHead);
    }

    // Head 64

    private @Nullable ItemStack checkHead64() {
        String materialStr = configuration.getString("head-64");
        if (materialStr == null) {
            return null;
        }
        return FishUtils.getSkullFromBase64(materialStr);
    }

    private @Nullable ItemStack checkRandomHead64() {
        ArrayList<String> materialStrs = new ArrayList<>(configuration.getStringList("multiple-head-64"));
        if (materialStrs.isEmpty()) {
            return null;
        }
        if (materialStrs.size() == 1) {
            return FishUtils.getSkullFromBase64(materialStrs.getFirst());
        }
        return getRandomItem(materialStrs, FishUtils::getSkullFromBase64);
    }

    // Head UUID

    private @Nullable ItemStack checkHeadUUID() {
        String materialStr = configuration.getString("head-uuid");
        if (materialStr == null) {
            return null;
        }
        return FishUtils.getSkullFromUUIDString(materialStr);
    }

    private @Nullable ItemStack checkRandomHeadUUID() {
        ArrayList<String> materialStrs = new ArrayList<>(configuration.getStringList("multiple-head-uuid"));
        if (materialStrs.isEmpty()) {
            return null;
        }
        if (materialStrs.size() == 1) {
            return FishUtils.getSkullFromUUIDString(materialStrs.getFirst());
        }
        return getRandomItem(materialStrs, FishUtils::getSkullFromUUIDString);
    }

    // Own Head

    private @Nullable ItemStack checkOwnHead() {
        if (relevantPlayer == null) {
            return null;
        }
        String materialStr = configuration.getString("own-head");
        if (materialStr == null) {
            return null;
        }
        return FishUtils.getSkullFromUUID(relevantPlayer);
    }

    private @Nullable ItemStack getRandomItem(@NonNull List<String> strings, @NonNull Function<String, ItemStack> function) {
        if (randomIndex != -1) {
            Logging.debug("Random index is set to " + randomIndex + ", trying to use it.");
            try {
                String randomStr = strings.get(randomIndex);
                ItemStack randomItem = function.apply(randomStr);
                if (randomItem != null) {
                    return randomItem;
                }
            } catch (IndexOutOfBoundsException exception) {
                Logging.debug("Random index " + randomIndex + " is out of bounds, getting a new one.");
            }
        }

        ArrayList<String> checkList = new ArrayList<>(strings);
        final Random random = EvenMoreFish.getInstance().getRandom();

        // Get a random item from the list, keep trying until we find a valid one
        while (!checkList.isEmpty()) {
            int randomIndex = random.nextInt(checkList.size());
            String randomStr = checkList.remove(randomIndex);
            ItemStack randomItem = function.apply(randomStr);

            if (randomItem != null) {
                this.randomIndex = randomIndex;
                return randomItem;
            }

            Logging.debug(
                configuration.getRouteAsString() + " has an invalid name in its list: " + randomStr
            );
        }

        Logging.debug(
            configuration.getRouteAsString() + " has no valid items in its list."
        );
        return null;
    }

    public boolean isRawItem() {
        return rawItem;
    }

    @Override
    public void setRandomIndex(int randomIndex) {
        this.randomIndex = randomIndex;
        this.baseItem = getBaseItem();
    }

    @Override
    public int getRandomIndex() {
        return randomIndex;
    }

    public void setFinalChanges(@Nullable Consumer<ItemStack> finalChanges) {
        this.finalChanges = finalChanges;
    }

    public boolean isUsingFallbackBaseItem() {
        return this.usingFallbackBaseItem;
    }

    // Configs

    private void loadConfigMap() {
        loadConfig(new CustomModelDataItemConfig(configuration));
        loadConfig(new DisplayNameItemConfig(configuration));
        loadConfig(new DurabilityItemConfig(configuration));
        loadConfig(new DyeColorItemConfig(configuration));
        loadConfig(new EnchantmentsItemConfig(configuration));
        loadConfig(new FireResistantItemConfig(configuration));
        loadConfig(new GlowingItemConfig(configuration));
        loadConfig(new HideTooltipItemConfig(configuration));
        loadConfig(new ItemModelItemConfig(configuration));
        loadConfig(new ItemRarityItemConfig(configuration));
        loadConfig(new LoreItemConfig(configuration));
        loadConfig(new MaxStackSizeItemConfig(configuration));
        loadConfig(new PotionEffectItemConfig(configuration));
        loadConfig(new QuantityItemConfig(configuration));
        loadConfig(new TooltipStyleItemConfig(configuration));
        loadConfig(new UnbreakableItemConfig(configuration));
    }

    private <T extends ItemConfig<?>> void loadConfig(@NonNull T config) {
        configMap.put(config.getClass(), config);
    }

}
