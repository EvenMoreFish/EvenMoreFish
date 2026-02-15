package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.EMFGui;
import com.oheers.fish.gui.EMFGuiException;
import com.oheers.fish.gui.guis.SellGui;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiConfig extends ConfigBase {

    private static GuiConfig instance = null;

    public GuiConfig() {
        super("guis.yml", "guis.yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }
    
    public static GuiConfig getInstance() {
        return instance;
    }

    @Override
    public UpdaterSettings getUpdaterSettings() {
        UpdaterSettings.Builder builder = UpdaterSettings.builder(super.getUpdaterSettings());

        // Config Version 1 - Remove competition menu button
        builder.addCustomLogic("1", document -> document.remove("main-menu.coming-soon-competitions"));

        // Config Version 6 - Remove journal coming soon button
        builder.addCustomLogic("6", document -> document.remove("main-menu.coming-soon-journal"));

        return builder.build();
    }

    public @NotNull Section getApplyBaitsConfig() throws EMFGuiException {
        String sectionName = "apply-baits-menu";
        Section section = getConfig().getSection(sectionName);
        if (section == null) {
            throw new EMFGuiException("Could not find configuration for apply baits menu. (" + sectionName + ")");
        }
        return section;
    }

    public @NotNull Section getBaitsConfig() throws EMFGuiException {
        String sectionName = "baits-menu";
        Section section = getConfig().getSection(sectionName);
        if (section == null) {
            throw new EMFGuiException("Could not find configuration for baits menu. (" + sectionName + ")");
        }
        return section;
    }

    public @NotNull Section getFishJournalConfig(@Nullable Rarity rarity) throws EMFGuiException {
        String sectionName = rarity == null ? "journal-menu" : "journal-rarity";
        Section section = getConfig().getSection(sectionName);
        if (section == null) {
            throw new EMFGuiException("Could not find configuration for fish journal menu. (" + sectionName + ")");
        }
        return section;
    }

    public @NotNull Section getMainMenuConfig() throws EMFGuiException {
        String sectionName = "main-menu";
        Section section = getConfig().getSection(sectionName);
        if (section == null) {
            throw new EMFGuiException("Could not find configuration for main menu. (" + sectionName + ")");
        }
        return section;
    }

    public @NotNull Section getSellMenuConfig(@NotNull SellGui.SellState state) throws EMFGuiException {
        String sectionName = state.getConfigLocation();
        Section section = getConfig().getSection(sectionName);
        if (section == null) {
            throw new EMFGuiException("Could not find configuration for sell menu. (" + sectionName + ")");
        }
        return section;
    }

}
