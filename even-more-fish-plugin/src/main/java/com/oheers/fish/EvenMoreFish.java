package com.oheers.fish;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.oheers.fish.api.EMFAPI;
import com.oheers.fish.api.baits.AbstractBaitManager;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.events.EMFPluginReloadEvent;
import com.oheers.fish.api.fishing.items.AbstractFishManager;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.competition.AutoRunner;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionQueue;
import com.oheers.fish.config.MainConfig;
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

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.vanishchecker.VanishChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class EvenMoreFish extends EMFPlugin {
    private final Random random = ThreadLocalRandom.current();
    private final Toggle toggle;

    // Do some fish in some rarities have the comp-check-exempt: true.
    private boolean raritiesCompCheckExempt = false;
    private CompetitionQueue competitionQueue;
    private final AutoRunner autoRunner = new AutoRunner();

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

    public EvenMoreFish() {
        this.toggle = new Toggle(this);
    }

    @Override
    public void onLoad() {
        if (!NBT.preloadApi()) {
            throw new RuntimeException("NBT-API wasn't initialized properly, disabling the plugin");
        }
        instance = this;
        loadCommands();
    }

    /**
     * Stuff to do onLoad() with commands
     */
    public abstract void loadCommands();

    public abstract void enableCommands();

    public abstract void registerCommands();

    @Override
    public void onEnable() {
        enableCommands();

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

        autoRunner.start();

        registerCommands();

        getLogger().info(() -> "EvenMoreFish by Oheers : Enabled");
    }

    public abstract void disableCommands();

    @Override
    public void onDisable() {
        // Do this first.
        autoRunner.stop();

        disableCommands();

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

        // Make sure this is in the reverse order of loading.
        this.competitionQueue.unload();
        RodManager.getInstance().unload();
        BaitManager.getInstance().unload();
        FishManager.getInstance().unload();

        getLogger().info(() -> "EvenMoreFish by Oheers : Disabled");
    }


    @Override
    public boolean isDebugSession() {
        return MainConfig.getInstance().shouldDebug();
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

        HandlerList.unregisterAll(McMMOTreasureEvent.getInstance());

        this.eventManager.registerOptionalListeners();

        competitionQueue.reload();

        // Refresh global economy instance with any new EconomyTypes that may have been registered.
        Economy.getInstance().setEconomyTypes(EMFRegistry.ECONOMY_TYPE.getRegistry().values());
        
        if (sender != null) {
            ConfigMessage.RELOAD_SUCCESS.getMessage().send(sender);
        }

        // This event is not cancellable.
        new EMFPluginReloadEvent().callEvent();
    }

    public Random getRandom() {
        return random;
    }

    public Toggle getToggle() {
        return toggle;
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

    public AutoRunner getAutoRunner() {
        return autoRunner;
    }

    public Map<UUID, Rarity> getDecidedRarities() {
        return decidedRarities;
    }

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    /**
     * @deprecated The methods this class provided can now be found in {@link AbstractFishManager} and {@link AbstractBaitManager}.
     */
    @Deprecated(forRemoval = true)
    public EMFAPI getApi() {
        return api;
    }

    public List<Player> getVisibleOnlinePlayers() {
        if (MainConfig.getInstance().shouldRespectVanish()) {
            return VanishChecker.getVisibleOnlinePlayers();
        }
        return List.copyOf(Bukkit.getOnlinePlayers());
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

}
