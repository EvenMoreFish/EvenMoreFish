package com.oheers.fish.gui.guis;

import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.gui.EMFGui;
import dev.triumphteam.gui.builder.gui.BaseGuiBuilder;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainMenuGui extends EMFGui {

    public MainMenuGui(@NotNull HumanEntity human) {
        super(
            GuiConfig.getInstance().getMainMenuConfig(),
            human
        );
    }

    @Override
    public @NotNull BaseGuiBuilder<?, ?> buildGui() {
        return Gui.gui().disableAllInteractions();
    }

    @Override
    public @Nullable String getItemCharacterKey() {
        return null;
    }

}
