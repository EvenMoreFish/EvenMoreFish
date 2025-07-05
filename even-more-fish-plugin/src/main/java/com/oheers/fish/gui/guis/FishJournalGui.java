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
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.utils.Logging;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
        Database database = EvenMoreFish.getInstance().getPluginDataManager().getDatabase();

        if (database == null) {
            Logging.warn("Can not show fish in the Journal Menu, please enable the database!");
            ItemFactory factory = ItemFactory.itemFactory(section, "undiscovered-fish");
            return factory.createItem(player.getUniqueId());
        }

        boolean hideUndiscovered = section.getBoolean("hide-undiscovered-fish", true);
        // If undiscovered fish should be hidden
        if (hideUndiscovered && !database.userHasFish(fish, player)) {
            ItemFactory factory = ItemFactory.itemFactory(section, "undiscovered-fish");
            return factory.createItem(player.getUniqueId());
        }

        ItemStack item = fish.give();

        item.editMeta(meta -> {
            ItemFactory factory = ItemFactory.itemFactory(section, "fish-item");
            EMFSingleMessage display = prepareDisplay(factory, fish);
            if (display != null) {
                meta.displayName(display.getComponentMessage(player));
            }
            meta.lore(prepareLore(factory, fish).getComponentListMessage(player));
        });

        return item;
    }

    private @Nullable EMFSingleMessage prepareDisplay(@NotNull ItemFactory factory, @NotNull Fish fish) {
        final String displayStr = factory.getDisplayName().getConfiguredValue();
        if (displayStr == null) {
            return null;
        }
        EMFSingleMessage display = EMFSingleMessage.fromString(displayStr);
        display.setVariable("{fishname}", fish.getDisplayName());
        return display;
    }

    private @NotNull EMFListMessage prepareLore(@NotNull ItemFactory factory, @NotNull Fish fish) {
        final int userId = EvenMoreFish.getInstance().getPluginDataManager().getUserManager().getUserId(player.getUniqueId());

        final UserFishStats userFishStats = EvenMoreFish.getInstance().getPluginDataManager().getUserFishStatsDataManager().get(UserFishRarityKey.of(userId, fish).toString());
        final FishStats fishStats = EvenMoreFish.getInstance().getPluginDataManager().getFishStatsDataManager().get(FishRarityKey.of(fish).toString());

        final String discoverDate = getValueOrUnknown(() -> userFishStats.getFirstCatchTime().format(DateTimeFormatter.ISO_DATE));
        final String discoverer = getValueOrUnknown(() -> FishUtils.getPlayerName(fishStats.getDiscoverer()));

        EMFListMessage lore = EMFListMessage.fromStringList(
            factory.getLore().getConfiguredValue()
        );

        lore.setVariable("{times-caught}", getValueOrUnknown(() -> Integer.toString(userFishStats.getQuantity())));
        lore.setVariable("{largest-size}", getValueOrUnknown(() -> String.valueOf(userFishStats.getLongestLength())));
        lore.setVariable("{smallest-size}", getValueOrUnknown(() -> String.valueOf(userFishStats.getShortestLength())));
        lore.setVariable("{discover-date}", discoverDate);
        lore.setVariable("{discoverer}", discoverer);
        lore.setVariable("{server-largest}", getValueOrUnknown(() -> String.valueOf(fishStats.getLongestLength())));
        lore.setVariable("{server-smallest}", getValueOrUnknown(() -> String.valueOf(fishStats.getShortestLength())));
        lore.setVariable("{server-caught}", getValueOrUnknown(() -> String.valueOf(fishStats.getQuantity())));

        return lore;
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
                        new FishJournalGui(player, rarity).open();
                        return true;
                    })
                )
            );
            return group;
        }
        );
    }

    private ItemStack getRarityItem(Rarity rarity, Section section) {
        Database database = EvenMoreFish.getInstance().getPluginDataManager().getDatabase();
        boolean hideUndiscovered = section.getBoolean("hide-undiscovered-rarities", true);

        if (database == null) {
            Logging.warn("Can not show rarities in the Journal Menu, please enable the database!");
            ItemFactory factory = ItemFactory.itemFactory(section, "undiscovered-rarity");
            return factory.createItem(player.getUniqueId());
        }

        if (hideUndiscovered && !database.userHasRarity(rarity, player)) {
            ItemFactory factory = ItemFactory.itemFactory(section, "undiscovered-rarity");
            return factory.createItem(player.getUniqueId());
        }

        ItemStack rarityItem = rarity.getMaterial();

        final ItemFactory factory = ItemFactory.itemFactory(section, "rarity-item");
        ItemStack configuredItem = factory.createItem(player.getUniqueId());

        // Carry the configured item's lore and display name to the rarity item
        ItemMeta configuredMeta = configuredItem.getItemMeta();
        if (configuredMeta != null) {
            rarityItem.editMeta(meta -> {
                Component configuredDisplay = configuredMeta.displayName();
                if (configuredDisplay != null) {
                    EMFSingleMessage display = EMFSingleMessage.of(configuredDisplay);
                    display.setRarity(rarity.getDisplayName());
                    meta.displayName(display.getComponentMessage(player));
                }
                meta.lore(configuredMeta.lore());
                if (configuredMeta.hasCustomModelData()) {
                    meta.setCustomModelData(configuredMeta.getCustomModelData());
                }
            });
        }

        return rarityItem;
    }

    @Override
    public void doRescue() {}

}
