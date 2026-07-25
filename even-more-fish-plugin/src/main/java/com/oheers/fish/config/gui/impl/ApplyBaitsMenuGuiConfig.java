package com.oheers.fish.config.gui.impl;

import com.oheers.fish.config.gui.GuiConfig;
import org.jspecify.annotations.NonNull;

public class ApplyBaitsMenuGuiConfig extends GuiConfig {

    private static final ApplyBaitsMenuGuiConfig INSTANCE = new ApplyBaitsMenuGuiConfig();

    private ApplyBaitsMenuGuiConfig() {
        super("applybaits.yml");
    }

    public static @NonNull ApplyBaitsMenuGuiConfig getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isPaginated() {
        return false;
    }
}
