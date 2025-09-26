package com.oheers.fish;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.oheers.fish.api.EMFAPI;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.requirement.RequirementType;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.commands.AdminCommand;
import com.oheers.fish.commands.MainCommand;
import com.oheers.fish.competition.AutoRunner;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionQueue;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.events.FishEatEvent;
import com.oheers.fish.events.FishInteractEvent;
import com.oheers.fish.events.McMMOTreasureEvent;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.fishing.rods.RodManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.plugin.ConfigurationManager;
import com.oheers.fish.plugin.DependencyManager;
import com.oheers.fish.plugin.EventManager;
import com.oheers.fish.plugin.IntegrationManager;
import com.oheers.fish.plugin.MetricsManager;
import com.oheers.fish.plugin.PluginDataManager;
import com.oheers.fish.update.UpdateChecker;
import de.themoep.inventorygui.InventoryGui;
import de.tr7zw.changeme.nbtapi.NBT;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.oheers.fish.FishUtils.classExists;

public class EvenMoreFish extends EMFPlugin {
    private final Random random = ThreadLocalRandom.current();

    private final NamespacedKey fishToggleKey = new NamespacedKey(this, "fish-disabled");

    // Do some fish in some rarities have the comp-check-exempt: true.
    private boolean raritiesCompCheckExempt = false;
    private CompetitionQueue competitionQueue;

    // this is for pre-deciding a rarity and running particles if it will be chosen
    // it's a work-in-progress solution and probably won't stick.
    private Map<UUID, Rarity> decidedRarities;
    private volatile boolean isUpdateAvailable;

    private DependencyManager dependencyManager;
    private ConfigurationManager configurationManager;
    private PluginDataManager pluginDataManager;
    private IntegrationManager integrationManager;
    private EventManager eventManager;
    private MetricsManager metricsManager;

    private static EvenMoreFish instance;
    private static TaskScheduler scheduler;
    private EMFAPI api;


    public static @NotNull EvenMoreFish getInstance() {
        return Objects.requireNonNull(instance, "Plugin not initialized yet!");
    }

    public static TaskScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void onLoad() {
        // Don't enable if the server is not using Paper.
        if (!isPaper()) {
            getLogger().severe("Spigot detected! EvenMoreFish no longer runs on Spigot, we recommend updating to Paper instead. https://papermc.io/downloads/paper");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!NBT.preloadApi()) {
            throw new RuntimeException("NBT-API wasn't initialized properly, disabling the plugin");
        }

        instance = this;

        CommandAPIBukkitConfig config = new CommandAPIBukkitConfig(this)
                .shouldHookPaperReload(true)
                .missingExecutorImplementationMessage("You are not able to use this command!");
        CommandAPI.onLoad(config);
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

        scheduler = UniversalScheduler.getScheduler(this);

        this.api = new EMFAPI();

        this.decidedRarities = new HashMap<>();

        this.configurationManager = new ConfigurationManager(this);
        this.configurationManager.loadConfigurations(); //need to test, order may be important

        this.dependencyManager = new DependencyManager(this);
        this.dependencyManager.checkDependencies(); // need to test, order may be important, if it is, we introduce multiple stages with events

        this.integrationManager = new IntegrationManager(this);
        this.integrationManager.loadAddons();

        // could not set up economy.
        if (!Economy.getInstance().isEnabled()) {
            getLogger().warning("EvenMoreFish won't be hooking into economy. If this wasn't by choice in config.yml, please install Economy handling plugins.");
            getLogger().warning("This message may be a false positive, if you can spot a \"successfully hooked into \" message, it can be ignored.");
        }

        this.pluginDataManager = new PluginDataManager(this);

        this.eventManager = new EventManager(this);
        this.eventManager.registerCoreListeners();
        this.eventManager.registerOptionalListeners();

        FishManager.getInstance().load();

        // Always load this after FishManager
        BaitManager.getInstance().load();

        // Always load this after BaitManager
        RodManager.getInstance().load();

        // Always load this after RodManager
        this.competitionQueue = new CompetitionQueue();
        this.competitionQueue.load();

        // check for updates on the Modrinth page
        new UpdateChecker(this).checkUpdate().thenAccept(available -> {
            isUpdateAvailable = available;
            if (available) {
                getLogger().warning("A new update is available! Download it from https://modrinth.com/plugin/evenmorefish");
            }
        });

        this.metricsManager = new MetricsManager(this);
        this.metricsManager.setupMetrics();

        AutoRunner.init();

        registerCommands();

        getLogger().info(() -> "EvenMoreFish by Oheers : Enabled");
    }

    @Override
    public void onDisable() {
        // If the server is not using Paper, the plugin won't have enabled in the first place.
        if (!isPaper()) {
            return;
        }

        CommandAPI.onDisable();

        terminateGuis();
        // Ends the current competition in case the plugin is being disabled when the server will continue running
        Competition active = Competition.getCurrentlyActive();
        if (active != null) {
            active.end(false);
        }
        
        // Don't use the scheduler here because it will throw errors on disable
        if (this.pluginDataManager != null) {
            this.pluginDataManager.shutdown();
        }

        RewardType.unregisterAll();
        RequirementType.unregisterAll();


        // Make sure this is in the reverse order of loading.
        this.competitionQueue.unload();
        RodManager.getInstance().unload();
        BaitManager.getInstance().unload();
        FishManager.getInstance().unload();

        getLogger().info(() -> "EvenMoreFish by Oheers : Disabled");
    }


    @Override
    public boolean isDebugSession() {
        return MainConfig.getInstance().debugSession();
    }

    // gets called on server shutdown to simulate all players closing their Guis
    private void terminateGuis() {
        getServer().getOnlinePlayers().forEach(player -> {
            InventoryGui inventoryGui = InventoryGui.getOpen(player);
            if (inventoryGui != null) {
                inventoryGui.close();
            }
        });
    }

    @Override
    public void reload(@Nullable CommandSender sender) {
        terminateGuis();

        this.configurationManager.reloadConfigurations();

        FishManager.getInstance().reload();
        BaitManager.getInstance().reload();
        RodManager.getInstance().reload();

        HandlerList.unregisterAll(FishInteractEvent.getInstance());
        HandlerList.unregisterAll(McMMOTreasureEvent.getInstance());

        this.eventManager.registerOptionalListeners();

        competitionQueue.reload();

        if (sender != null) {
            ConfigMessage.RELOAD_SUCCESS.getMessage().send(sender);
        }
        
    }

    private void registerCommands() {
        new MainCommand().getCommand().register(this);

        // Shortcut command for /emf admin
        if (MainConfig.getInstance().isAdminShortcutCommandEnabled()) {
            new AdminCommand(
                    MainConfig.getInstance().getAdminShortcutCommandName()
            ).getCommand().register(this);
        }
    }

    public Random getRandom() {
        return random;
    }


    public boolean isRaritiesCompCheckExempt() {
        return raritiesCompCheckExempt;
    }

    public void setRaritiesCompCheckExempt(boolean exempt) {
        this.raritiesCompCheckExempt = exempt;
    }

    public CompetitionQueue getCompetitionQueue() {
        return competitionQueue;
    }

    public Map<UUID, Rarity> getDecidedRarities() {
        return decidedRarities;
    }

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public EMFAPI getApi() {
        return api;
    }

    public List<Player> getVisibleOnlinePlayers() {
        return List.copyOf(Bukkit.getOnlinePlayers());
    }

    // FISH TOGGLE METHODS

    public void performFishToggle(@NotNull Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        // If custom fishing is disabled
        if (isCustomFishingDisabled(player)) {
            // Set fish-disabled to false
            pdc.set(fishToggleKey, PersistentDataType.BOOLEAN, false);
            ConfigMessage.TOGGLE_ON.getMessage().send(player);
        } else {
            // Set fish-disabled to true
            pdc.set(fishToggleKey, PersistentDataType.BOOLEAN, true);
            ConfigMessage.TOGGLE_OFF.getMessage().send(player);
        }
    }


    public boolean isCustomFishingDisabled(@NotNull Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc.getOrDefault(fishToggleKey, PersistentDataType.BOOLEAN, false);
    }

    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public PluginDataManager getPluginDataManager() {
        return pluginDataManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public MetricsManager getMetricsManager() {
        return metricsManager;
    }

    private boolean isPaper() {
        return classExists("com.destroystokyo.paper.PaperConfig")
                || classExists("io.papermc.paper.configuration.Configuration");
    }

}
