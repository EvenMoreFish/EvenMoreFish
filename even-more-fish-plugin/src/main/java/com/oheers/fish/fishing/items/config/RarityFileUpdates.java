package com.oheers.fish.fishing.items.config;

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

            // This code is awful but handles all possible cases. Possibly look into cleaning it up later.
            String format;
            if (colour.contains(">") && colour.contains("</")) {
                int closingTag = colour.indexOf(">");
                format = colour.substring(0, closingTag + 1) + "{name}" + colour.substring(closingTag + 1);
            } else if (colour.contains(">")) {
                format = colour.substring(0, colour.indexOf(">") + 1) + "{name}";
            } else {
                format = colour + "{name}";
            }

            config.set("format", format);
            // Remove the old colour
            config.remove("colour");
        }

        rarity.save();
    }

}
