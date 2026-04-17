package com.oheers.fish.fishing.items.config;

import com.oheers.fish.FishUtils;
import com.oheers.fish.fishing.items.Rarity;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

public class RarityFileUpdates {

    private final Rarity rarity;
    private boolean updated = false;

    public RarityFileUpdates(@NotNull Rarity rarity) {
        this.rarity = rarity;
    }


    public void update() {
        YamlDocument config = rarity.getConfig();

        // colour -> format
        if (config.contains("colour")) {
            String colour = config.getString("colour");
            String format = FishUtils.getFormat(colour);

            config.set("format", format);
            // Remove the old colour
            config.remove("colour");
            this.updated = true;
        }

        // display -> displayname
        if (config.contains("display") && !config.contains("displayname")) {
            config.set("displayname", config.getString("display"));
            config.remove("display");
            this.updated = true;
        }

        // Extended broadcast settings
        if (config.contains("broadcast")) {
            Section broadcast = config.getSection("broadcast");
            if (broadcast == null) {
                config.move("broadcast", "broadcast.enabled");
                config.set("broadcast.only-rods", false);
                config.set("broadcast.range", -1);
                this.updated = true;
            }
        }

        updateRequirementFormats();

        if (this.updated) {
            rarity.save();
        }
    }

    private void updateRequirementFormats() {
        Section config = rarity.getConfig();
        updateRequirementFormats(config);
        Section fishSect = config.getSection("fish");
        if (fishSect != null) {
            fishSect.getRoutesAsStrings(false).forEach(fishName -> {
                Section section = fishSect.getSection(fishName);
                if (section == null) {
                    return;
                }
                updateRequirementFormats(section);
            });
        }
    }

    private void updateRequirementFormats(@NotNull Section section) {
        Section ingameSection = section.getSection("requirement.ingame-time");
        if (ingameSection != null) {
            int min = ingameSection.getInt("minTime");
            int max = ingameSection.getInt("maxTime");
            ingameSection.remove("minTime");
            ingameSection.remove("maxTime");
            section.set("requirement.ingame-time", min + "-" + max);
            this.updated = true;
        }
        Section irlSection = section.getSection("requirement.irl-time");
        if (irlSection != null) {
            String min = irlSection.getString("minTime");
            String max = irlSection.getString("maxTime");
            irlSection.remove("minTime");
            irlSection.remove("maxTime");
            section.set("requirement.irl-time", min + "-" + max);
            this.updated = true;
        }
    }

}
