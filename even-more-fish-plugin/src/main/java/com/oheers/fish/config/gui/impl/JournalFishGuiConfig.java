package com.oheers.fish.config.gui.impl;

import com.oheers.fish.config.gui.GuiConfig;
import org.jspecify.annotations.NonNull;

public class JournalFishGuiConfig extends GuiConfig {

    private static final JournalFishGuiConfig INSTANCE = new JournalFishGuiConfig();

    private JournalFishGuiConfig() {
        super("journal/fish.yml");
    }

    public static @NonNull JournalFishGuiConfig getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isPaginated() {
        return true;
    }

}
