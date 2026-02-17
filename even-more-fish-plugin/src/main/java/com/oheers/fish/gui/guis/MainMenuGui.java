package com.oheers.fish.gui.guis;

import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.gui.EMFGui;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
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

    @Override
    public @NotNull BaseGui buildGui() {
        return Gui.gui().create();
    }

}
