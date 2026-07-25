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
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@ApiStatus.Internal
public class ItemConfigResolver {

    private static final ItemConfigResolver instance = new ItemConfigResolver();

    private @NonNull Function<Section, ItemConfig<Number>> customModelDataResolver = CustomModelDataItemConfig::new;
    private @NonNull Function<Section, ItemConfig<String>> displayNameResolver = DisplayNameItemConfig::new;
    private @NonNull Function<Section, ItemConfig<Color>> dyeColourResolver = DyeColourItemConfig::new;
    private @NonNull Function<Section, ItemConfig<Map<Enchantment, Integer>>> enchantmentsResolver = EnchantmentsItemConfig::new;
    private @NonNull Function<Section, ItemConfig<Boolean>> glowingResolver = GlowingItemConfig::new;
    private @NonNull Function<Section, ItemConfig<Integer>> damageResolver = ItemDamageItemConfig::new;
    private @NonNull Function<Section, ItemConfig<List<Component>>> loreResolver = LoreItemConfig::new;
    private @NonNull Function<Section, ItemConfig<PotionEffect>> potionMetaResolver = PotionMetaItemConfig::new;
    private @NonNull Function<Section, ItemConfig<Integer>> quantityResolver = QuantityItemConfig::new;
    private @NonNull Function<Section, ItemConfig<Boolean>> unbreakableResolver = UnbreakableItemConfig::new;

    // These are set elsewhere as they have no functionality in 1.20.1.
    private @NonNull Function<Section, ItemConfig<NamespacedKey>> itemModelResolver = EmptyItemConfig::new;
    private @NonNull Function<Section, ItemConfig<Boolean>> fireResistantResolver = EmptyItemConfig::new;
    private @NonNull Function<Section, ItemConfig<Boolean>> hideTooltipResolver = EmptyItemConfig::new;
    private @NonNull Function<Section, ItemConfig<String>> itemRarityResolver = EmptyItemConfig::new;
    private @NonNull Function<Section, ItemConfig<NamespacedKey>> tooltipStyleResolver = EmptyItemConfig::new;
    private @NonNull Function<Section, ItemConfig<Integer>> maxStackSizeResolver = EmptyItemConfig::new;

    private ItemConfigResolver() {}

    public static @NonNull ItemConfigResolver getInstance() {
        return instance;
    }

    private <T> ItemConfig<T> resolve(Function<Section, ItemConfig<T>> resolver, @NonNull Section section) {
        return resolver == null ? null : resolver.apply(section);
    }

    public ItemConfig<Number> getCustomModelData(@NonNull Section section) {
        return resolve(customModelDataResolver, section);
    }

    public void setCustomModelDataResolver(@NonNull Function<Section, ItemConfig<Number>> customModelDataResolver) {
        this.customModelDataResolver = customModelDataResolver;
    }

    public @NonNull ItemConfig<String> getDisplayName(@NonNull Section section) {
        return resolve(displayNameResolver, section);
    }

    public void setDisplayNameResolver(@NonNull Function<Section, ItemConfig<String>> displayNameResolver) {
        this.displayNameResolver = displayNameResolver;
    }

    public @NonNull ItemConfig<Color> getDyeColour(@NonNull Section section) {
        return resolve(dyeColourResolver, section);
    }

    public void setDyeColourResolver(@NonNull Function<Section, ItemConfig<Color>> dyeColourResolver) {
        this.dyeColourResolver = dyeColourResolver;
    }

    public @NonNull ItemConfig<Map<Enchantment, Integer>> getEnchantments(@NonNull Section section) {
        return resolve(enchantmentsResolver, section);
    }

    public void setEnchantmentsResolver(@NonNull Function<Section, ItemConfig<Map<Enchantment, Integer>>> enchantmentsResolver) {
        this.enchantmentsResolver = enchantmentsResolver;
    }

    public @NonNull ItemConfig<Boolean> getGlowing(@NonNull Section section) {
        return resolve(glowingResolver, section);
    }

    public void setGlowingResolver(@NonNull Function<Section, ItemConfig<Boolean>> glowingResolver) {
        this.glowingResolver = glowingResolver;
    }

    public @NonNull ItemConfig<Integer> getDamage(@NonNull Section section) {
        return resolve(damageResolver, section);
    }

    public void setDamageResolver(@NonNull Function<Section, ItemConfig<Integer>> damageResolver) {
        this.damageResolver = damageResolver;
    }

    public @NonNull ItemConfig<List<Component>> getLore(@NonNull Section section) {
        return resolve(loreResolver, section);
    }

    public void setLoreResolver(@NonNull Function<Section, ItemConfig<List<Component>>> loreResolver) {
        this.loreResolver = loreResolver;
    }

    public @NonNull ItemConfig<PotionEffect> getPotionMeta(@NonNull Section section) {
        return resolve(potionMetaResolver, section);
    }

    public void setPotionMetaResolver(@NonNull Function<Section, ItemConfig<PotionEffect>> potionMetaResolver) {
        this.potionMetaResolver = potionMetaResolver;
    }

    public @NonNull ItemConfig<Integer> getQuantity(@NonNull Section section) {
        return resolve(quantityResolver, section);
    }

    public void setQuantityResolver(@NonNull Function<Section, ItemConfig<Integer>> quantityResolver) {
        this.quantityResolver = quantityResolver;
    }

    public @NonNull ItemConfig<Boolean> getUnbreakable(@NonNull Section section) {
        return resolve(unbreakableResolver, section);
    }

    public void setUnbreakableResolver(@NonNull Function<Section, ItemConfig<Boolean>> unbreakableResolver) {
        this.unbreakableResolver = unbreakableResolver;
    }

    public @NonNull ItemConfig<NamespacedKey> getItemModel(@NonNull Section section) {
        return resolve(itemModelResolver, section);
    }

    public void setItemModelResolver(@NonNull Function<Section, ItemConfig<NamespacedKey>> itemModelResolver) {
        this.itemModelResolver = itemModelResolver;
    }

    public @NonNull ItemConfig<Boolean> getFireResistant(@NonNull Section section) {
        return resolve(fireResistantResolver, section);
    }

    public void setFireResistantResolver(@NonNull Function<Section, ItemConfig<Boolean>> fireResistantResolver) {
        this.fireResistantResolver = fireResistantResolver;
    }

    public @NonNull ItemConfig<Boolean> getHideTooltip(@NonNull Section section) {
        return resolve(hideTooltipResolver, section);
    }

    public void setHideTooltipResolver(@NonNull Function<Section, ItemConfig<Boolean>> hideTooltipResolver) {
        this.hideTooltipResolver = hideTooltipResolver;
    }

    public @NonNull ItemConfig<String> getItemRarity(@NonNull Section section) {
        return resolve(itemRarityResolver, section);
    }

    public void setItemRarityResolver(@NonNull Function<Section, ItemConfig<String>> itemRarityResolver) {
        this.itemRarityResolver = itemRarityResolver;
    }

    public @NonNull ItemConfig<NamespacedKey> getTooltipStyle(@NonNull Section section) {
        return resolve(tooltipStyleResolver, section);
    }

    public void setTooltipStyleResolver(@NonNull Function<Section, ItemConfig<NamespacedKey>> tooltipStyleResolver) {
        this.tooltipStyleResolver = tooltipStyleResolver;
    }

    public @NonNull ItemConfig<Integer> getMaxStackSize(@NonNull Section section) {
        return resolve(maxStackSizeResolver, section);
    }

    public void setMaxStackSizeResolver(@NonNull Function<Section, ItemConfig<Integer>> maxStackSizeResolver) {
        this.maxStackSizeResolver = maxStackSizeResolver;
    }

}
