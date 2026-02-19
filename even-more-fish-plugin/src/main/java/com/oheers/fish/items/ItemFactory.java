package com.oheers.fish.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.config.ConfigUtils;
import com.oheers.fish.items.configs.ItemConfig;
import com.oheers.fish.utils.ItemUtils;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NbtApiException;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ItemFactory {

    private final @NotNull Section configuration;
    private boolean rawItem = false;
    private UUID relevantPlayer = null;
    private int randomIndex = -1;
    private Consumer<ItemStack> finalChanges = null;
    private @NotNull ItemStack baseItem;

    private final ItemConfig<Float> customModelData;
    private final ItemConfig<Integer> itemDamage;
    private final ItemConfig<String> displayName;
    private final ItemConfig<Color> dyeColour;
    private final ItemConfig<Boolean> glowing;
    private final ItemConfig<List<String>> lore;
    private final ItemConfig<PotionEffect> potionMeta;
    private final ItemConfig<Map<Enchantment, Integer>> enchantments;
    private final ItemConfig<Boolean> unbreakable;
    private final ItemConfig<Integer> quantity;
    private final ItemConfig<NamespacedKey> itemModel;
    private final ItemConfig<Boolean> fireResistant;
    private final ItemConfig<Boolean> hideTooltip;
    private final ItemConfig<String> itemRarity;

    private ItemFactory(@NotNull Section initialSection, @Nullable String configLocation) {
        if (configLocation == null) {
            this.configuration = initialSection;
        } else {
            this.configuration = ConfigUtils.getOrCreateSection(initialSection, configLocation);
        }

        // Updates the configuration to put everything in the correct place
        new ItemFactoryConversion().performConversions(this.configuration);

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

        this.baseItem = getBaseItem();
    }

    /**
     * Creates a new ItemFactory instance with the given configuration.
     * @param configuration The configuration to use.
     * @return A new ItemFactory instance.
     */
    public static ItemFactory itemFactory(@NotNull Section configuration) {
        return new ItemFactory(configuration, null);
    }

    /**
     * Creates a new ItemFactory instance with the given configuration and config location.
     * @param configuration The configuration to use.
     * @param configLocation The config location to use.
     * @return A new ItemFactory instance.
     */
    public static ItemFactory itemFactory(@NotNull Section configuration, @NotNull String configLocation) {
        return new ItemFactory(configuration, configLocation);
    }

    public @NotNull ItemStack createItem() {
        return createItem((Map<String, ?>) null);
    }

    public @NotNull ItemStack createItem(@Nullable Map<String, ?> replacements) {
        ItemStack item = baseItem.clone();

        if (!rawItem) {
            customModelData.apply(item, replacements);
            itemDamage.apply(item, replacements);
            displayName.apply(item, replacements);
            dyeColour.apply(item, replacements);
            glowing.apply(item, replacements);
            lore.apply(item, replacements);
            potionMeta.apply(item, replacements);
            enchantments.apply(item, replacements);
            unbreakable.apply(item, replacements);
            quantity.apply(item, replacements);
            itemModel.apply(item, replacements);
            fireResistant.apply(item, replacements);
            hideTooltip.apply(item, replacements);
            itemRarity.apply(item, replacements);

            if (finalChanges != null) {
                finalChanges.accept(item);
            }
        }

        return item;
    }

    public @NotNull ItemStack createItem(@NotNull UUID relevantPlayer) {
        this.relevantPlayer = relevantPlayer;
        return createItem();
    }

    public @NotNull ItemStack createItem(@NotNull UUID relevantPlayer, @Nullable Map<String, ?> replacements) {
        this.relevantPlayer = relevantPlayer;
        return createItem(replacements);
    }

    public @NotNull ItemStack getBaseItem() {
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

    // Customization Methods //

    public ItemConfig<Float> getCustomModelData() {
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

    public ItemConfig<List<String>> getLore() {
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

    // Base Item Methods //

    // Raw NBT
    private @Nullable ItemStack checkRawNbt() {
        String rawValue = configuration.getString("item.raw-nbt");
        if (rawValue == null) {
            return null;
        }

        try {
            return NBT.itemStackFromNBT(NBT.parseNBT(rawValue));
        } catch (NbtApiException exception) {
            EvenMoreFish.getInstance().getLogger().severe(configuration.getRouteAsString() + " has invalid raw NBT: " + rawValue);
            return null;
        }
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
        if (!EvenMoreFish.getInstance().getDependencyManager().isUsingHeadsDB()) {
            return null;
        }
        String materialStr = configuration.getString("item.headdb");
        if (materialStr == null) {
            return null;
        }
        ItemStack item = EvenMoreFish.getInstance().getDependencyManager().getHdbapi().getItemHead(materialStr);
        if (item == null) {
            EvenMoreFish.getInstance().debug(configuration.getRouteAsString() + " has invalid headdb: " + materialStr);
            return null;
        }
        return item;
    }

    private @Nullable ItemStack checkRandomHeadDB() {
        if (!EvenMoreFish.getInstance().getDependencyManager().isUsingHeadsDB()) {
            return null;
        }
        ArrayList<String> materialStrs = new ArrayList<>(configuration.getStringList("item.multiple-headdb"));
        if (materialStrs.isEmpty()) {
            return null;
        }
        if (materialStrs.size() == 1) {
            return EvenMoreFish.getInstance().getDependencyManager().getHdbapi().getItemHead(materialStrs.get(0));
        }
        return getRandomItem(materialStrs, EvenMoreFish.getInstance().getDependencyManager().getHdbapi()::getItemHead);
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

    private @Nullable ItemStack getRandomItem(@NotNull List<String> strings, @NotNull Function<String, ItemStack> function) {
        if (randomIndex != -1) {
            EvenMoreFish.getInstance().debug("Random index is set to " + randomIndex + ", trying to use it.");
            try {
                String randomStr = strings.get(randomIndex);
                ItemStack randomItem = function.apply(randomStr);
                if (randomItem != null) {
                    return randomItem;
                }
            } catch (IndexOutOfBoundsException exception) {
                EvenMoreFish.getInstance().debug("Random index " + randomIndex + " is out of bounds, getting a new one.");
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

            EvenMoreFish.getInstance().debug(
                configuration.getRouteAsString() + " has an invalid name in its list: " + randomStr
            );
        }

        EvenMoreFish.getInstance().debug(
            configuration.getRouteAsString() + " has no valid items in its list."
        );
        return null;
    }

    public boolean isRawItem() {
        return rawItem;
    }

    public void setRandomIndex(int randomIndex) {
        this.randomIndex = randomIndex;
        this.baseItem = getBaseItem();
    }

    public int getRandomIndex() {
        return randomIndex;
    }

    public void setFinalChanges(@Nullable Consumer<ItemStack> finalChanges) {
        this.finalChanges = finalChanges;
    }

}
