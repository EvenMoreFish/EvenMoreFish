package com.oheers.fish.gui.guis;

import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.gui.EMFGui;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

public class MainMenuGui extends EMFGui {

    public MainMenuGui(@NotNull HumanEntity human) {
        super(
            GuiConfig.getInstance().getMainMenuConfig(),
            human
        );
    }

    @Override
    public void open() {

    }
}
