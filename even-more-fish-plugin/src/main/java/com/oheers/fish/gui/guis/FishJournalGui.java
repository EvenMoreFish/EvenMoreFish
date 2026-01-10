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
import com.oheers.fish.api.Logging;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

public class FishJournalGui extends ConfigGui {

    private final Rarity rarity;
    private final SortType sortType;

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
            sortType = SortType.fromString(config.getString("sort-type"));
        } else {
            sortType = SortType.ALPHABETICAL;
        }
    }

    private DynamicGuiElement getGroup(Section section) {
        return (rarity == null) ? getRarityGroup(section) : getFishGroup(section);
    }

    private DynamicGuiElement getFishGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("fish-character"), 'f');

        return new DynamicGuiElement(
                character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            sortType.sortFish(this.rarity.getFishList()).forEach(fish -> {
                if (!fish.getShowInJournal()) {
                    return;
                }
                ItemStack item = getFishItem(fish, section);
                if (item.isEmpty()) {
                    return;
                }
                group.addElement(new StaticGuiElement(character, item));
            });
            return group;
        }
        );
    }

    private @NotNull String getUnknownMessage() {
        return getGuiConfig().getString("unknown-message", "Unknown");
    }

    private ItemStack getFishItem(Fish fish, Section section) {
        final Database database = requireDatabase("Can not show fish in the Journal Menu, please enable the database!");

        if (database == null) {
            return ItemFactory.itemFactory(section, "undiscovered-fish").createItem(player.getUniqueId());
        }

        boolean hideUndiscovered = section.getBoolean("hide-undiscovered-fish", true);
        // If undiscovered fish should be hidden
        if (hideUndiscovered && !database.userHasFish(fish, player)) {
            return ItemFactory.itemFactory(section, "undiscovered-fish").createItem(player.getUniqueId());
        }

        final ItemStack item = fish.give();

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

        final String discoverDate = getValueOrDefault(() -> userFishStats.getFirstCatchTime().format(DateTimeFormatter.ISO_DATE), getUnknownMessage());
        final String discoverer = getValueOrDefault(() -> FishUtils.getPlayerName(fishStats.getDiscoverer()), getUnknownMessage());

        EMFListMessage lore = EMFListMessage.fromStringList(
                Optional.ofNullable(factory.getLore().getConfiguredValue())
                        .orElse(Collections.emptyList())
        );

        lore.setVariable("{times-caught}", getValueOrDefault(() -> Integer.toString(userFishStats.getQuantity()), "0"));
        lore.setVariable("{largest-size}", getValueOrDefault(() -> String.valueOf(userFishStats.getLongestLength()), "0"));
        lore.setVariable("{smallest-size}", getValueOrDefault(() -> String.valueOf(userFishStats.getShortestLength()), "0"));
        lore.setVariable("{discover-date}", discoverDate);
        lore.setVariable("{discoverer}", discoverer);
        lore.setVariable("{server-largest}", getValueOrDefault(() -> String.valueOf(fishStats.getLongestLength()), "0"));
        lore.setVariable("{server-smallest}", getValueOrDefault(() -> String.valueOf(fishStats.getShortestLength()), "0"));
        lore.setVariable("{server-caught}", getValueOrDefault(() -> String.valueOf(fishStats.getQuantity()), "0"));

        return lore;
    }

    private @NotNull String getValueOrDefault(@NotNull Supplier<String> supplier, @NotNull String def) {
        try {
            return Optional.ofNullable(supplier.get()).orElse(def);
        } catch (Exception exception) {
            EvenMoreFish.getInstance().debug(
                "An exception occurred while getting a value. Defaulting to " + def,
                new RuntimeException()
            );
            return def;
        }
    }


    private DynamicGuiElement getRarityGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("rarity-character"), 'r');

        return new DynamicGuiElement(
            character, who -> {
            GuiElementGroup group = new GuiElementGroup(character);
            sortType.sortRarities(FishManager.getInstance().getRarityMap().values()).forEach(rarity -> {
                if (!rarity.getShowInJournal()) {
                    return;
                }
                ItemStack item = getRarityItem(rarity, section);
                if (item.isEmpty()) {
                    return;
                }
                group.addElement(
                    new StaticGuiElement(
                        character, item, click -> {
                        new FishJournalGui(player, rarity).open();
                        return true;
                    })
                );
            });
            return group;
        }
        );
    }

    private ItemStack getRarityItem(Rarity rarity, Section section) {
        final Database database = requireDatabase("Can not show rarities in the Journal Menu, please enable the database!");

        if (database == null) {
            return ItemFactory.itemFactory(section, "undiscovered-rarity").createItem(player.getUniqueId());
        }

        boolean hideUndiscovered = section.getBoolean("hide-undiscovered-rarity", true);
        if (hideUndiscovered && !database.userHasRarity(rarity, player)) {
            return ItemFactory.itemFactory(section, "undiscovered-rarity").createItem(player.getUniqueId());
        }

        final ItemStack rarityItem = rarity.getJournalItem();
        final ItemStack configuredItem = ItemFactory.itemFactory(section, "rarity-item").createItem(player.getUniqueId());

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
    public void doRescue() { /* Don't rescue, view only */ }


    private @Nullable Database requireDatabase(String logMessage) {
        Database db = EvenMoreFish.getInstance().getPluginDataManager().getDatabase();
        if (db == null) {
            Logging.warn(logMessage);
        }
        return db;
    }

    public enum SortType {
        ALPHABETICAL(Comparator.comparing(Rarity::getId), Comparator.comparing(Fish::getName)),
        WEIGHT(Comparator.comparingDouble(Rarity::getWeight).reversed(), Comparator.comparingDouble(Fish::getWeight).reversed());

        private final Comparator<Rarity> rarityComparator;
        private final Comparator<Fish> fishComparator;

        SortType(Comparator<Rarity> rarityComparator, Comparator<Fish> fishComparator) {
            this.rarityComparator = rarityComparator;
            this.fishComparator = fishComparator;
        }

        public TreeSet<Rarity> sortRarities(@NotNull Collection<Rarity> collection) {
            TreeSet<Rarity> set = new TreeSet<>(rarityComparator);
            set.addAll(collection);
            return set;
        }

        public TreeSet<Fish> sortFish(@NotNull Collection<Fish> collection) {
            TreeSet<Fish> set = new TreeSet<>(fishComparator);
            set.addAll(collection);
            return set;
        }

        public static SortType fromString(@Nullable String string) {
            if (string == null) {
                return ALPHABETICAL;
            }
            try {
                return valueOf(string.toUpperCase());
            } catch (IllegalArgumentException exception) {
                return ALPHABETICAL;
            }
        }

    }

}
