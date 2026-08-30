package com.oheers.fish.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.config.serializer.ItemSerializer;
import com.oheers.fish.api.items.AbstractItemFactory;
import com.oheers.fish.items.configs.ItemConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
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

    private ItemConfig<Number> customModelData;
    private ItemConfig<Integer> itemDamage;
    private ItemConfig<String> displayName;
    private ItemConfig<Color> dyeColour;
    private ItemConfig<Boolean> glowing;
    private ItemConfig<List<Component>> lore;
    private ItemConfig<PotionEffect> potionMeta;
    private ItemConfig<Map<Enchantment, Integer>> enchantments;
    private ItemConfig<Boolean> unbreakable;
    private ItemConfig<Integer> quantity;
    private ItemConfig<NamespacedKey> itemModel;
    private ItemConfig<Boolean> fireResistant;
    private ItemConfig<Boolean> hideTooltip;
    private ItemConfig<String> itemRarity;
    private ItemConfig<NamespacedKey> tooltipStyle;
    private ItemConfig<Integer> maxStackSize;

    private ItemFactory(@NonNull Section initialSection, @Nullable String configLocation, @Nullable String itemPath) {
        Section section = configLocation == null ? initialSection : initialSection.createSection(configLocation);

        // Internally updates the configuration to put everything in the correct place.
        // As of 2.3.1, this no longer overwrites the file to avoid conflicting with fish display names.
        if (itemPath != null) {
            new ItemFactoryConversion().performConversions(section);
        }

        this.configuration = itemPath == null ? section : section.createSection(itemPath);

        ItemConfigResolver resolver = ItemConfigResolver.getInstance();

        this.customModelData = resolver.getCustomModelData(this.configuration);
        this.itemDamage = resolver.getDamage(this.configuration);
        this.displayName = resolver.getDisplayName(this.configuration);
        this.dyeColour = resolver.getDyeColour(this.configuration);
        this.glowing = resolver.getGlowing(this.configuration);
        this.lore = resolver.getLore(this.configuration);
        this.potionMeta = resolver.getPotionMeta(this.configuration);
        this.enchantments = resolver.getEnchantments(this.configuration);
        this.unbreakable = resolver.getUnbreakable(this.configuration);
        this.quantity = resolver.getQuantity(this.configuration);
        this.itemModel = resolver.getItemModel(this.configuration);
        this.fireResistant = resolver.getFireResistant(this.configuration);
        this.hideTooltip = resolver.getHideTooltip(this.configuration);
        this.itemRarity = resolver.getItemRarity(this.configuration);
        this.tooltipStyle = resolver.getTooltipStyle(this.configuration);
        this.maxStackSize = resolver.getMaxStackSize(this.configuration);

        this.baseItem = getBaseItem();
    }

    @Override
    public @NonNull ItemFactory createCopy() {
        ItemFactory newFactory = new ItemFactory(this.configuration, null, null);
        newFactory.relevantPlayer = this.relevantPlayer;
        newFactory.randomIndex = this.randomIndex;
        newFactory.finalChanges = this.finalChanges;

        // Copy all ItemConfig instances to the new factory - TODO figure out a cleaner way to handle ItemConfigs because this is not nice to maintain.
        newFactory.customModelData = this.customModelData.createCopy();
        newFactory.itemDamage = this.itemDamage.createCopy();
        newFactory.displayName = this.displayName.createCopy();
        newFactory.dyeColour = this.dyeColour.createCopy();
        newFactory.glowing = this.glowing.createCopy();
        newFactory.lore = this.lore.createCopy();
        newFactory.potionMeta = this.potionMeta.createCopy();
        newFactory.enchantments = this.enchantments.createCopy();
        newFactory.unbreakable = this.unbreakable.createCopy();
        newFactory.quantity = this.quantity.createCopy();
        newFactory.itemModel = this.itemModel.createCopy();
        newFactory.fireResistant = this.fireResistant.createCopy();
        newFactory.hideTooltip = this.hideTooltip.createCopy();
        newFactory.itemRarity = this.itemRarity.createCopy();
        newFactory.tooltipStyle = this.tooltipStyle.createCopy();
        newFactory.maxStackSize = this.maxStackSize.createCopy();

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

        if (!rawItem) {
            OfflinePlayer player = relevantPlayer == null ? null : Bukkit.getOfflinePlayer(relevantPlayer);

            if (this.usingItemAddon) {
                ItemFactoryConfig.getAddonDisplayBehavior().applyDisplay(item, player, replacements, displayName);
                ItemFactoryConfig.getAddonLoreBehavior().applyLore(item, player, replacements, lore);
            } else {
                displayName.apply(item, player, replacements);
                lore.apply(item, player, replacements);
            }

            customModelData.apply(item, player, replacements);
            itemDamage.apply(item, player, replacements);
            dyeColour.apply(item, player, replacements);
            glowing.apply(item, player, replacements);
            potionMeta.apply(item, player, replacements);
            enchantments.apply(item, player, replacements);
            unbreakable.apply(item, player, replacements);
            quantity.apply(item, player, replacements);
            itemModel.apply(item, player, replacements);
            fireResistant.apply(item, player, replacements);
            hideTooltip.apply(item, player, replacements);
            itemRarity.apply(item, player, replacements);
            tooltipStyle.apply(item, player, replacements);
            maxStackSize.apply(item, player, replacements);

            if (finalChanges != null) {
                finalChanges.accept(item);
            }
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

    // Customization Methods //

    public ItemConfig<Number> getCustomModelData() {
        return customModelData;
    }

    public ItemConfig<Integer> getItemDamage() {
        return itemDamage;
    }

    public ItemConfig<String> getDisplayName() {
        return displayName;
    }

    public ItemConfig<Color> getDyeColour() {
        return dyeColour;
    }

    public ItemConfig<Boolean> getGlowing() {
        return glowing;
    }

    public ItemConfig<List<Component>> getLore() {
        return lore;
    }

    public ItemConfig<PotionEffect> getPotionMeta() {
        return potionMeta;
    }

    public ItemConfig<Map<Enchantment, Integer>> getEnchantments() {
        return enchantments;
    }

    public ItemConfig<Boolean> getUnbreakable() {
        return unbreakable;
    }

    public ItemConfig<Integer> getQuantity() {
        return quantity;
    }

    public ItemConfig<NamespacedKey> getItemModel() {
        return itemModel;
    }

    public ItemConfig<Boolean> getFireResistant() {
        return fireResistant;
    }

    public ItemConfig<Boolean> getHideTooltip() {
        return hideTooltip;
    }

    public ItemConfig<String> getItemRarity() {
        return itemRarity;
    }

    public ItemConfig<NamespacedKey> getTooltipStyle() {
        return tooltipStyle;
    }

    public ItemConfig<Integer> getMaxStackSize() {
        return maxStackSize;
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
        final Random random = EvenMoreFish.RANDOM;

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

}
