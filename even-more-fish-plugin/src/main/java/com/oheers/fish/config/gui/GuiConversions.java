package com.oheers.fish.config.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.config.gui.impl.ApplyBaitsMenuGuiConfig;
import com.oheers.fish.config.gui.impl.BaitsMenuGuiConfig;
import com.oheers.fish.config.gui.impl.JournalFishGuiConfig;
import com.oheers.fish.config.gui.impl.JournalRaritiesGuiConfig;
import com.oheers.fish.config.gui.impl.MainMenuGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuConfirmGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuNormalGuiConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.message.ComponentMessage;

import java.io.File;

public class GuiConversions {

    public void performCheck() {
        File guisFile = new File(EvenMoreFish.getInstance().getDataFolder(), "guis.yml");
        if (!guisFile.exists() || !guisFile.isFile()) {
            return;
        }
        EvenMoreFish.getInstance().getLogger().info("Performing automatic conversion of gui configs.");
        File guiDir = getGuiDirectory();
        if (!guiDir.exists()) {
            guiDir.mkdirs();
        }
        ConfigBase config = new ConfigBase(guisFile, EvenMoreFish.getInstance(), false);
        Section general = config.getConfig().getSection("general");
        for (String key : config.getConfig().getRoutesAsStrings(false)) {
            Section section = config.getConfig().getSection(key);
            if (section != null) {
                convertSectionToFile(section, general);
            }
        }
        finalizeConversion(config);
    }

    private void finalizeConversion(@NotNull ConfigBase config) {
        // Rename the file to rarities.yml.old
        File file = config.getFile();
        file.renameTo(new File(EvenMoreFish.getInstance().getDataFolder(), "guis.yml.old"));
        file.delete();

        Logging.info(
            ComponentMessage.componentMessage("<yellow>Your gui configs have been automatically converted to the new format.").get()
        );
    }

    /**
     * @return The 'gui' directory. This may not exist yet.
     */
    public File getGuiDirectory() {
        return new File(EvenMoreFish.getInstance().getDataFolder(), "gui");
    }

    private void convertSectionToFile(@NotNull Section section, @Nullable Section general) {
        String id = section.getNameAsString();
        if (id == null) {
            return;
        }
        GuiConfig config = switch (section.getNameAsString()) {
            case "main-menu" -> MainMenuGuiConfig.getInstance();
            case "sell-menu-normal" -> SellMenuNormalGuiConfig.getInstance();
            case "sell-menu-confirm" -> SellMenuConfirmGuiConfig.getInstance();
            case "baits-menu" -> BaitsMenuGuiConfig.getInstance();
            case "apply-baits-menu" -> ApplyBaitsMenuGuiConfig.getInstance();
            case "journal-menu" -> JournalRaritiesGuiConfig.getInstance();
            case "journal-rarity" -> JournalFishGuiConfig.getInstance();
            default -> null;
        };
        if (config == null) {
            return;
        }
        // If paginated, add page buttons.
        if (config.isPaginated() && general != null) {
            addPageButtons(config.getConfig(), general);
        }
        config.getConfig().setAll(section.getRouteMappedValues(true));
        config.save();
    }

    private void addPageButtons(@NotNull Section config, @NotNull Section general) {
        for (String key : general.getRoutesAsStrings(false)) {
            Section section = general.getSection(key);
            switch (key) {
                case "first-page" -> section.set("character", "f");
                case "previous-page" -> section.set("character", "p");
                case "next-page" -> section.set("character", "n");
                case "last-page" -> section.set("character", "l");
                default -> {
                    continue;
                }
            }
            config.set(key, section);
        }
    }

}
