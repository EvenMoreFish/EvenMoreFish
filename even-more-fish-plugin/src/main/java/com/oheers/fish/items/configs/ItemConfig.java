package com.oheers.fish.items.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public abstract class ItemConfig<T> {

    private T def;
    private T override;
    protected final Section section;
    protected boolean enabled = true;

    public ItemConfig(@NotNull Section section) {
        this.section = section;
    }

    public T getActualValue() {
        T val = override == null ? getConfiguredValue() : override;
        return val == null ? def : val;
    }

    /**
     * Applies the actual value to the item if this config is enabled.
     * @param item The item to apply the config to.
     */
    public void apply(@NotNull ItemStack item, @Nullable Map<String, ?> replacements) {
        if (!enabled) {
            return;
        }
        T value = getActualValue();
        if (value != null) {
            applyToItem(replacements).accept(item, value);
        }
    }

    public abstract T getConfiguredValue();

    protected abstract BiConsumer<ItemStack, T> applyToItem(@Nullable Map<String, ?> replacements);

    public void setDefault(T def) {
        this.def = def;
    }

    public void setOverride(T override) {
        this.override = override;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
