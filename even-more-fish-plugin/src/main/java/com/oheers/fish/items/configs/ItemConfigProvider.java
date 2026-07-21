package com.oheers.fish.items.configs;

import com.oheers.fish.EvenMoreFish;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.papermc.paper.inventory.ItemRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Provides ItemConfig instances for ItemFactory.
 * <p>
 * Should store instances locally and return them to allow edits.
 * <p>
 * If the Minecraft version does not support a config, use {@link EmptyItemConfig#EmptyItemConfig(Section)}.
 */
@ApiStatus.Internal
public abstract class ItemConfigProvider {

    protected final @NotNull Section section;

    public ItemConfigProvider(@NotNull Section section) {
        this.section = section;
    }

    public static @NotNull ItemConfigProvider create(@NotNull Section section) {
        return EvenMoreFish.getInstance().getVersionProvider().createItemConfigProvider(section);
    }

    public abstract @NotNull ItemConfig<Number> customModelData();

    public abstract @NotNull ItemConfig<String> displayName();

    public abstract @NotNull ItemConfig<Color> dyeColour();

    public abstract @NotNull ItemConfig<Map<Enchantment, Integer>> enchantments();

    public abstract @NotNull ItemConfig<Boolean> glowing();

    public abstract @NotNull ItemConfig<Integer> damage();

    public abstract @NotNull ItemConfig<List<Component>> lore();

    public abstract @NotNull ItemConfig<PotionEffect> potionEffect();

    public abstract @NotNull ItemConfig<Integer> quantity();

    public abstract @NotNull ItemConfig<Boolean> unbreakable();

    public abstract @NotNull ItemConfig<NamespacedKey> itemModel();

    public abstract @NotNull ItemConfig<Boolean> fireResistant();

    public abstract @NotNull ItemConfig<String> itemRarity();

    public abstract @NotNull ItemConfig<NamespacedKey> tooltipStyle();

    public abstract @NotNull ItemConfig<Integer> maxStackSize();

    public void apply(@NotNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        customModelData().apply(item, player, replacements);
        displayName().apply(item, player, replacements);
        dyeColour().apply(item, player, replacements);
        enchantments().apply(item, player, replacements);
        glowing().apply(item, player, replacements);
        damage().apply(item, player, replacements);
        lore().apply(item, player, replacements);
        potionEffect().apply(item, player, replacements);
        quantity().apply(item, player, replacements);
        unbreakable().apply(item, player, replacements);
        itemModel().apply(item, player, replacements);
        fireResistant().apply(item, player, replacements);
        itemRarity().apply(item, player, replacements);
        tooltipStyle().apply(item, player, replacements);
        maxStackSize().apply(item, player, replacements);
    }

}
