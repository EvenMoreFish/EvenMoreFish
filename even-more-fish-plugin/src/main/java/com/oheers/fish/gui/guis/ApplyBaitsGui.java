package com.oheers.fish.gui.guis;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.baits.ApplicationResult;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitManager;
import com.oheers.fish.baits.BaitNBTManager;
import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.gui.ConfigGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import de.themoep.inventorygui.GuiStorageElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApplyBaitsGui extends ConfigGui {

    private final Inventory baitInventory;

    public ApplyBaitsGui(@NotNull Player player, @Nullable Inventory baitInventory) {
        super(
            GuiConfig.getInstance().getConfig().getSection("apply-baits-menu"),
            player
        );
        this.baitInventory = Objects.requireNonNullElseGet(baitInventory, () -> Bukkit.createInventory(player, 54));

        setCloseAction(close -> {
            processBaits();
            doRescue();
            return false;
        });

        createGui();

        Section config = getGuiConfig();
        if (config != null) {
            getGui().addElement(new GuiStorageElement(FishUtils.getCharFromString(getGuiConfig().getString("bait-character", "b"), 'b'), this.baitInventory));
        }
    }

    private void processBaits() {
        ItemStack handItem = this.player.getInventory().getItemInMainHand();
        if (!Checks.canUseRod(handItem)) {
            return;
        }
        boolean changedRod = false;
        List<String> ignoredBaits = new ArrayList<>();
        for (ItemStack item : baitInventory.getContents()) {
            Bait bait = BaitManager.getInstance().getBait(item);
            if (bait == null) {
                continue;
            }
            if (ignoredBaits.contains(bait.getId())) {
                continue;
            }
            ApplicationResult result;

            // Try to apply all the baits.
            try {
                result = BaitNBTManager.applyBaitedRodNBT(handItem, bait, item.getAmount());
                EvenMoreFish.getInstance().incrementMetricBaitsApplied(item.getAmount());
                // When a specific bait is maxed.
            } catch (MaxBaitReachedException exception) {
                EMFMessage message = ConfigMessage.BAITS_MAXED_ON_ROD.getMessage();
                message.setBait(bait.format(bait.getId()));
                message.send(this.player);
                // We should now start to ignore this bait.
                ignoredBaits.add(bait.getId());
                continue;
                // When the rod cannot contain any more baits.
            } catch (MaxBaitsReachedException exception) {
                ConfigMessage.BAITS_MAXED.getMessage().send(this.player);
                // Return here as the fishing rod cannot fit any more baits.
                return;
            }

            if (result == null || result.getFishingRod() == null) {
                continue;
            }

            // Remove the bait items from the inventory.
            this.baitInventory.remove(item);
            // Set the handItem variable.
            handItem = result.getFishingRod();
            changedRod = true;
        }

        if (changedRod) {
            this.player.getInventory().setItemInMainHand(handItem);
        }
    }

}
