package com.oheers.fish.config.gui.impl;

import com.oheers.fish.config.gui.GuiConfig;
import org.jspecify.annotations.NonNull;

public class BaitsMenuGuiConfig extends GuiConfig {

    private static final BaitsMenuGuiConfig INSTANCE = new BaitsMenuGuiConfig();

    private BaitsMenuGuiConfig() {
        super("baits.yml");
    }

    public static @NonNull BaitsMenuGuiConfig getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isPaginated() {
        return true;
    }

}
