package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;

public class GuiFillerConfig extends ConfigBase {

    private static GuiFillerConfig instance;

    public GuiFillerConfig() {
        super("gui-fillers.yml", "gui-fillers.yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }

    public static GuiFillerConfig getInstance() { return instance; }

    public EMFConfigReader getSupplier() {
        return new EMFConfigReader(getConfig());
    }

}
