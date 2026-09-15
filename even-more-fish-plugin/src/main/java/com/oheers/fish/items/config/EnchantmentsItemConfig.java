package com.oheers.fish.items.config;

import org.bukkit.OfflinePlayer;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.oheers.fish.items.config.ItemConfig;
import uk.firedev.daisylib.pairs.ReadOnlyPair;
import uk.firedev.daisylib.utils.CommonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class EnchantmentsItemConfig extends ItemConfig<Map<Enchantment, Integer>> {

    public EnchantmentsItemConfig(@NonNull Section section) {
        super(section);
    }

    public EnchantmentsItemConfig(@NonNull EnchantmentsItemConfig base) {
        super(base);
    }

    @Override
    public Map<Enchantment, Integer> getConfiguredValue() {
        List<String> strings = section.getStringList("enchantments");
        if (strings.isEmpty()) {
            return null;
        }
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        for (String string : strings) {
            ReadOnlyPair<Enchantment, Integer> parsed = parseEnchantment(string);
            enchantments.put(parsed.left(), parsed.right());
        }
        return enchantments;
    }

    private ReadOnlyPair<Enchantment, Integer> parseEnchantment(@NonNull String string) {
        String[] split = string.split(",");
        String name = split[0];
        Enchantment enchantment = CommonUtils.getEnchantment(name);
        if (split.length == 1) {
            return new ReadOnlyPair<>(enchantment, 1);
        } else {
            Integer level = CommonUtils.getIntOrDefault(split[1], 1);
            return new ReadOnlyPair<>(enchantment, level);
        }
    }

    @Override
    protected BiConsumer<ItemStack, Map<Enchantment, Integer>> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return ItemStack::addUnsafeEnchantments;
    }

    @Override
    public @NonNull EnchantmentsItemConfig createCopy() {
        return new EnchantmentsItemConfig(this);
    }

}
