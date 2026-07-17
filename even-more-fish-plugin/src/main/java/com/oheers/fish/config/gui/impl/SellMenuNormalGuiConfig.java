package com.oheers.fish.config.gui.impl;

import com.oheers.fish.config.gui.GuiConfig;
import org.jetbrains.annotations.NotNull;

public class SellMenuNormalGuiConfig extends GuiConfig {

    private static final SellMenuNormalGuiConfig INSTANCE = new SellMenuNormalGuiConfig();

    private SellMenuNormalGuiConfig() {
        super("sell/normal.yml");
    }

    public static @NotNull SellMenuNormalGuiConfig getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isPaginated() {
        return false;
    }

}
