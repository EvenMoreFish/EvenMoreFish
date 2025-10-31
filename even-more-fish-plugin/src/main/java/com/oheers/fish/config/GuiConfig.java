package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.config.ConfigBase;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

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

}
