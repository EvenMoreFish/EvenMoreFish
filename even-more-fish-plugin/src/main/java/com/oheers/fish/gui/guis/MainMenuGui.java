package com.oheers.fish.gui.guis;

import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.gui.ConfigGui;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

public class MainMenuGui extends ConfigGui {

    public MainMenuGui(@NotNull HumanEntity viewer) {
        super(
            GuiConfig.getInstance().getConfig().getSection("main-menu"),
            viewer
        );
        createGui();
    }

}
