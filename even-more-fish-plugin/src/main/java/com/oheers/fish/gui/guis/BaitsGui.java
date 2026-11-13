package com.oheers.fish.gui.guis;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.sort.SortType;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.config.gui.impl.BaitsMenuGuiConfig;
import com.oheers.fish.gui.EMFConfigGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.utils.CooldownHelper;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.message.ComponentListMessage;
import uk.firedev.daisylib.messages.message.ComponentMessage;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BaitsGui extends EMFConfigGui {

    private final CooldownHelper confirmation = CooldownHelper.create();
    private final CooldownHelper cooldown = CooldownHelper.create();
    private final SortType sortType;

    public BaitsGui(@NonNull HumanEntity player) {
        super(
            BaitsMenuGuiConfig.getInstance(),
            player
        );

        sortType = FishUtils.getEnumValue(
            SortType.class,
            guiConfig.getConfig().getString("sort-type"),
            SortType.ALPHABETICAL
        );
    }

    private DynamicGuiElement getBaitsGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("bait-character", "b"), 'b');

        return new DynamicGuiElement(character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            sortType.sort(BaitManager.getInstance().getItemMap().values())
                .forEach(bait -> group.addElement(createBaitElement(character, bait)));
            return group;
        });
    }

    private StaticGuiElement createBaitElement(char character, @NonNull BaitHandler bait) {
        return new StaticGuiElement(
            character,
            createBaitItem(bait),
            click -> {
                UUID uuid = player.getUniqueId();
                if (cooldown.hasCooldown(uuid)) {
                    return true;
                }
                if (requireConfirmation(uuid)) {
                    ConfigMessage.BAIT_CONFIRM_PURCHASE.getMessage().send(player);
                    return true;
                }
                bait.attemptPurchase(player);
                // Quarter-second cooldown to prevent spam and accidents.
                cooldown.applyCooldown(uuid, Duration.ofMillis(250));
                return true;
            }
        );
    }

    private List<String> getPurchaseLoreFormat() {
        return guiConfig.getConfig().getStringList("purchase-lore");
    }

    private ItemStack createBaitItem(@NonNull BaitHandler bait) {
        ItemStack item = bait.create(player);
        item.editMeta(meta -> applyLore(meta, bait));
        return item;
    }

    private void applyLore(@NonNull ItemMeta meta, @NonNull BaitHandler bait) {
        Economy economy = bait.getEconomy();
        if (economy == null || economy.isEmpty()) {
            return;
        }
        List<String> loreFormat = getPurchaseLoreFormat();
        if (loreFormat.isEmpty()) {
            return;
        }
        ComponentListMessage purchaseLore = ComponentMessage.componentMessage(loreFormat)
            .replace("{quantity}", bait.getPurchaseQuantity())
            .replace("{price}", economy.getWorthFormat(bait.getPurchasePrice(), false))
            .replace("{bait}", bait.getDisplayName());
        meta.lore(purchaseLore.get());
    }

    private boolean requireConfirmation(@NonNull UUID uuid) {
        if (!confirmation.hasCooldown(uuid)) {
            confirmation.applyCooldown(uuid, Duration.ofSeconds(5));
            return true;
        }
        confirmation.removeCooldown(uuid);
        return false;
    }

    @Override
    public void configure(@NonNull InventoryGui inventoryGui) {
        inventoryGui.addElements(getBaitsGroup(guiConfig.getConfig()));
    }

    @Override
    protected void addClickActions(@NonNull Map<String, ConfigGuiClickAction> map) {}

}
