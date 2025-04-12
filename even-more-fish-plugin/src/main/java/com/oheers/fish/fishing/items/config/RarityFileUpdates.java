package com.oheers.fish.fishing.items.config;

import com.oheers.fish.FishUtils;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

public class RarityFileUpdates {

    public static void update(@NotNull Rarity rarity) {
        YamlDocument config = rarity.getConfig();

        // colour -> format
        if (config.contains("colour")) {
            String colour = config.getString("colour");
            String format = FishUtils.getFormat(colour);

            config.set("format", format);
            // Remove the old colour
            config.remove("colour");
        }

        rarity.save();
    }

}
