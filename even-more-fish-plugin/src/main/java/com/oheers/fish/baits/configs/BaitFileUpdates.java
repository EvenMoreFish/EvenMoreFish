package com.oheers.fish.baits.configs;

import com.oheers.fish.FishUtils;
import com.oheers.fish.baits.Bait;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;

public class BaitFileUpdates {

    public static void update(@NotNull Bait bait) {
        YamlDocument config = bait.getConfig();

        // bait-theme -> format
        if (config.contains("bait-theme")) {
            String theme = config.getString("bait-theme");
            String format = FishUtils.getFormat(theme);

            config.set("format", format);
            // Remove the old key
            config.remove("bait-theme");
        }

        bait.save();
    }

}
