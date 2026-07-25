package com.oheers.fish.config.gui.impl;

import com.oheers.fish.config.gui.GuiConfig;
import org.jspecify.annotations.NonNull;

public class SellMenuConfirmGuiConfig extends GuiConfig {

    private static final SellMenuConfirmGuiConfig INSTANCE = new SellMenuConfirmGuiConfig();

    private SellMenuConfirmGuiConfig() {
        super("sell/confirm.yml");
    }

    public static @NonNull SellMenuConfirmGuiConfig getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isPaginated() {
        return false;
    }

}
