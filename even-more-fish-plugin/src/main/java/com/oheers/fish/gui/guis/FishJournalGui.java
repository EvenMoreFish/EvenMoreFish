package com.oheers.fish.gui.guis;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.gui.impl.JournalFishGuiConfig;
import com.oheers.fish.config.gui.impl.JournalRaritiesGuiConfig;
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
import com.oheers.fish.utils.sort.SortType;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

public class FishJournalGui extends ConfigGui {
    private final int userId;
    private final Rarity rarity;
    private final SortType sortType;
    private final boolean usePreloadedStatsOnly;

    public static void openAsync(@NotNull Player player, @Nullable Rarity rarity) {
        openAsync(player, rarity, null);
    }

    public static void openAsync(@NotNull Player player, @Nullable Rarity rarity, @Nullable InventoryGui expectedOpenGui) {
        EvenMoreFish plugin = EvenMoreFish.getInstance();
        plugin.debug("Preparing fish journal for %s.".formatted(player.getName()));
        plugin.getPluginDataManager().preloadUserDataAsync(player.getUniqueId()).whenComplete((userId, throwable) -> {
            if (throwable != null) {
                Logging.warn("Could not prepare fish journal for " + player.getName() + ".", throwable);
                return;
            }
            plugin.debug("Fish journal data prepared for %s with user id %d.".formatted(player.getName(), userId));
            EvenMoreFish.getScheduler().runTask(player, () -> {
                try {
                    if (!player.isOnline()) {
                        plugin.debug("Skipping fish journal open for %s because the player is offline.".formatted(player.getName()));
                        return;
                    }
                    if (expectedOpenGui != null && InventoryGui.getOpen(player) != expectedOpenGui) {
                        plugin.debug("Skipping fish journal open for %s because the open GUI changed while data loaded.".formatted(player.getName()));
                        return;
                    }
                    new FishJournalGui(player, rarity, userId, true).open();
                } catch (Exception exception) {
                    Logging.warn("Could not open fish journal for " + player.getName() + ".", exception);
                }
            });
        });
    }

    public FishJournalGui(@NotNull HumanEntity player, @Nullable Rarity rarity) {
        this(
            player,
            rarity,
            EvenMoreFish.getInstance().getPluginDataManager().getUserManager().getUserId(player.getUniqueId()),
            false
        );
    }

    private FishJournalGui(@NotNull HumanEntity player, @Nullable Rarity rarity, int userId, boolean usePreloadedStatsOnly) {
        super(
            (rarity == null)
                ? JournalRaritiesGuiConfig.getInstance()
                : JournalFishGuiConfig.getInstance(),
            player
        );

        this.rarity = rarity;
        if (rarity != null) {
            addReplacement("{rarity}", rarity.getDisplayName());
        }

        this.userId = userId;
        this.usePreloadedStatsOnly = usePreloadedStatsOnly;
        createGui();

        Section config = getGuiConfig();
        if (config != null) {
            sortType = FishUtils.getEnumValue(
                SortType.class,
                config.getString("sort-type"),
                SortType.ALPHABETICAL
            );
            getGui().addElement(getGroup(config));
        } else {
            sortType = SortType.ALPHABETICAL;
        }
    }

    private GuiElement getGroup(Section section) {
        return (rarity == null) ? getRarityGroup(section) : getFishGroup(section);
    }

    private GuiElement getFishGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("fish-character"), 'f');

        GuiElementGroup group = new GuiElementGroup(character);
        sortType.sort(this.rarity.getFishList()).forEach(fish -> {
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
        if (hideUndiscovered && !userHasFish(database, fish)) {
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

    /**
     * Answers "has this user caught this fish" from the preloaded cache when
     * possible, so opening the journal does not run one blocking query per
     * fish on the server thread.
     */
    private boolean userHasFish(@NotNull Database database, @NotNull Fish fish) {
        final var dataManager = EvenMoreFish.getInstance().getPluginDataManager();
        if (usePreloadedStatsOnly || dataManager.isUserFishStatsPreloaded(userId)) {
            return dataManager.getUserFishStatsDataManager().peek(UserFishRarityKey.of(userId, fish).toString()) != null;
        }
        return database.userHasFish(fish.getRarity().getId(), fish.getName(), userId);
    }

    private @NotNull EMFListMessage prepareLore(@NotNull ItemFactory factory, @NotNull Fish fish) {
        final var dataManager = EvenMoreFish.getInstance().getPluginDataManager();
        // When the caches were preloaded, a miss means "no row exists" and
        // falling through to the blocking loader would query the database
        // once per fish while the GUI builds on the server thread.
        final UserFishStats userFishStats = (usePreloadedStatsOnly || dataManager.isUserFishStatsPreloaded(userId))
            ? dataManager.getUserFishStatsDataManager().peek(UserFishRarityKey.of(userId, fish).toString())
            : dataManager.getUserFishStatsDataManager().get(UserFishRarityKey.of(userId, fish).toString());
        final FishStats fishStats = dataManager.isFishStatsPreloaded()
            ? dataManager.getFishStatsDataManager().peek(FishRarityKey.of(fish).toString())
            : dataManager.getFishStatsDataManager().get(FishRarityKey.of(fish).toString());

        final String discoverDate = getDiscoverDate(userFishStats, getUnknownMessage());

        @SuppressWarnings("Convert2MethodRef") // Suppressed as it introduces an unwanted Objects#requireNonNull when compiled.
        final String discoverer = getDiscoverer(fishStats, getUnknownMessage());

        EMFListMessage lore = EMFListMessage.ofList(
            Optional.ofNullable(factory.getLore().getConfiguredValue())
                .orElse(Collections.emptyList())
        );

        lore.setVariable("{times-caught}", getValueOrDefault(() -> userFishStats == null ? null : Integer.toString(userFishStats.getQuantity()), "0"));
        lore.setVariable("{largest-size}", getValueOrDefault(() -> userFishStats == null ? null : String.valueOf(userFishStats.getLongestLength()), "0"));
        lore.setVariable("{smallest-size}", getValueOrDefault(() -> userFishStats == null ? null : String.valueOf(userFishStats.getShortestLength()), "0"));
        lore.setVariable("{discover-date}", discoverDate);
        lore.setVariable("{discoverer}", discoverer);
        lore.setVariable("{server-largest}", getValueOrDefault(() -> fishStats == null ? null : String.valueOf(fishStats.getLongestLength()), "0"));
        lore.setVariable("{server-smallest}", getValueOrDefault(() -> fishStats == null ? null : String.valueOf(fishStats.getShortestLength()), "0"));
        lore.setVariable("{server-caught}", getValueOrDefault(() -> fishStats == null ? null : String.valueOf(fishStats.getQuantity()), "0"));

        return lore;
    }

    static @NotNull String getDiscoverDate(@Nullable UserFishStats userFishStats, @NotNull String unknownMessage) {
        return userFishStats == null ? unknownMessage : userFishStats.getFirstCatchTime().format(DateTimeFormatter.ISO_DATE);
    }

    static @NotNull String getDiscoverer(@Nullable FishStats fishStats, @NotNull String unknownMessage) {
        return fishStats == null ? unknownMessage : Optional.ofNullable(fishStats.getDiscovererName()).orElse(unknownMessage);
    }

    private @NotNull String getValueOrDefault(@NotNull Supplier<String> supplier, @NotNull String def) {
        try {
            return Optional.ofNullable(supplier.get()).orElse(def);
        } catch (Exception exception) {
            EvenMoreFish.getInstance().debug(
                "An exception occurred while getting a value. Defaulting to " + def,
                exception
            );
            return def;
        }
    }


    private GuiElement getRarityGroup(Section section) {
        char character = FishUtils.getCharFromString(section.getString("rarity-character"), 'r');

        GuiElementGroup group = new GuiElementGroup(character);
        sortType.sort(FishManager.getInstance().getRarityMap().values()).forEach(rarity -> {
            if (!rarity.getShowInJournal()) {
                return;
            }
            ItemStack item = getRarityItem(rarity, section);
            if (item.isEmpty()) {
                return;
            }
            group.addElement(
                new StaticGuiElement(character, item, click -> {
                    FishJournalGui.openAsync(player, rarity, click.getGui());
                    return true;
                })
            );
        });
        return group;
    }

    private ItemStack getRarityItem(Rarity rarity, Section section) {
        final Database database = requireDatabase("Can not show rarities in the Journal Menu, please enable the database!");

        if (database == null) {
            return ItemFactory.itemFactory(section, "undiscovered-rarity").createItem(player.getUniqueId());
        }

        boolean hideUndiscovered = section.getBoolean("hide-undiscovered-rarity", true);
        if (hideUndiscovered && !userHasRarity(database, rarity)) {
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

    private boolean userHasRarity(@NotNull Database database, @NotNull Rarity rarity) {
        final var dataManager = EvenMoreFish.getInstance().getPluginDataManager();
        if (usePreloadedStatsOnly || dataManager.isUserFishStatsPreloaded(userId)) {
            return rarity.getFishList().stream()
                .anyMatch(fish -> dataManager.getUserFishStatsDataManager().peek(UserFishRarityKey.of(userId, fish).toString()) != null);
        }
        return database.userHasRarity(rarity.getId(), userId);
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

}
