package com.oheers.fish.items;

import com.oheers.fish.items.configs.CustomModelDataItemConfig;
import com.oheers.fish.items.configs.DisplayNameItemConfig;
import com.oheers.fish.items.configs.DyeColourItemConfig;
import com.oheers.fish.items.configs.EmptyItemConfig;
import com.oheers.fish.items.configs.EnchantmentsItemConfig;
import com.oheers.fish.items.configs.FireResistantItemConfig;
import com.oheers.fish.items.configs.GlowingItemConfig;
import com.oheers.fish.items.configs.ItemConfig;
import com.oheers.fish.items.configs.ItemConfigProvider;
import com.oheers.fish.items.configs.ItemDamageItemConfig;
import com.oheers.fish.items.configs.ItemRarityItemConfig;
import com.oheers.fish.items.configs.LoreItemConfig;
import com.oheers.fish.items.configs.MaxStackSizeItemConfig;
import com.oheers.fish.items.configs.PotionMetaItemConfig;
import com.oheers.fish.items.configs.QuantityItemConfig;
import com.oheers.fish.items.configs.UnbreakableItemConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ItemConfigProviderImpl extends ItemConfigProvider {

    private final ItemConfig<Number> customModelData;
    private final ItemConfig<String> displayName;
    private final ItemConfig<Color> dyeColour;
    private final ItemConfig<Map<Enchantment, Integer>> enchantments;
    private final ItemConfig<Boolean> glowing;
    private final ItemConfig<Integer> damage;
    private final ItemConfig<List<Component>> lore;
    private final ItemConfig<PotionEffect> potionEffect;
    private final ItemConfig<Integer> quantity;
    private final ItemConfig<Boolean> unbreakable;
    private final ItemConfig<NamespacedKey> itemModel;
    private final ItemConfig<Boolean> fireResistant;
    private final ItemConfig<String> itemRarity;
    private final ItemConfig<NamespacedKey> tooltipStyle;
    private final ItemConfig<Integer> maxStackSize;

    public ItemConfigProviderImpl(@NotNull Section section) {
        super(section);

        this.customModelData = new CustomModelDataItemConfig(section);
        this.displayName = new DisplayNameItemConfig(section);
        this.dyeColour = new DyeColourItemConfig(section);
        this.enchantments = new EnchantmentsItemConfig(section);
        this.glowing = new GlowingItemConfig(section);
        this.damage = new ItemDamageItemConfig(section);
        this.lore = new LoreItemConfig(section);
        this.potionEffect = new PotionMetaItemConfig(section);
        this.quantity = new QuantityItemConfig(section);
        this.unbreakable = new UnbreakableItemConfig(section);
        this.fireResistant = new FireResistantItemConfig(section);
        this.itemRarity = new ItemRarityItemConfig(section);
        this.maxStackSize = new MaxStackSizeItemConfig(section);

        this.itemModel = new EmptyItemConfig<>(section);
        this.tooltipStyle = new EmptyItemConfig<>(section);
    }

    @Override
    public @NotNull ItemConfig<Number> customModelData() {
        return customModelData;
    }

    @NotNull
    @Override
    public ItemConfig<String> displayName() {
        return displayName;
    }

    @NotNull
    @Override
    public ItemConfig<Color> dyeColour() {
        return dyeColour;
    }

    @NotNull
    @Override
    public ItemConfig<Map<Enchantment, Integer>> enchantments() {
        return enchantments;
    }

    @NotNull
    @Override
    public ItemConfig<Boolean> glowing() {
        return glowing;
    }

    @NotNull
    @Override
    public ItemConfig<Integer> damage() {
        return damage;
    }

    @NotNull
    @Override
    public ItemConfig<List<Component>> lore() {
        return lore;
    }

    @NotNull
    @Override
    public ItemConfig<PotionEffect> potionEffect() {
        return potionEffect;
    }

    @NotNull
    @Override
    public ItemConfig<Integer> quantity() {
        return quantity;
    }

    @NotNull
    @Override
    public ItemConfig<Boolean> unbreakable() {
        return unbreakable;
    }

    @NotNull
    @Override
    public ItemConfig<NamespacedKey> itemModel() {
        return itemModel;
    }

    @NotNull
    @Override
    public ItemConfig<Boolean> fireResistant() {
        return fireResistant;
    }

    @NotNull
    @Override
    public ItemConfig<String> itemRarity() {
        return itemRarity;
    }

    @NotNull
    @Override
    public ItemConfig<NamespacedKey> tooltipStyle() {
        return tooltipStyle;
    }

    @NotNull
    @Override
    public ItemConfig<Integer> maxStackSize() {
        return maxStackSize;
    }

}
