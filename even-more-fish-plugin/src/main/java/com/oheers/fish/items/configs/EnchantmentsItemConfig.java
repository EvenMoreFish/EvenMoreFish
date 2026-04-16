package com.oheers.fish.items.configs;

import com.oheers.fish.FishUtils;
import com.oheers.fish.utils.Pair;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class EnchantmentsItemConfig extends ItemConfig<Map<Enchantment, Integer>> {

    public EnchantmentsItemConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public Map<Enchantment, Integer> getConfiguredValue() {
        List<String> strings = section.getStringList("item.enchantments");
        if (strings.isEmpty()) {
            return Map.of();
        }
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        for (String string : strings) {
            Pair<Enchantment, Integer> parsed = parseEnchantment(string);
            enchantments.put(parsed.getLeft(), parsed.getRight());
        }
        return enchantments;
    }

    private Pair<Enchantment, Integer> parseEnchantment(@NotNull String string) {
        String[] split = string.split(",");
        String name = split[0];
        Enchantment enchantment = FishUtils.getEnchantment(name);
        if (split.length == 1) {
            return new Pair<>(enchantment, 1);
        } else {
            Integer level = FishUtils.getInteger(split[1]);
            if (level == null) {
                level = 1;
            }
            return new Pair<>(enchantment, level);
        }
    }

    @Override
    protected BiConsumer<ItemStack, Map<Enchantment, Integer>> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
        return ItemStack::addUnsafeEnchantments;
    }

}
