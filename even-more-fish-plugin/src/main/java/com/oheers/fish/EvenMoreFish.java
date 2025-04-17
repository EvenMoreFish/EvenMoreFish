package com.oheers.fish;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.oheers.fish.addons.DefaultAddons;
import com.oheers.fish.addons.impl.Head64ItemAddon;
import com.oheers.fish.api.EMFAPI;
import com.oheers.fish.api.addons.AddonManager;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.requirement.RequirementType;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.baits.BaitListener;
import com.oheers.fish.baits.BaitManager;
import com.oheers.fish.commands.AdminCommand;
import com.oheers.fish.commands.MainCommand;
import com.oheers.fish.competition.AutoRunner;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionQueue;
import com.oheers.fish.competition.JoinChecker;
import com.oheers.fish.competition.rewardtypes.CommandRewardType;
import com.oheers.fish.competition.rewardtypes.EXPRewardType;
import com.oheers.fish.competition.rewardtypes.EffectRewardType;
import com.oheers.fish.competition.rewardtypes.HealthRewardType;
import com.oheers.fish.competition.rewardtypes.HungerRewardType;
import com.oheers.fish.competition.rewardtypes.ItemRewardType;
import com.oheers.fish.competition.rewardtypes.MessageRewardType;
import com.oheers.fish.competition.rewardtypes.external.AuraSkillsXPRewardType;
import com.oheers.fish.competition.rewardtypes.external.GPClaimBlocksRewardType;
import com.oheers.fish.competition.rewardtypes.external.McMMOXPRewardType;
import com.oheers.fish.competition.rewardtypes.external.MoneyRewardType;
import com.oheers.fish.competition.rewardtypes.external.PermissionRewardType;
import com.oheers.fish.competition.rewardtypes.external.PlayerPointsRewardType;
import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.config.GuiFillerConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.database.Database;
import com.oheers.fish.economy.GriefPreventionEconomyType;
import com.oheers.fish.economy.PlayerPointsEconomyType;
import com.oheers.fish.economy.VaultEconomyType;
import com.oheers.fish.events.AuraSkillsFishingEvent;
import com.oheers.fish.events.AureliumSkillsFishingEvent;
import com.oheers.fish.events.FishEatEvent;
import com.oheers.fish.events.FishInteractEvent;
import com.oheers.fish.events.McMMOTreasureEvent;
import com.oheers.fish.fishing.FishingProcessor;
import com.oheers.fish.fishing.HuntingProcessor;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.requirements.BiomeRequirementType;
import com.oheers.fish.requirements.BiomeSetRequirementType;
import com.oheers.fish.requirements.DisabledRequirementType;
import com.oheers.fish.requirements.GroupRequirementType;
import com.oheers.fish.requirements.IRLTimeRequirementType;
import com.oheers.fish.requirements.InGameTimeRequirementType;
import com.oheers.fish.requirements.MoonPhaseRequirementType;
import com.oheers.fish.requirements.NearbyPlayersRequirementType;
import com.oheers.fish.requirements.PermissionRequirementType;
import com.oheers.fish.requirements.RegionRequirementType;
import com.oheers.fish.requirements.WeatherRequirementType;
import com.oheers.fish.requirements.WorldRequirementType;
import com.oheers.fish.utils.HeadDBIntegration;
import com.oheers.fish.utils.ItemFactory;
import com.oheers.fish.utils.ItemProtectionListener;
import com.oheers.fish.utils.nbt.NbtKeys;
import de.themoep.inventorygui.InventoryGui;
import de.tr7zw.changeme.nbtapi.NBT;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.milkbowl.vault.permission.Permission;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.vanishchecker.VanishChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oheers.fish.FishUtils.classExists;

public class EvenMoreFish extends EMFPlugin {

    private final Random random = new Random();
    private final boolean isPaper = classExists("com.destroystokyo.paper.PaperConfig")
        || classExists("io.papermc.paper.configuration.Configuration");

    private Permission permission = null;
    private ItemStack customNBTRod;
    private boolean checkingEatEvent;
    private boolean checkingIntEvent;
    // Do some fish in some rarities have the comp-check-exempt: true.
    private boolean raritiesCompCheckExempt = false;
    private CompetitionQueue competitionQueue;
    private Logger logger;
    private PluginManager pluginManager;
    private int metricFishCaught = 0;
    private int metricBaitsUsed = 0;
    private int metricBaitsApplied = 0;
    private boolean firstLoad = false;

    // this is for pre-deciding a rarity and running particles if it will be chosen
    // it's a work-in-progress solution and probably won't stick.
    private Map<UUID, Rarity> decidedRarities;
    private boolean isUpdateAvailable;
    private boolean usingVault;
    private boolean usingPAPI;
    private boolean usingMcMMO;
    private boolean usingHeadsDB;
    private boolean usingPlayerPoints;
    private boolean usingGriefPrevention;

    private Database database;
    private HeadDatabaseAPI HDBapi;

    private static EvenMoreFish instance;
    private static TaskScheduler scheduler;
    private EMFAPI api;

    private AddonManager addonManager;

    public EvenMoreFish() {
        // Assigns the EMFPlugin instance for API usage.
        super();
        instance = this;
    }

    public static @NotNull EvenMoreFish getInstance() {
        return instance;
    }

    public static TaskScheduler getScheduler() {
        return scheduler;
    }

    public AddonManager getAddonManager() {
        return addonManager;
    }

    @Override
    public void onLoad() {
        CommandAPIBukkitConfig config = new CommandAPIBukkitConfig(this)
                .shouldHookPaperReload(true)
                .usePluginNamespace()
                .missingExecutorImplementationMessage("You are not able to use this command!");
        CommandAPI.onLoad(config);
    }

    @Override
    public void onEnable() {
        // Don't enable if the server is not using Paper.
        if (!isPaper) {
            getLogger().severe("Spigot detected! EvenMoreFish no longer runs on Spigot, we recommend updating to Paper instead. https://papermc.io/downloads/paper");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!NBT.preloadApi()) {
            throw new RuntimeException("NBT-API wasn't initialized properly, disabling the plugin");
        }

        CommandAPI.onEnable();

        // If EMF folder does not exist, this is the first load.
        firstLoad = !getDataFolder().exists();

        scheduler = UniversalScheduler.getScheduler(this);

        this.api = new EMFAPI();

        decidedRarities = new HashMap<>();

        logger = getLogger();
        pluginManager = getServer().getPluginManager();

        usingVault = Bukkit.getPluginManager().isPluginEnabled("Vault");
        usingGriefPrevention = Bukkit.getPluginManager().isPluginEnabled("GriefPrevention");
        usingPlayerPoints = Bukkit.getPluginManager().isPluginEnabled("PlayerPoints");

        new MainConfig();
        new MessageConfig();

        saveAdditionalDefaultAddons();
        loadAddonManager();

        new GuiConfig();
        new GuiFillerConfig();

        checkPapi();

        if (MainConfig.getInstance().requireNBTRod()) {
            customNBTRod = createCustomNBTRod();
        }

        loadEconomy();

        // could not set up economy.
        if (!Economy.getInstance().isEnabled()) {
            EvenMoreFish.getInstance().getLogger().warning("EvenMoreFish won't be hooking into economy. If this wasn't by choice in config.yml, please install Economy handling plugins.");
        }

        setupPermissions();

        FishManager.getInstance().load();
        BaitManager.getInstance().load();

        competitionQueue = new CompetitionQueue();
        competitionQueue.load();

        // check for updates on the modrinth page
        checkUpdate().thenAccept(available ->
            isUpdateAvailable = available
        );

        listeners();
        registerCommands();

        if (!MainConfig.getInstance().debugSession()) {
            metrics();
        }

        AutoRunner.init();

        if (MainConfig.getInstance().databaseEnabled()) {
            DataManager.init();

            database = new Database();
            DataManager.getInstance().loadUserReportsIntoCache();
        }

        logger.log(Level.INFO, "EvenMoreFish by Oheers : Enabled");

        // Set this to false as the plugin is now loaded.
        firstLoad = false;
    }

    @Override
    public void onDisable() {
        // If the server is not using Paper, the plugin won't have enabled in the first place.
        if (!isPaper) {
            return;
        }

        CommandAPI.onDisable();

        terminateGuis();
        // Don't use the scheduler here because it will throw errors on disable
        saveUserData(false);

        // Ends the current competition in case the plugin is being disabled when the server will continue running
        Competition active = Competition.getCurrentlyActive();
        if (active != null) {
            active.end(false);
        }

        RewardType.unregisterAll();
        RequirementType.unregisterAll();

        if (MainConfig.getInstance().databaseEnabled()) {
            database.shutdown();
        }

        FishManager.getInstance().unload();
        BaitManager.getInstance().unload();

        logger.log(Level.INFO, "EvenMoreFish by Oheers : Disabled");
    }

    private void saveAdditionalDefaultAddons() {
        if (!MainConfig.getInstance().useAdditionalAddons()) {
            return;
        }

        for (final String fileName : Arrays.stream(DefaultAddons.values())
                .map(DefaultAddons::getFullFileName)
                .toList()) {
            final File addonFile = new File(getDataFolder(), "addons/" + fileName);
            final File jarFile = new File(getDataFolder(), "addons/" + fileName.replace(".addon", ".jar"));
            if (!jarFile.exists()) {
                try {
                    this.saveResource("addons/" + fileName, true);
                    addonFile.renameTo(jarFile);
                } catch (IllegalArgumentException e) {
                    debug(Level.WARNING, String.format("Default addon %s does not exist.", fileName));
                }
            }
        }
    }

    public static void debug(final String message) {
        debug(Level.INFO, message);
    }

    public static void debug(final Level level, final String message) {
        if (MainConfig.getInstance().debugSession()) {
            getInstance().getLogger().log(level, () -> "DEBUG %s".formatted(message));
        }
    }

    public static void dbVerbose(final String message) {
        if (MainConfig.getInstance().doDBVerbose()) {
            getInstance().getLogger().info("DB-VERBOSE %s".formatted(message));
        }
    }

    private void listeners() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new JoinChecker(), this);
        pm.registerEvents(new FishingProcessor(), this);
        pm.registerEvents(new HuntingProcessor(), this);
        pm.registerEvents(new UpdateNotify(), this);
        pm.registerEvents(new SkullSaver(), this);
        pm.registerEvents(new BaitListener(), this);
        pm.registerEvents(new ItemProtectionListener(), this);

        optionalListeners();
    }

    private void optionalListeners() {
        PluginManager pm = getServer().getPluginManager();

        if (checkingEatEvent) {
            pm.registerEvents(FishEatEvent.getInstance(), this);
        }

        if (checkingIntEvent) {
            pm.registerEvents(FishInteractEvent.getInstance(), this);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            usingMcMMO = true;
            if (MainConfig.getInstance().disableMcMMOTreasure()) {
                pm.registerEvents(McMMOTreasureEvent.getInstance(), this);
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
            usingHeadsDB = true;
            pm.registerEvents(new HeadDBIntegration(), this);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AureliumSkills")) {
            if (MainConfig.getInstance().disableAureliumSkills()) {
                pm.registerEvents(new AureliumSkillsFishingEvent(), this);
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("AuraSkills")) {
            if (MainConfig.getInstance().disableAureliumSkills()) {
                pm.registerEvents(new AuraSkillsFishingEvent(), this);
            }
        }
    }

    private void metrics() {
        Metrics metrics = new Metrics(this, 11054);

        metrics.addCustomChart(new SingleLineChart("fish_caught", () -> {
            int returning = metricFishCaught;
            metricFishCaught = 0;
            return returning;
        }));

        metrics.addCustomChart(new SingleLineChart("baits_applied", () -> {
            int returning = metricBaitsApplied;
            metricBaitsApplied = 0;
            return returning;
        }));

        metrics.addCustomChart(new SingleLineChart("baits_used", () -> {
            int returning = metricBaitsUsed;
            metricBaitsUsed = 0;
            return returning;
        }));

        metrics.addCustomChart(new SimplePie("database", () -> MainConfig.getInstance().databaseEnabled() ? "true" : "false"));

        metrics.addCustomChart(new SimplePie("paper-adapter", () -> "true"));
    }

    private boolean setupPermissions() {
        if (!usingVault) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp == null ? null : rsp.getProvider();
        return permission != null;
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

    private void saveUserData(boolean scheduler) {
        Runnable save = () -> {
            if (!(MainConfig.getInstance().isDatabaseOnline())) {
                return;
            }

            DataManager.getInstance().saveFishReports();
            DataManager.getInstance().saveUserReports();

            DataManager.getInstance().uncacheAll();
        };
        if (scheduler) {
            getScheduler().runTask(save);
        } else {
            save.run();
        }
    }


    public ItemStack createCustomNBTRod() {
        ItemFactory itemFactory = new ItemFactory("nbt-rod-item", MainConfig.getInstance().getConfig());
        itemFactory.enableDefaultChecks();
        itemFactory.setItemDisplayNameCheck(true);
        itemFactory.setItemLoreCheck(true);

        ItemStack customRod = itemFactory.createItem(null, 0);

        setCustomNBTRod(customRod);

        return customRod;
    }

    /**
     * Allows external plugins to set their own items as an EMF NBT-rod.
     * @param item The item to set as an EMF NBT-rod.
     */
    public void setCustomNBTRod(@NotNull ItemStack item) {
        NBT.modify(item, nbt -> {
            nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND).setBoolean(NbtKeys.EMF_ROD_NBT, true);
        });
    }

    @Override
    public void reload(@Nullable CommandSender sender) {

        // If EMF folder does not exist, assume first load again.
        firstLoad = !getDataFolder().exists();

        terminateGuis();

        reloadConfig();
        saveDefaultConfig();

        MainConfig.getInstance().reload();
        MessageConfig.getInstance().reload();
        GuiConfig.getInstance().reload();
        GuiFillerConfig.getInstance().reload();

        FishManager.getInstance().reload();
        BaitManager.getInstance().reload();

        HandlerList.unregisterAll(FishEatEvent.getInstance());
        HandlerList.unregisterAll(FishInteractEvent.getInstance());
        HandlerList.unregisterAll(McMMOTreasureEvent.getInstance());
        optionalListeners();

        if (MainConfig.getInstance().requireNBTRod()) {
            customNBTRod = createCustomNBTRod();
        }

        competitionQueue.load();

        if (sender != null) {
            ConfigMessage.RELOAD_SUCCESS.getMessage().send(sender);
        }

        firstLoad = false;

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

    // Checks for updates, surprisingly
    @SuppressWarnings("UnstableApiUsage")
    private CompletableFuture<Boolean> checkUpdate() {
        return CompletableFuture.supplyAsync(() -> {
            ComparableVersion modrinthVersion = new ComparableVersion(new UpdateChecker(this).getVersion());
            ComparableVersion serverVersion = new ComparableVersion(getPluginMeta().getVersion());
            return modrinthVersion.compareTo(serverVersion) > 0;
        });
    }

    private void checkPapi() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            usingPAPI = true;
            new PlaceholderReceiver(this).register();
        }
    }

    public Random getRandom() {
        return random;
    }

    public Permission getPermission() {
        return permission;
    }

    public ItemStack getCustomNBTRod() {
        return customNBTRod;
    }

    public boolean isCheckingEatEvent() {
        return checkingEatEvent;
    }

    public void setCheckingEatEvent(boolean bool) {
        this.checkingEatEvent = bool;
    }

    public boolean isCheckingIntEvent() {
        return checkingIntEvent;
    }

    public void setCheckingIntEvent(boolean bool) {
        this.checkingIntEvent = bool;
    }

    public boolean isRaritiesCompCheckExempt() {
        return raritiesCompCheckExempt;
    }

    public void setRaritiesCompCheckExempt(boolean bool) {
        this.raritiesCompCheckExempt = bool;
    }

    public CompetitionQueue getCompetitionQueue() {
        return competitionQueue;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public int getMetricFishCaught() {
        return metricFishCaught;
    }

    public void incrementMetricFishCaught(int value) {
        this.metricFishCaught = (metricFishCaught + value);
    }

    public int getMetricBaitsUsed() {
        return metricBaitsUsed;
    }

    public void incrementMetricBaitsUsed(int value) {
        this.metricBaitsUsed = (metricBaitsUsed + value);
    }

    public int getMetricBaitsApplied() {
        return metricBaitsApplied;
    }

    public void incrementMetricBaitsApplied(int value) {
        this.metricBaitsApplied = (metricBaitsApplied + value);
    }

    public Map<UUID, Rarity> getDecidedRarities() {
        return decidedRarities;
    }

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public boolean isUsingVault() {return usingVault;}

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

    public boolean isUsingGriefPrevention() {return usingGriefPrevention;}

    public Database getDatabase() {
        return database;
    }

    public HeadDatabaseAPI getHDBapi() {
        return HDBapi;
    }

    public void setHDBapi(HeadDatabaseAPI api) {
        this.HDBapi = api;
    }

    public EMFAPI getApi() {
        return api;
    }

    private void loadEconomy() {
        PluginManager pm = Bukkit.getPluginManager();

        if (pm.isPluginEnabled("Vault")) {
            new VaultEconomyType().register();
        }
        if (pm.isPluginEnabled("PlayerPoints")) {
            new PlayerPointsEconomyType().register();
        }
        if (pm.isPluginEnabled("GriefPrevention")) {
            new GriefPreventionEconomyType().register();
        }
    }

    private void loadItemAddons() {
        // Load ItemAddons
        new Head64ItemAddon().register();
    }

    private void loadRewardTypes() {
        // Load RewardTypes
        new CommandRewardType().register();
        new EffectRewardType().register();
        new HealthRewardType().register();
        new HungerRewardType().register();
        new ItemRewardType().register();
        new MessageRewardType().register();
        new EXPRewardType().register();
        loadExternalRewardTypes();
    }

    private void loadRequirementTypes() {
        // Load RequirementTypes
        new BiomeRequirementType().register();
        new BiomeSetRequirementType().register();
        new DisabledRequirementType().register();
        new InGameTimeRequirementType().register();
        new IRLTimeRequirementType().register();
        new MoonPhaseRequirementType().register();
        new NearbyPlayersRequirementType().register();
        new PermissionRequirementType().register();
        new RegionRequirementType().register();
        new WeatherRequirementType().register();
        new WorldRequirementType().register();

        // Load Group RequirementType
        if (isUsingVault()) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp != null) {
                new GroupRequirementType(rsp.getProvider()).register();
            }
        }
    }

    private void loadExternalRewardTypes() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.isPluginEnabled("PlayerPoints")) {
            new PlayerPointsRewardType().register();
        }
        if (pm.isPluginEnabled("GriefPrevention")) {
            new GPClaimBlocksRewardType().register();
        }
        if (pm.isPluginEnabled("AuraSkills")) {
            new AuraSkillsXPRewardType().register();
        }
        if (pm.isPluginEnabled("mcMMO")) {
            new McMMOXPRewardType().register();
        }
        // Only enable the PERMISSION type if Vault perms is found.
        if (getPermission() != null && getPermission().isEnabled()) {
            new PermissionRewardType().register();
        }
        // Only enable the Money RewardType is Vault is enabled.
        if (pm.isPluginEnabled("Vault")) {
            new MoneyRewardType().register();
        }
    }

    private void loadAddonManager() {
        this.addonManager = new AddonManager();
        this.addonManager.load();

        // Internal Addons
        loadItemAddons();
        loadRewardTypes();
        loadRequirementTypes();
    }

    public List<Player> getVisibleOnlinePlayers() {
        return MainConfig.getInstance().shouldRespectVanish() ? VanishChecker.getVisibleOnlinePlayers() : new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    // FISH TOGGLE METHODS

    public void performFishToggle(@NotNull Player player) {
        NamespacedKey key = new NamespacedKey(this, "fish-disabled");
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        // If custom fishing is disabled
        if (isCustomFishingDisabled(player)) {
            // Set fish-disabled to false
            pdc.set(key, PersistentDataType.BOOLEAN, false);
            ConfigMessage.TOGGLE_ON.getMessage().send(player);
        } else {
            // Set fish-disabled to true
            pdc.set(key, PersistentDataType.BOOLEAN, true);
            ConfigMessage.TOGGLE_OFF.getMessage().send(player);
        }
    }

    public boolean isCustomFishingDisabled(@NotNull Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(this, "fish-disabled");
        return pdc.getOrDefault(key, PersistentDataType.BOOLEAN, false);
    }

    public boolean isFirstLoad() {
        return firstLoad;
    }

}
