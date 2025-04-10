package com.oheers.fish.gui.guis;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.gui.ConfigGui;
import com.oheers.fish.selling.SellHelper;
import de.themoep.inventorygui.GuiStorageElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SellGui extends ConfigGui {

    private final Inventory fishInventory;

    public SellGui(@NotNull Player player, @NotNull SellState sellState, @Nullable Inventory fishInventory) {
        super(sellState.getGuiConfig(), player);

        this.fishInventory = Objects.requireNonNullElseGet(fishInventory, () -> Bukkit.createInventory(player, 54));

        Economy economy = Economy.getInstance();

        SellHelper shopHelper = new SellHelper(this.fishInventory, player);
        addReplacement("{sell-price}", economy.getWorthFormat(shopHelper.getTotalWorth(), true));

        SellHelper playerHelper = new SellHelper(player.getInventory(), player);
        addReplacement("{sell-all-price}", economy.getWorthFormat(playerHelper.getTotalWorth(), true));

        setCloseAction(close -> {
            if (MainConfig.getInstance().sellOverDrop()) {
                new SellHelper(this.fishInventory, this.player).sellFish();
            }
            doRescue();
            return false;
        });

        createGui();

        Section config = getGuiConfig();
        if (config != null) {
            super.gui.addElement(new GuiStorageElement(FishUtils.getCharFromString(getGuiConfig().getString("deposit-character", "i"), 'i'), this.fishInventory));
        }
    }

    public Inventory getFishInventory() {
        return this.fishInventory;
    }

    public enum SellState {
        NORMAL("sell-menu-normal"),
        CONFIRM("sell-menu-confirm");

        private final String configLocation;

        SellState(@NotNull String configLocation) {
            this.configLocation = configLocation;
        }

        public Section getGuiConfig() {
            return GuiConfig.getInstance().getConfig().getSection(configLocation);
        }
    }

}
