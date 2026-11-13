package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.api.economy.selling.SellHelper;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.commands.main.MainCommand;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.economy.GriefPreventionEconomyType;
import com.oheers.fish.economy.PlayerPointsEconomyType;
import com.oheers.fish.economy.VaultEconomyType;
import com.oheers.fish.events.AuraSkillsFishingEvent;
import com.oheers.fish.events.DeprecatedEventListener;
import com.oheers.fish.events.McMMOTreasureEvent;
import com.oheers.fish.gui.EMFConfigGui;
import com.oheers.fish.gui.guis.BaitsGui;
import com.oheers.fish.gui.guis.FishJournalGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.placeholders.PlaceholderReceiver;
import com.oheers.fish.utils.HeadDBIntegration;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.config.ConfigGui;
import de.themoep.inventorygui.config.GlobalConfigActions;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.firedev.daisylib.DaisyLib;
import uk.firedev.daisylib.messages.MessageSettings;
import uk.firedev.daisylib.messages.ObjectProcessor;

import java.util.List;

public class DependencyManager implements Listener {
    private final EvenMoreFish plugin;
    private HeadDatabaseAPI hdbapi;

    // Dependency flags
    private boolean usingPAPI;
    private boolean usingMcMMO;
    private boolean usingHeadsDB;
    private boolean usingPlayerPoints;
    private boolean usingGriefPrevention;
    private boolean usingAuraSkills;
    private boolean usingExcellentEconomy;

    public DependencyManager(EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    public void loadBundledDependencies() {
        DaisyLib.Settings.ALLOW_LEGACY_MESSAGES = () -> true;
        MessageSettings.setAllowEmptyAppend(false);
        MessageSettings.setAllowEmptyPrepend(false);

        ObjectProcessor.registerProcessor(
            EMFSingleMessage.class,
            EMFSingleMessage::getComponentListMessage
        );
        ObjectProcessor.registerProcessor(
            EMFListMessage.class,
            EMFListMessage::getComponentListMessage
        );
        ObjectProcessor.registerProcessor(
            IRarity.class,
            rarity -> List.of(rarity.getDisplayName())
        );
        ObjectProcessor.registerProcessor(
            IFish.class,
            fish -> List.of(fish.getDisplayName())
        );
        configureInventoryGui();
    }

    public void checkDependencies() {
        PluginManager pm = Bukkit.getPluginManager();

        this.usingGriefPrevention = pm.isPluginEnabled("GriefPrevention");
        this.usingPlayerPoints = pm.isPluginEnabled("PlayerPoints");
        this.usingMcMMO = pm.isPluginEnabled("mcMMO");
        this.usingHeadsDB = pm.isPluginEnabled("HeadDatabase") && FishUtils.classExists("me.arcaniax.hdb.api.HeadDatabaseAPI");
        this.usingPAPI = pm.isPluginEnabled("PlaceholderAPI");
        this.usingAuraSkills = pm.isPluginEnabled("AuraSkills");
        this.usingExcellentEconomy = pm.isPluginEnabled("ExcellentEconomy");

        loadVaultEconomy();
        loadPlayerPointsEconomy();
        loadGriefPreventionEconomy();
        checkPapi();

        // Handle deprecated events.
        pm.registerEvents(new DeprecatedEventListener(), plugin);
    }

    public void checkOptionalDependencies() {
        PluginManager pm = plugin.getServer().getPluginManager();
        if (usingMcMMO && MainConfig.getInstance().disableMcMMOTreasure()) {
            pm.registerEvents(McMMOTreasureEvent.getInstance(), plugin);
        }


        if (usingHeadsDB) {
            pm.registerEvents(new HeadDBIntegration(), plugin);
        }

        if (usingAuraSkills && MainConfig.getInstance().disableAuraSkills()) {
            pm.registerEvents(new AuraSkillsFishingEvent(), plugin);
        }
    }

    public boolean isUsingPAPI() {
        return usingPAPI;
    }

    public boolean isUsingMcMMO() {
        return usingMcMMO;
    }

    public boolean isUsingHeadsDB() {
        return usingHeadsDB;
    }

    public boolean isUsingPlayerPoints() {
        return usingPlayerPoints;
    }

    public boolean isUsingGriefPrevention() {
        return usingGriefPrevention;
    }

    public boolean isUsingExcellentEconomy() {
        return usingExcellentEconomy;
    }

    public @Nullable HeadDatabaseAPI getHdbapi() {
        return hdbapi;
    }

    public boolean isUsingAuraSkills() {
        return usingAuraSkills;
    }

    public boolean isHeadsDBLoaded() {
        return usingHeadsDB && hdbapi != null;
    }

    public void loadVaultEconomy() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            loadEconomyType(new VaultEconomyType(), "Vault");
        }
    }

    public void loadPlayerPointsEconomy() {
        if (isUsingPlayerPoints()) {
            loadEconomyType(new PlayerPointsEconomyType(), "PlayerPoints");
        }
    }

    public void loadGriefPreventionEconomy() {
        if (isUsingGriefPrevention()) {
            loadEconomyType(new GriefPreventionEconomyType(), "GriefPrevention");
        }
    }

    private void loadEconomyType(@NonNull EconomyType type, @NonNull String name) {
        type.load();
        if (type.register()) {
            EvenMoreFish.getInstance().getLogger().info("EvenMoreFish has successfully hooked into " + name + ".");
        }
    }

    private void checkPapi() {
        if (isUsingPAPI()) {
            usingPAPI = true;
            new PlaceholderReceiver(plugin).register();
        }
    }

    public void setHdbapi(HeadDatabaseAPI hdbapi) {
        this.hdbapi = hdbapi;
    }

    private void configureInventoryGui() {
        GlobalConfigActions.register("full-exit", (configGui, inventoryGui, click) -> {
            doRescue(configGui, inventoryGui);
            click.getWhoClicked().closeInventory();
            return true;
        });
        // Exiting a sub-menu should open the main menu
        GlobalConfigActions.register("open-main-menu", (configGui, inventoryGui, click) -> {
            doRescue(configGui, inventoryGui);
            new MainMenuGui(click.getWhoClicked()).open();
            return true;
        });

        // Toggling custom fish should redraw the GUI and leave it at that
        GlobalConfigActions.register("fish-toggle", (configGui, inventoryGui, click) -> {
            EvenMoreFish.getInstance().getToggle().performFishToggle((Player) click.getWhoClicked());
            inventoryGui.draw();
            return true;
        });

        // The shop action should just open the shop menu
        GlobalConfigActions.register("open-shop", (configGui, inventoryGui, click) -> {
            doRescue(configGui, inventoryGui);
            new SellGui((Player) click.getWhoClicked(), SellGui.SellState.NORMAL, null).open();
            return true;
        });

        GlobalConfigActions.register("show-command-help", (configGui, inventoryGui, click) -> {
            click.getWhoClicked().closeInventory();
            MainCommand.sendHelpMessage(click.getWhoClicked());
            return true;
        });

        GlobalConfigActions.register("sell-inventory", (configGui, inventoryGui, click) -> {
            Player player = (Player) click.getWhoClicked();
            if (configGui instanceof SellGui sellGui) {
                new SellGui(player, SellGui.SellState.CONFIRM, sellGui.getFishInventory()).open();
                return true;
            }
            SellHelper.get().sell(player.getInventory(), player);
            player.closeInventory();
            return true;
        });

        GlobalConfigActions.register("sell-shop", (configGui, inventoryGui, click) -> {
            Player player = (Player) click.getWhoClicked();
            if (configGui instanceof SellGui sellGui) {
                new SellGui(player, SellGui.SellState.CONFIRM, sellGui.getFishInventory()).open();
                return true;
            }

            FishUtils.sellInventoryGui(inventoryGui, player);
            player.closeInventory();
            return true;
        });

        GlobalConfigActions.register("sell-inventory-confirm", (configGui, inventoryGui, click) -> {
            Player player = (Player) click.getWhoClicked();
            SellHelper.get().sell(player.getInventory(), player);
            doRescue(configGui, inventoryGui);
            player.closeInventory();
            return true;
        });

        GlobalConfigActions.register("sell-shop-confirm", (configGui, inventoryGui, click) -> {
            Player player = (Player) click.getWhoClicked();
            FishUtils.sellInventoryGui(inventoryGui, player);
            doRescue(configGui, inventoryGui);
            player.closeInventory();
            return true;
        });

        GlobalConfigActions.register("open-baits-menu", (configGui, inventoryGui, click) -> {
            doRescue(configGui, inventoryGui);
            new BaitsGui(click.getWhoClicked()).open();
            return true;
        });

        GlobalConfigActions.register("open-journal-menu", (configGui, inventoryGui, click) -> {
            if (!DatabaseUtil.isDatabaseOnline()) {
                ConfigMessage.JOURNAL_DISABLED.getMessage().send(click.getWhoClicked());
                return true;
            }
            doRescue(configGui, inventoryGui);
            FishJournalGui.openAsync((Player) click.getWhoClicked());
            return true;
        });
    }

    private void doRescue(ConfigGui<?> configGui, InventoryGui gui) {
        if (configGui instanceof EMFConfigGui custom) {
            custom.doRescue(gui);
        }
    }

}
