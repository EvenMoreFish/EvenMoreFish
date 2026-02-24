package com.oheers.fish.gui.guis;

import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.gui.EMFGui;
import dev.triumphteam.gui.builder.gui.BaseGuiBuilder;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaitsGui extends EMFGui {

    public BaitsGui(@NotNull HumanEntity human) {
        super(
            GuiConfig.getInstance().getBaitsConfig(),
            human
        );
    }

    @Override
    public @NotNull BaseGuiBuilder<?, ?> buildGui() {
        return Gui.paginated().disableAllInteractions();
    }

    @Override
    public @Nullable String getItemCharacterKey() {
        return "bait-character";
    }

}
