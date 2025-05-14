package com.oheers.fish.gui.guis;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.data.UserFishRarityKey;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
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
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

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
        if (this.rarity == null) {
            return getRarityGroup(section);
        } else {
            return getFishGroup(section);
        }
    }

    private DynamicGuiElement getFishGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("fish-character", "f"), 'f');

        return new DynamicGuiElement(
                character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            this.rarity.getFishList().forEach(fish ->
                    group.addElement(new StaticGuiElement(character, getFishItem(fish, section)))
            );
            return group;
        }
        );
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
            final String displayStr = section.getString("fish-item.item.displayname");
            if (displayStr != null) {
                EMFSingleMessage display = EMFSingleMessage.fromString(displayStr);
                display.setVariable("{fishname}", fish.getDisplayName());
                meta.displayName(display.getComponentMessage());
            }

            final int userId = EvenMoreFish.getInstance().getUserManager().getUserId(player.getUniqueId());

            final UserFishStats userFishStats = EvenMoreFish.getInstance().getUserFishStatsDataManager().get(UserFishRarityKey.of(userId, fish).toString());
            final FishStats fishStats = EvenMoreFish.getInstance().getFishStatsDataManager().get(FishRarityKey.of(fish).toString());

            final String discoverDate = getValueOrUnknown(() -> userFishStats.getFirstCatchTime().format(DateTimeFormatter.ISO_DATE));
            final String discoverer = getValueOrUnknown(() -> FishUtils.getPlayerName(fishStats.getDiscoverer()));

            EMFListMessage lore = EMFListMessage.fromStringList(
                    section.getStringList("fish-item.lore")
            );

            lore.setVariable("{times-caught}", getValueOrUnknown(() -> Integer.toString(userFishStats.getQuantity())));
            lore.setVariable("{largest-size}", getValueOrUnknown(() -> String.valueOf(userFishStats.getLongestLength())));
            lore.setVariable("{smallest-size}", getValueOrUnknown(() -> String.valueOf(userFishStats.getShortestLength())));
            lore.setVariable("{discover-date}", discoverDate);
            lore.setVariable("{discoverer}", discoverer);
            lore.setVariable("{server-largest}", getValueOrUnknown(() -> String.valueOf(fishStats.getLongestLength())));
            lore.setVariable("{server-smallest}", getValueOrUnknown(() -> String.valueOf(fishStats.getShortestLength())));
            lore.setVariable("{server-caught}", getValueOrUnknown(() -> String.valueOf(fishStats.getQuantity())));
            meta.lore(lore.getComponentListMessage());
        });

        ItemUtils.changeMaterial(item, fish.getFactory().getMaterial());

        return item;
    }

    @NotNull
    private String getValueOrUnknown(Supplier<String> supplier) {
        try {
            String value = supplier.get();
            return (value == null) ? "Unknown" : value;
        } catch (NullPointerException e) {
            return "Unknown";
        }
    }


    private DynamicGuiElement getRarityGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("rarity-character", "r"), 'r');

        return new DynamicGuiElement(
                character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            FishManager.getInstance().getRarityMap().values().forEach(rarity ->
                    group.addElement(
                            new StaticGuiElement(
                                    character, getRarityItem(rarity, section), click -> {
                                click.getGui().close();
                                new FishJournalGui(player, rarity).open();
                                return true;
                            }
                            ))
            );
            return group;
        }
        );
    }

    private ItemStack getRarityItem(Rarity rarity, Section section) {
        Database database = EvenMoreFish.getInstance().getDatabase();
        boolean hideUndiscovered = section.getBoolean("hide-undiscovered-rarities", true);

        if (hideUndiscovered && !database.userHasRarity(rarity, player)) {
            ItemFactory factory = new ItemFactory("undiscovered-rarity", section);
            factory.enableAllChecks();
            return factory.createItem(player, -1);
        }

        final ItemFactory factory = new ItemFactory("rarity-item", section);
        factory.enableAllChecks();
        ItemStack item = factory.createItem(null, -1);
        item = ItemUtils.changeMaterial(item, rarity.getMaterial());

        item.editMeta(meta -> {
            Component originalDisplay = meta.displayName();
            if (originalDisplay != null) {
                EMFSingleMessage display = EMFSingleMessage.of(originalDisplay);
                display.setRarity(rarity.getDisplayName());
                meta.displayName(display.getComponentMessage());
            }
        });

        return item;
    }

    @Override
    public void doRescue() {}

}
