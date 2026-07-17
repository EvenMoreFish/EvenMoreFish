package com.oheers.fish.config.gui.impl;

import com.oheers.fish.config.gui.GuiConfig;
import org.jetbrains.annotations.NotNull;

public class JournalRaritiesGuiConfig extends GuiConfig {

    private static final JournalRaritiesGuiConfig INSTANCE = new JournalRaritiesGuiConfig();

    private JournalRaritiesGuiConfig() {
        super("journal/rarities.yml");
    }

    public static @NotNull JournalRaritiesGuiConfig getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isPaginated() {
        return true;
    }

}
