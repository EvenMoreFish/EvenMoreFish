package com.oheers.fish.gui.guis;

import com.oheers.fish.config.gui.impl.MainMenuGuiConfig;
import com.oheers.fish.gui.ConfigGui;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

public class MainMenuGui extends ConfigGui {

    public MainMenuGui(@NotNull HumanEntity viewer) {
        super(
            MainMenuGuiConfig.getInstance(),
            viewer
        );
        createGui();
    }

}
