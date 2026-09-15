package com.oheers.fish.items.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@ApiStatus.Internal
public abstract class ItemConfig<T> {

    protected T def;
    protected List<BiFunction<T, ItemStack, T>> transformers = new ArrayList<>();
    protected final Section section;
    protected boolean enabled = true;

    public ItemConfig(@NonNull Section section) {
        this.section = section;
    }

    protected ItemConfig(@NonNull ItemConfig<T> base) {
        super();
        this.def = base.def;
        this.transformers = new ArrayList<>(base.transformers);
        this.section = base.section;
        this.enabled = base.enabled;
    }

    public @Nullable T getActualValue() {
        T configured = getConfiguredValue();
        if (configured == null) {
            return def;
        }
        return configured;
    }

    /**
     * Applies the actual value to the item if this config is enabled.
     * @param item The item to apply the config to.
     */
    public void apply(@NonNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        if (!enabled) {
            return;
        }
        T value = getActualValue();
        for (BiFunction<T, ItemStack, T> transformer : transformers) {
            value = transformer.apply(value, item);
        }
        if (value != null) {
            applyToItem(player, replacements).accept(item, value);
        }
    }

    public abstract @Nullable T getConfiguredValue();

    protected abstract BiConsumer<ItemStack, T> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements);

    public void setDefault(@Nullable T def) {
        this.def = def;
    }

    public void addTransformer(@Nullable BiFunction<T, ItemStack, T> transformer) {
        this.transformers.add(transformer);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public abstract @NonNull ItemConfig<T> createCopy();

}
