package com.oheers.fish.gui.guis;

import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.EMFGui;
import dev.triumphteam.gui.builder.gui.BaseGuiBuilder;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FishJournalGui extends EMFGui {

    private final @Nullable Rarity rarity;

    public FishJournalGui(@NotNull HumanEntity human, @Nullable Rarity rarity) {
        super(
            GuiConfig.getInstance().getFishJournalConfig(rarity),
            human
        );
        this.rarity = rarity;
    }

    @Override
    public @NotNull BaseGuiBuilder<?, ?> buildGui() {
        return Gui.paginated().disableAllInteractions();
    }

    @Override
    public @Nullable String getItemCharacterKey() {
        return rarity == null ? "rarity-character" : "fish-character";
    }

}
