package com.oheers.fish;

import com.comphenix.protocol.utility.MinecraftVersion;
import com.devskiller.friendly_id.FriendlyId;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.oheers.fish.api.EMFAPI;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.baits.AbstractBaitManager;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.economy.selling.SoldFish;
import com.oheers.fish.api.events.EMFPluginReloadEvent;
import com.oheers.fish.api.fishing.items.AbstractFishManager;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.config.DimensionFishingConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.manager.DataManager;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.plugin.loading.EMFVersionLoader;
import com.oheers.fish.plugin.loading.EMFVersionProvider;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.competition.AutoRunner;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionQueue;
import com.oheers.fish.config.MainConfig;
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
import com.oheers.fish.utils.MinecraftVersionHelper;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.evenmorefish.dimensionfishing.DimensionFishing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.vanishchecker.VanishChecker;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EvenMoreFish extends EMFPlugin {

    private final EMFVersionLoader loader;
    private final EMFVersionProvider versionProvider;

    private final DimensionFishing dimensionFishing;

    private final Random random = ThreadLocalRandom.current();
    private final Toggle toggle;

    private final boolean isFolia = FishUtils.classExists("io.papermc.paper.threadedregions.RegionizedServer");

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
        if (instance == null) {
            throw new IllegalStateException("Plugin not initialized yet!");
        }
        return instance;
    }

    public static TaskScheduler getScheduler() {
        return scheduler;
    }

    public EvenMoreFish() {
        this.loader = new EMFVersionLoader(this, getClassLoader());
        this.versionProvider = loader.getVersionProvider();
        this.toggle = new Toggle(this);

        // Dimension Fishing is disabled on Folia for now.
        if (MinecraftVersionHelper.isAtLeastVersion("1.21.1") && !isFolia) {
            this.dimensionFishing = new DimensionFishing(
                this,
                DimensionFishingConfig.getInstance()
            );
        } else {
            this.dimensionFishing = null;
        }
    }

    @Override
    public void onLoad() {
        instance = this;
        versionProvider.loadCommands();
        versionProvider.load();
        if (dimensionFishing != null) {
            dimensionFishing.load();
        }
    }

    @Override
    public void onEnable() {
        versionProvider.enableCommands();

        scheduler = UniversalScheduler.getScheduler(this);

        this.api = new EMFAPI();

        this.decidedRarities = new HashMap<>();

        this.configurationManager = new ConfigurationManager(this);
        this.configurationManager.loadConfigurations(); //need to test, order may be important

        this.dependencyManager = new DependencyManager(this);
        this.dependencyManager.checkDependencies(); // need to test, order may be important, if it is, we introduce multiple stages with events

        this.integrationManager = new IntegrationManager(this);
        this.integrationManager.loadAddons();

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

        versionProvider.registerCommands();
        versionProvider.enable();

        if (dimensionFishing != null) {
            dimensionFishing.enable();
            this.integrationManager.setupDimensionFishing();
        }

        // Attempt to resume a competition if the temporary file exists.
        Competition.resumeFromFile();

        getLogger().info(() -> "EvenMoreFish by Oheers : Enabled");
    }

    @Override
    public void onDisable() {
        // Do this first.
        autoRunner.stop();

        if (dimensionFishing != null) {
            dimensionFishing.disable();
        }
        versionProvider.disableCommands();

        terminateGuis();
        // Ends the current competition in case the plugin is being disabled when the server will continue running
        Competition active = Competition.getCurrentlyActive();
        if (active != null) {
            active.end(false, true);
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

        this.integrationManager.unloadAddons();

        loader.onDisable();

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

        versionProvider.resendCommands();
        versionProvider.reload();

        if (dimensionFishing != null) {
            dimensionFishing.reload(sender);
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

    public EMFVersionProvider getVersionProvider() {
        return this.versionProvider;
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

    public @Nullable DimensionFishing getDimensionFishing() {
        return this.dimensionFishing;
    }

    // Things that don't belong here but have no place right now.

    /**
     * Temporary and for internal use only. Will be removed once API methods for messages are added.
     */
    @Override
    public void sendMessage(@NotNull String id, @NotNull Player player) {
        try {
            ConfigMessage message = ConfigMessage.valueOf(id.toUpperCase(Locale.ROOT));
            message.send(player);
        } catch (IllegalArgumentException exception) {
            Logging.warn("Invalid message id " + id, exception);
        }
    }

    /**
     * Temporary and for internal use only. Will be removed once a proper place is found for it.
     */
    @Override
    public void logSoldFish(@NotNull SoldFish sold) {
        if (!DatabaseUtil.isDatabaseOnline() || sold.getPlayer() == null) {
            return;
        }
        final UUID uuid = sold.getPlayer().getUniqueId();
        final String transactionId = FriendlyId.createFriendlyId();
        final Timestamp timestamp = Timestamp.from(Instant.now());
        final IFish fish = sold.getFish();
        final String fishName = fish.getName();
        final String rarityId = fish.getRarity().getId();
        final int quantity = sold.getQuantity();
        final float length = fish.getLength();
        final double finalValue = sold.getFinalValue();
        final double rawValue = sold.getValue();

        // Resolve the user row, insert sale data, and update cached report
        // state on the single FIFO database worker.
        pluginDataManager.getDatabaseWorker().execute(() -> {
            final int userId = pluginDataManager.getUserManager().getUserId(uuid);
            if (userId == 0) {
                getLogger().warning("Skipping sold fish database update because user id could not be resolved for " + uuid);
                return;
            }

            Database database = pluginDataManager.getDatabase();
            database.createTransaction(transactionId, userId, timestamp);
            database.createSale(
                transactionId,
                fishName,
                rarityId,
                quantity,
                length,
                finalValue
            );

            final DataManager<UserReport> userReportDataManager = pluginDataManager.getUserReportDataManager();
            final UserReport report = userReportDataManager.get(uuid.toString());
            if (report == null) {
                getLogger().warning("Skipping sold fish report update because user report could not be loaded for " + uuid);
                return;
            }
            report.incrementFishSold(quantity);
            report.incrementMoneyEarned(rawValue);

            userReportDataManager.update(uuid.toString(), report);
        });
    }

    /**
     * Temporary and for internal use only. Will be removed once API methods for messages are added.
     */
    @Override
    public void sendSoldMessage(double value, int count, @NotNull Player player) {
        EMFMessage message = ConfigMessage.FISH_SALE.getMessage();
        message.setSellPrice(Economy.getInstance().getWorthFormat(value, true));
        message.setAmount(count);
        message.setPlayer(player);
        message.send(player);
    }

}
