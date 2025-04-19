package com.oheers.fish.gui.guis.journal;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.ConfigGui;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.utils.ItemFactory;
import com.oheers.fish.utils.ItemUtils;
import de.themoep.inventorygui.*;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FishJournalGui extends ConfigGui {

    private final Rarity rarity;

    public FishJournalGui(@NotNull HumanEntity player, @Nullable Rarity rarity) {
        super(
            GuiConfig.getInstance().getConfig().getSection(
                rarity == null ? "journal-menu" : "journal-rarity"
            ),
            player
        );

        this.rarity = rarity;

        createGui();

        Section config = getGuiConfig();
        if (config != null) {
            getGui().addElement(getGroup(config));
        }
    }

    private DynamicGuiElement getGroup(Section section) {
        return (this.rarity == null ? getRarityGroup(section) : getFishGroup(section));
    }

    private DynamicGuiElement getFishGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("fish-character", "f"), 'f');

        return new DynamicGuiElement(character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            this.rarity.getFishList().forEach(fish ->
                group.addElement(new StaticGuiElement(character, getFishItem(fish, section)))
            );
            return group;
        });
    }

    private ItemStack getFishItem(Fish fish, Section section) {
        Database database = EvenMoreFish.getInstance().getDatabase();

        boolean hideUndiscovered = section.getBoolean("hide-undiscovered-fish", true);
        // If undiscovered fish should be hidden
        if (hideUndiscovered && !database.userHasFish(fish, player)) {
            ItemFactory factory = new ItemFactory("undiscovered-fish", section);
            factory.enableAllChecks();
            return factory.createItem(null, -1);
        }

        ItemFactory factory = new ItemFactory("fish-item", section);
        factory.enableAllChecks();
        ItemStack item = factory.createItem(null, -1);

        item.editMeta(meta -> {
            // Display Name
            String displayStr = section.getString("fish-item.item.displayname");
            if (displayStr != null) {
                EMFSingleMessage display = EMFSingleMessage.fromString(displayStr);
                display.setVariable("{fishname}", fish.getDisplayName());
                meta.displayName(display.getComponentMessage());
            }

            // Lore
            LocalDateTime discover = database.getFirstCatchDateForPlayer(fish, player);
            String discoverDate = discover == null ? "Unknown" : discover.format(DateTimeFormatter.ISO_DATE);

            String discoverer = FishUtils.getPlayerName(database.getDiscoverer(fish));
            if (discoverer == null) {
                discoverer = "Unknown";
            }

            EMFListMessage lore = EMFListMessage.fromStringList(
                section.getStringList("fish-item.lore")
            );
            lore.setVariable("{times-caught}", Integer.toString(database.getAmountFishCaughtForPlayer(fish, player)));
            lore.setVariable("{largest-size}", database.getLargestFishSizeForPlayer(fish, player));
            lore.setVariable("{discover-date}", discoverDate);
            lore.setVariable("{discoverer}", discoverer);
            lore.setVariable("{server-largest}", database.getLargestFishSize(fish));
            lore.setVariable("{server-caught}", database.getAmountFishCaught(fish));
            meta.lore(lore.getComponentListMessage());
        });

        ItemUtils.changeMaterial(item, fish.getFactory().getMaterial());

        return item;
    }

    private DynamicGuiElement getRarityGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("rarity-character", "r"), 'r');

        return new DynamicGuiElement(character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            FishManager.getInstance().getRarityMap().values().forEach(rarity -> {
                ItemFactory factory = new ItemFactory("rarity-item", section);
                factory.enableAllChecks();
                ItemStack item = factory.createItem(null, -1);
                item = ItemUtils.changeMaterial(item, rarity.getMaterial());

                item.editMeta(meta -> {
                    Component originalDisplay = meta.displayName();
                    if (originalDisplay == null) {
                        return;
                    }
                    EMFSingleMessage display = EMFSingleMessage.of(originalDisplay);
                    display.setRarity(rarity.getDisplayName());
                    meta.displayName(display.getComponentMessage());
                });

                group.addElement(new StaticGuiElement(character, item, click -> {
                    click.getGui().close();
                    new FishJournalGui(player, rarity).open();
                    return true;
                }));
            });
            return group;
        });
    }

    @Override
    public void doRescue() {}

}
