package com.oheers.fish.gui.guis;

import com.oheers.fish.FishUtils;
import com.oheers.fish.baits.BaitManager;
import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.gui.ConfigGui;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

public class BaitsGui extends ConfigGui {

    public BaitsGui(@NotNull HumanEntity player) {
        super(
            GuiConfig.getInstance().getConfig().getSection("baits-menu"),
            player
        );

        createGui();

        Section config = getGuiConfig();
        if (config != null) {
            getGui().addElements(getBaitsGroup(config));
        }
    }

    private DynamicGuiElement getBaitsGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("bait-character", "b"), 'b');

        return new DynamicGuiElement(character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            BaitManager.getInstance().getBaitMap().values()
                .forEach(bait -> group.addElement(new StaticGuiElement(character, bait.create(player))));
            return group;
        });
    }

}
