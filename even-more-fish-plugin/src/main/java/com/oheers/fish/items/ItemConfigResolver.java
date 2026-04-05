package com.oheers.fish.items;

import com.oheers.fish.items.configs.CustomModelDataItemConfig;
import com.oheers.fish.items.configs.DisplayNameItemConfig;
import com.oheers.fish.items.configs.DyeColourItemConfig;
import com.oheers.fish.items.configs.EmptyItemConfig;
import com.oheers.fish.items.configs.EnchantmentsItemConfig;
import com.oheers.fish.items.configs.GlowingItemConfig;
import com.oheers.fish.items.configs.ItemConfig;
import com.oheers.fish.items.configs.ItemDamageItemConfig;
import com.oheers.fish.items.configs.LoreItemConfig;
import com.oheers.fish.items.configs.PotionMetaItemConfig;
import com.oheers.fish.items.configs.QuantityItemConfig;
import com.oheers.fish.items.configs.UnbreakableItemConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemConfigResolver {

    private static final ItemConfigResolver instance = new ItemConfigResolver();

    private @NotNull Function<Section, ItemConfig<Number>> customModelDataResolver = CustomModelDataItemConfig::new;
    private @NotNull Function<Section, ItemConfig<String>> displayNameResolver = DisplayNameItemConfig::new;
    private @NotNull Function<Section, ItemConfig<Color>> dyeColourResolver = DyeColourItemConfig::new;
    private @NotNull Function<Section, ItemConfig<Map<Enchantment, Integer>>> enchantmentsResolver = EnchantmentsItemConfig::new;
    private @NotNull Function<Section, ItemConfig<Boolean>> glowingResolver = GlowingItemConfig::new;
    private @NotNull Function<Section, ItemConfig<Integer>> damageResolver = ItemDamageItemConfig::new;
    private @NotNull Function<Section, ItemConfig<List<String>>> loreResolver = LoreItemConfig::new;
    private @NotNull Function<Section, ItemConfig<PotionEffect>> potionMetaResolver = PotionMetaItemConfig::new;
    private @NotNull Function<Section, ItemConfig<Integer>> quantityResolver = QuantityItemConfig::new;
    private @NotNull Function<Section, ItemConfig<Boolean>> unbreakableResolver = UnbreakableItemConfig::new;

    // These are set elsewhere as they have no functionality in 1.20.1.
    private @NotNull Function<Section, ItemConfig<NamespacedKey>> itemModelResolver = EmptyItemConfig::new;
    private @NotNull Function<Section, ItemConfig<Boolean>> fireResistantResolver = EmptyItemConfig::new;
    private @NotNull Function<Section, ItemConfig<Boolean>> hideTooltipResolver = EmptyItemConfig::new;
    private @NotNull Function<Section, ItemConfig<String>> itemRarityResolver = EmptyItemConfig::new;

    private ItemConfigResolver() {}

    public static @NotNull ItemConfigResolver getInstance() {
        return instance;
    }

    private <T> ItemConfig<T> resolve(Function<Section, ItemConfig<T>> resolver, @NotNull Section section) {
        return resolver == null ? null : resolver.apply(section);
    }

    public ItemConfig<Number> getCustomModelData(@NotNull Section section) {
        return resolve(customModelDataResolver, section);
    }

    public void setCustomModelDataResolver(@NotNull Function<Section, ItemConfig<Number>> customModelDataResolver) {
        this.customModelDataResolver = customModelDataResolver;
    }

    public @NotNull ItemConfig<String> getDisplayName(@NotNull Section section) {
        return resolve(displayNameResolver, section);
    }

    public void setDisplayNameResolver(@NotNull Function<Section, ItemConfig<String>> displayNameResolver) {
        this.displayNameResolver = displayNameResolver;
    }

    public @NotNull ItemConfig<Color> getDyeColour(@NotNull Section section) {
        return resolve(dyeColourResolver, section);
    }

    public void setDyeColourResolver(@NotNull Function<Section, ItemConfig<Color>> dyeColourResolver) {
        this.dyeColourResolver = dyeColourResolver;
    }

    public @NotNull ItemConfig<Map<Enchantment, Integer>> getEnchantments(@NotNull Section section) {
        return resolve(enchantmentsResolver, section);
    }

    public void setEnchantmentsResolver(@NotNull Function<Section, ItemConfig<Map<Enchantment, Integer>>> enchantmentsResolver) {
        this.enchantmentsResolver = enchantmentsResolver;
    }

    public @NotNull ItemConfig<Boolean> getGlowing(@NotNull Section section) {
        return resolve(glowingResolver, section);
    }

    public void setGlowingResolver(@NotNull Function<Section, ItemConfig<Boolean>> glowingResolver) {
        this.glowingResolver = glowingResolver;
    }

    public @NotNull ItemConfig<Integer> getDamage(@NotNull Section section) {
        return resolve(damageResolver, section);
    }

    public void setDamageResolver(@NotNull Function<Section, ItemConfig<Integer>> damageResolver) {
        this.damageResolver = damageResolver;
    }

    public @NotNull ItemConfig<List<String>> getLore(@NotNull Section section) {
        return resolve(loreResolver, section);
    }

    public void setLoreResolver(@NotNull Function<Section, ItemConfig<List<String>>> loreResolver) {
        this.loreResolver = loreResolver;
    }

    public @NotNull ItemConfig<PotionEffect> getPotionMeta(@NotNull Section section) {
        return resolve(potionMetaResolver, section);
    }

    public void setPotionMetaResolver(@NotNull Function<Section, ItemConfig<PotionEffect>> potionMetaResolver) {
        this.potionMetaResolver = potionMetaResolver;
    }

    public @NotNull ItemConfig<Integer> getQuantity(@NotNull Section section) {
        return resolve(quantityResolver, section);
    }

    public void setQuantityResolver(@NotNull Function<Section, ItemConfig<Integer>> quantityResolver) {
        this.quantityResolver = quantityResolver;
    }

    public @NotNull ItemConfig<Boolean> getUnbreakable(@NotNull Section section) {
        return resolve(unbreakableResolver, section);
    }

    public void setUnbreakableResolver(@NotNull Function<Section, ItemConfig<Boolean>> unbreakableResolver) {
        this.unbreakableResolver = unbreakableResolver;
    }

    public @NotNull ItemConfig<NamespacedKey> getItemModel(@NotNull Section section) {
        return resolve(itemModelResolver, section);
    }

    public void setItemModelResolver(@NotNull Function<Section, ItemConfig<NamespacedKey>> itemModelResolver) {
        this.itemModelResolver = itemModelResolver;
    }

    public @NotNull ItemConfig<Boolean> getFireResistant(@NotNull Section section) {
        return resolve(fireResistantResolver, section);
    }

    public void setFireResistantResolver(@NotNull Function<Section, ItemConfig<Boolean>> fireResistantResolver) {
        this.fireResistantResolver = fireResistantResolver;
    }

    public @NotNull ItemConfig<Boolean> getHideTooltip(@NotNull Section section) {
        return resolve(hideTooltipResolver, section);
    }

    public void setHideTooltipResolver(@NotNull Function<Section, ItemConfig<Boolean>> hideTooltipResolver) {
        this.hideTooltipResolver = hideTooltipResolver;
    }

    public @NotNull ItemConfig<String> getItemRarity(@NotNull Section section) {
        return resolve(itemRarityResolver, section);
    }

    public void setItemRarityResolver(@NotNull Function<Section, ItemConfig<String>> itemRarityResolver) {
        this.itemRarityResolver = itemRarityResolver;
    }

}
