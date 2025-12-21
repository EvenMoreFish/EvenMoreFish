package com.oheers.fish.gui.guis;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.gui.ConfigGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.utils.CooldownHelper;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;
import uk.firedev.messagelib.message.ComponentListMessage;
import uk.firedev.messagelib.message.ComponentMessage;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class BaitsGui extends ConfigGui {

    private final CooldownHelper confirmation = CooldownHelper.create();
    private final CooldownHelper cooldown = CooldownHelper.create();

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
            BaitManager.getInstance().getItemMap().values()
                .forEach(bait -> group.addElement(createBaitElement(character, bait)));
            return group;
        });
    }

    private StaticGuiElement createBaitElement(char character, @NotNull BaitHandler bait) {
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
        return getGuiConfig().getStringList("purchase-lore");
    }

    private ItemStack createBaitItem(@NotNull BaitHandler bait) {
        ItemStack item = bait.create(player);
        item.editMeta(meta -> applyLore(meta, bait));
        return item;
    }

    private void applyLore(@NotNull ItemMeta meta, @NotNull BaitHandler bait) {
        List<String> loreFormat = getPurchaseLoreFormat();
        Economy economy = bait.getEconomy();
        if (loreFormat.isEmpty() || economy == null) {
            return;
        }
        ComponentListMessage purchaseLore = ComponentMessage.componentMessage(loreFormat)
            .replace("{quantity}", bait.getPurchaseQuantity())
            .replace("{price}", economy.getWorthFormat(bait.getPurchasePrice(), false))
            .replace("{bait}", bait.getDisplayName());
        meta.lore(purchaseLore.get());
    }

    private boolean requireConfirmation(@NotNull UUID uuid) {
        if (!confirmation.hasCooldown(uuid)) {
            confirmation.applyCooldown(uuid, Duration.ofSeconds(5));
            return true;
        }
        confirmation.removeCooldown(uuid);
        return false;
    }

}
