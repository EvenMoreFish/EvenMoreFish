package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.gui.EMFGui;
import com.oheers.fish.gui.EMFGuiItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class GuiFillerConfig extends ConfigBase {

    private static GuiFillerConfig instance;

    public GuiFillerConfig() {
        super("gui-fillers.yml", "gui-fillers.yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }

    public static GuiFillerConfig getInstance() { return instance; }

    public List<EMFGuiItem> getDefaultFillerItems(@NotNull EMFGui gui) {
        return getConfig().getRoutesAsStrings(false).stream()
            .map(getConfig()::getSection)
            .filter(section -> section != null && section.contains("item"))
            .map(section -> EMFGuiItem.create(gui, section))
            .filter(Objects::nonNull)
            .toList();
    }

}
