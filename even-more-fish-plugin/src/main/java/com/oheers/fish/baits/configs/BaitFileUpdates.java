package com.oheers.fish.baits.configs;

import com.oheers.fish.FishUtils;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.model.WeightModifier;
import com.oheers.fish.config.MainConfig;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BaitFileUpdates {

    public static void update(@NotNull BaitHandler bait) {
        YamlDocument config = bait.getConfig();
        boolean changed = false;

        // bait-theme -> format
        if (config.contains("bait-theme")) {
            String theme = config.getString("bait-theme");
            String format = FishUtils.getFormat(theme);

            config.set("format", format);
            // Remove the old key
            config.remove("bait-theme");
            changed = true;
        }

        changed |= migrateLegacyBaitModifiers(config, MainConfig.getInstance().getBaitBoostRate());

        if (changed) {
            bait.save();
        }
    }

    static boolean migrateLegacyBaitModifiers(@NotNull YamlDocument config, double baitBoostRate) {
        boolean changed = false;
        String legacyModifier = WeightModifier.multiply(baitBoostRate).describe();

        changed |= migrateLegacyRarityModifiers(config, legacyModifier);
        changed |= migrateLegacyFishModifiers(config, legacyModifier);

        return changed;
    }

    private static boolean migrateLegacyRarityModifiers(@NotNull YamlDocument config, @NotNull String legacyModifier) {
        List<String> legacyRarities = config.getStringList("rarities");
        if (legacyRarities.isEmpty()) {
            return false;
        }

        if (!config.contains("rarity-modifiers")) {
            Map<String, Object> migrated = new LinkedHashMap<>();
            for (String rarity : legacyRarities) {
                migrated.put(rarity, legacyModifier);
            }
            config.set("rarity-modifiers", migrated);
        }

        config.remove("rarities");
        return true;
    }

    private static boolean migrateLegacyFishModifiers(@NotNull YamlDocument config, @NotNull String legacyModifier) {
        Section legacyFish = config.getSection("fish");
        if (legacyFish == null) {
            return false;
        }

        if (!config.contains("fish-modifiers")) {
            Map<String, Object> migrated = new LinkedHashMap<>();
            for (String rarity : legacyFish.getRoutesAsStrings(false)) {
                List<String> fishList = config.getStringList("fish." + rarity);
                if (fishList.isEmpty()) {
                    continue;
                }

                Map<String, Object> rarityFish = new LinkedHashMap<>();
                for (String fishName : fishList) {
                    rarityFish.put(fishName, legacyModifier);
                }
                migrated.put(rarity, rarityFish);
            }
            config.set("fish-modifiers", migrated);
        }

        config.remove("fish");
        return true;
    }
}
