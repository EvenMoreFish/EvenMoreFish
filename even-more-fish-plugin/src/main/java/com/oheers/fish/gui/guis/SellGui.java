package com.oheers.fish.gui.guis;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.gui.GuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuConfirmGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuNormalGuiConfig;
import com.oheers.fish.gui.ConfigGui;
import com.oheers.fish.api.economy.selling.SellHelper;
import de.themoep.inventorygui.GuiStorageElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

// TODO look into dynamically updating the sell items when a fish is added/removed - AFTER we switch to another library
public class SellGui extends ConfigGui {

    private final Inventory fishInventory;

    public SellGui(@NotNull Player player, @NotNull SellState sellState, @Nullable Inventory fishInventory) {
        super(sellState.getGuiConfig(), player);

        this.fishInventory = Optional.ofNullable(fishInventory).orElse(Bukkit.createInventory(player, 54));

        Economy economy = Economy.getInstance();

        double shopSaleValue = FishUtils.calculateInventoryWorth(this.fishInventory);
        addReplacement("{sell-price}", economy.getWorthFormat(shopSaleValue, true));

        double playerSaleValue = FishUtils.calculateInventoryWorth(player.getInventory());
        addReplacement("{sell-all-price}", economy.getWorthFormat(playerSaleValue, true));

        setCloseAction(close -> {
            if (MainConfig.getInstance().sellOverDrop()) {
                SellHelper.get().sell(this.fishInventory, this.player);
            }
            doRescue();
            return false;
        });

        createGui();

        Section config = getGuiConfig();
        if (config != null) {
            getGui().addElement(new GuiStorageElement(FishUtils.getCharFromString(getGuiConfig().getString("deposit-character", "i"), 'i'), this.fishInventory));
        }
    }

    public Inventory getFishInventory() {
        return this.fishInventory;
    }

    public enum SellState {
        NORMAL(SellMenuNormalGuiConfig::getInstance),
        CONFIRM(SellMenuConfirmGuiConfig::getInstance);

        private final Supplier<GuiConfig> configSupplier;

        SellState(@NotNull Supplier<GuiConfig> configSupplier) {
            this.configSupplier = configSupplier;
        }

        public GuiConfig getGuiConfig() {
            return configSupplier.get();
        }
    }

}
