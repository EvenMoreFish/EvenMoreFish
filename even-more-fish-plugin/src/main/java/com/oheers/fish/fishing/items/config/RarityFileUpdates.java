package com.oheers.fish.fishing.items.config;

import com.oheers.fish.FishUtils;
import com.oheers.fish.fishing.items.Rarity;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

public class RarityFileUpdates {

    public static void update(@NotNull Rarity rarity) {
        YamlDocument config = rarity.getConfig();

        boolean updated = false;

        // colour -> format
        if (config.contains("colour")) {
            String colour = config.getString("colour");
            String format = FishUtils.getFormat(colour);

            config.set("format", format);
            // Remove the old colour
            config.remove("colour");
            updated = true;
        }

        // Extended broadcast settings
        if (config.contains("broadcast")) {
            Section broadcast = config.getSection("broadcast");
            if (broadcast == null) {
                config.move("broadcast", "broadcast.enabled");
                config.set("broadcast.only-rods", false);
                config.set("broadcast.range", -1);
                updated = true;
            }
        }

        if (updated) {
            rarity.save();
        }
    }

}
