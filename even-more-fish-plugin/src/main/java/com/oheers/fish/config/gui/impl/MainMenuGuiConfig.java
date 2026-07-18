package com.oheers.fish.config.gui.impl;

import com.oheers.fish.config.gui.GuiConfig;
import org.jetbrains.annotations.NotNull;

public class MainMenuGuiConfig extends GuiConfig {

    private static final MainMenuGuiConfig INSTANCE = new MainMenuGuiConfig();

    private MainMenuGuiConfig() {
        super("main.yml");
    }

    public static @NotNull MainMenuGuiConfig getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isPaginated() {
        return false;
    }

}
