package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.api.economy.EconomyType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.bossbar.BossBar;
import org.apache.commons.lang3.LocaleUtils;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainConfig extends ConfigBase {

    private static MainConfig instance = null;

    // Cache these so we don't have a mismatch after reload.
    private final boolean adminShortcutEnabled;
    private final String adminShortcutName;
    private final String mainCommandName;
    private final List<String> mainCommandAliases;
    private final String adminSubCommandName;
    private final String nextSubCommandName;
    private final String toggleSubCommandName;
    private final String guiSubCommandName;
    private final String helpSubCommandName;
    private final String topSubCommandName;
    private final String shopSubCommandName;
    private final String sellAllSubCommandName;
    private final String applyBaitsSubCommandName;
    private final String journalSubCommandName;

    private Map<String, List<Biome>> biomeSets;
    private Map<String, Map<String, Double>> regionBoosts;

    public MainConfig() {
        super("config.yml", "config.yml", EvenMoreFish.getInstance(), true);
        instance = this;

        // Command caching
        this.mainCommandName = getConfig().getString("command.main", "emf");
        this.mainCommandAliases = getConfig().getStringList("command.aliases");
        this.adminShortcutEnabled = getConfig().getBoolean("command.admin-shortcut.enabled", true);
        this.adminShortcutName = getConfig().getString("command.admin-shortcut.name", "emfa");
        this.adminSubCommandName = getConfig().getString("command.subcommands.admin", "admin");
        this.nextSubCommandName = getConfig().getString("command.subcommands.next", "next");
        this.toggleSubCommandName = getConfig().getString("command.subcommands.toggle", "toggle");
        this.guiSubCommandName = getConfig().getString("command.subcommands.gui", "gui");
        this.helpSubCommandName = getConfig().getString("command.subcommands.help", "help");
        this.topSubCommandName = getConfig().getString("command.subcommands.top", "top");
        this.shopSubCommandName = getConfig().getString("command.subcommands.shop", "shop");
        this.sellAllSubCommandName = getConfig().getString("command.subcommands.sellall", "sellall");
        this.applyBaitsSubCommandName = getConfig().getString("command.subcommands.applybaits", "applybaits");
        this.journalSubCommandName = getConfig().getString("command.subcommands.journal", "journal");

        this.biomeSets = loadBiomeSets();
        this.regionBoosts = loadRegionBoosts();
    }

    @Override
    public void reload() {
        super.reload();

        this.biomeSets = loadBiomeSets();
        this.regionBoosts = loadRegionBoosts();
    }

    public static MainConfig getInstance() {
        return instance;
    }

    public String getLocale() {
        return getConfig().getString("locale", "en");
    }

    public boolean doingRandomDurability() {
        return getConfig().getBoolean("random-durability", true);
    }

    @Deprecated
    public boolean isDatabaseOnline() {
        if (!databaseEnabled() || EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().usingV2())
            return false;

        return EvenMoreFish.getInstance().getPluginDataManager().getDatabase() != null;
    }

    public boolean isCatchEnabled() {
        return getConfig().getBoolean("fishing.catch-enabled", true);
    }

    public boolean isFishCatchOnlyInCompetition() {
        return getConfig().getBoolean("fishing.catch-only-in-competition", false);
    }

    public boolean isGiveStraightToInventory() {
        return getConfig().getBoolean("fishing.give-straight-to-inventory", false);
    }

    public boolean isFishCatchOverrideOnlyFish() {
        return getConfig().getBoolean("fishing.only-fish", false);
    }

    public boolean isHuntEnabled() {
        return getConfig().getBoolean("fishing.hunt-enabled", false);
    }

    public boolean isFishHuntOnlyInCompetition() {
        return getConfig().getBoolean("fishing.hunt-only-in-competition", true);
    }

    public boolean isFishHuntIgnoreSpawnerFish() {
        return getConfig().getBoolean("fishing.hunt-ignore-spawner-fish", true);
    }

    public boolean isCompetitionUnique() {
        return getConfig().getBoolean("fish-only-in-competition", false);
    }

    public boolean getEnabled() {
        return getConfig().getBoolean("enabled", true);
    }

    public boolean worldWhitelist() {
        return !getConfig().getStringList("allowed-worlds").isEmpty();
    }

    public List<String> getAllowedRegions() {
        return getConfig().getStringList("allowed-regions");
    }

    public List<String> getAllowedWorlds() {
        return getConfig().getStringList("allowed-worlds");
    }

    public boolean shouldRespectVanish() { return getConfig().getBoolean("respect-vanished", true); }

    public boolean shouldProtectBaitedRods() { return getConfig().getBoolean("protect-baited-rods", true); }

    public BossBar.Overlay getBarStyle() {
        String styleString = getConfig().getString("barstyle");
        return FishUtils.fetchBarStyle(styleString);
    }

    public boolean sellOverDrop() {
        return getConfig().getBoolean("sell-gui.sell-over-drop", false);
    }

    public boolean disableMcMMOTreasure() {
        return getConfig().getBoolean("disable-mcmmo-loot", true);
    }

    public boolean disableAuraSkills() {
        return getConfig().getBoolean("disable-auraskills-loot", true);
    }

    public boolean doDBVerbose() {
        return !getConfig().getBoolean("database.disable-verbose", false);
    }

    public boolean requireCustomRod() {
        return getConfig().getBoolean("fishing.require-custom-rod", false);
    }

    public boolean requireFishingPermission() {
        return getConfig().getBoolean("requires-fishing-permission", false);
    }

    public boolean preventCrafting() {
        return getConfig().getBoolean("item-protection.prevent-crafting", true);
    }

    public boolean preventConsume() {
        return getConfig().getBoolean("item-protection.prevent-consume", true);
    }

    public boolean preventFurnaceBurn() {
        return getConfig().getBoolean("item-protection.prevent-furnace-burn", true);
    }

    public boolean preventCooking() {
        return getConfig().getBoolean("item-protection.prevent-cooking", true);
    }

    public boolean preventPlacing() {
        return getConfig().getBoolean("item-protection.prevent-placing", true);
    }

    public boolean shouldDebug() {
        return getConfig().getBoolean("debug", false);
    }

    public boolean databaseEnabled() {
        return getConfig().getBoolean("database.enabled", false);
    }

    public String getAddress() {
        return getConfig().getString("database.address", "localhost");
    }

    public String getDatabase() {
        return getConfig().getString("database.database", "database");
    }

    public String getUsername() {
        return getConfig().getString("database.username", "root");
    }

    public String getPassword() {
        return getConfig().getString("database.password", "");
    }

    public String getPrefix() {
        return getConfig().getString("database.prefix", "emf_");
    }

    public String getDatabaseType() {
        return getConfig().getString("database.type", "sqlite");
    }

    public String getSaveIntervalUnit() {
        return getConfig().getString("database.advanced.save-interval.units", "SECONDS");
    }

    public int getCompetitionSaveInterval() {
        return getSaveInterval("competition");
    }

    public int getUserFishStatsSaveInterval() {
        return getSaveInterval("user-fish-stats");
    }

    private int getSaveInterval(final String path) {
        return getConfig().getInt("database.advanced.save-interval." + path, 5);
    }

    public boolean isDisableJooqStartupCommments() {
        return getConfig().getBoolean("database.advanced.disable-jooq-startup-comments", true);
    }

    public boolean isJooqExecuteLoggingEnabled() {
        return getConfig().getBoolean("database.advanced.jooq-execute-logging", false);
    }

    public boolean isJooqRenderFormattedEnabled() {
        return getConfig().getBoolean("database.advanced.jooq-render-formatted", false);
    }


    public boolean useAdditionalAddons() {
        return getConfig().getBoolean("addons.additional-addons", true);
    }

    public int getNearbyPlayersRequirementRange() { return getConfig().getInt("requirement.nearby-players.range", 0); }

    public boolean isAdminShortcutCommandEnabled() {
        return adminShortcutEnabled;
    }

    public @NotNull String getAdminShortcutCommandName() {
        return adminShortcutName;
    }

    public String getMainCommandName() {
        return mainCommandName;
    }

    public List<String> getMainCommandAliases() {
        return mainCommandAliases;
    }

    public String getAdminSubCommandName() {
        return adminSubCommandName;
    }

    public String getNextSubCommandName() {
        return nextSubCommandName;
    }

    public String getToggleSubCommandName() {
        return toggleSubCommandName;
    }

    public String getGuiSubCommandName() {
        return guiSubCommandName;
    }

    public String getHelpSubCommandName() {
        return helpSubCommandName;
    }

    public String getTopSubCommandName() {
        return topSubCommandName;
    }

    public String getShopSubCommandName() {
        return shopSubCommandName;
    }

    public String getSellAllSubCommandName() {
        return sellAllSubCommandName;
    }

    public String getApplyBaitsSubCommandName() {
        return applyBaitsSubCommandName;
    }

    public String getJournalSubCommandName() {
        return journalSubCommandName;
    }

    public boolean useOldBaseCommandBehavior() {
        return getConfig().getBoolean("command.old-base-command-behavior", false);
    }

    private Map<String, List<Biome>> loadBiomeSets() {
        Map<String, List<Biome>> biomeSetMap = new HashMap<>();
        Section section = getConfig().getSection("biome-sets");
        if (section == null) {
            return Map.of();
        }
        section.getRoutesAsStrings(false).forEach(key -> {
            List<Biome> biomes = new ArrayList<>();
            section.getStringList(key).forEach(biomeString -> {
                Biome biome = FishUtils.getBiome(biomeString);
                if (biome == null) {
                    EvenMoreFish.getInstance().getLogger().severe(biomeString + " is not a valid biome, found when loading in biome set " + key + ".");
                }
                biomes.add(biome);
            });
            biomeSetMap.put(key, biomes);
        });
        return biomeSetMap;
    }

    private Map<String, Map<String, Double>> loadRegionBoosts() {
        Section regionBoostsSection = getConfig().getSection("region-boosts");
        if (regionBoostsSection == null) {
            return Map.of();
        }

        Map<String, Map<String, Double>> boosts = new HashMap<>();
        regionBoostsSection.getRoutesAsStrings(false).forEach(regionKey -> {
            Section regionSection = regionBoostsSection.getSection(regionKey);
            if (regionSection == null) {
                return;
            }

            Map<String, Double> rarityBoosts = new HashMap<>();
            regionSection.getRoutesAsStrings(false).forEach(rarityKey ->
                    rarityBoosts.put(rarityKey, regionSection.getDouble(rarityKey, 1.0))
            );

            boosts.put(regionKey, rarityBoosts);
        });

        return boosts;
    }

    public Map<String, List<Biome>> getBiomeSets() {
        return biomeSets;
    }

    public double getRegionBoost(String region, String rarity) {
        double defaultBoostRate = 1.0;
        if (region == null || rarity == null) {
            return defaultBoostRate; // Default boost rate is 1.0 if region or rarity is null
        }

        if (regionBoosts == null || regionBoosts.isEmpty()) {
            return defaultBoostRate; // Default boost rate is 1.0 if not specified
        }

        Map<String, Double> regionMap = regionBoosts.get(region);
        if (regionMap == null) {
            return defaultBoostRate; // Default boost rate is 1.0 if not specified
        }

        return regionMap.getOrDefault(rarity, defaultBoostRate); // Default boost rate is 1.0 if not specified
    }

    public boolean isRegionBoostsEnabled() {
        return regionBoosts != null && !regionBoosts.isEmpty();
    }

    public boolean isEconomyEnabled(@NotNull EconomyType type) {
        return getConfig().getBoolean("economy." + type.getIdentifier().toLowerCase() + ".enabled");
    }

    public double getEconomyMultiplier(@NotNull EconomyType type) {
        return getConfig().getDouble("economy." + type.getIdentifier().toLowerCase() + ".multiplier");
    }

    public @Nullable String getEconomyDisplay(@NotNull EconomyType type) {
        return getConfig().getString("economy." + type.getIdentifier().toLowerCase() + ".display");
    }

    @Override
    public UpdaterSettings getUpdaterSettings() {
        return UpdaterSettings.builder(super.getUpdaterSettings())
            // Config Version 1 - Economy Rework
            .addCustomLogic("1", document -> {
                String economyType = document.getString("economy-type");
                document.remove("enable-economy");
                document.remove("economy-type");
                if (economyType != null && !economyType.equalsIgnoreCase("NONE")) {
                    String path = "economy." + economyType.toLowerCase();
                    document.set(path + ".enabled", true);
                }
            })
            // Config Version 1 - Add item protection configs
            .addRelocation("1", "block-crafting", "item-protection.block-crafting", '.')
            // Config Version 1 - Update fishing section of the config
            .addRelocation("1", "fish-only-in-competition", "fishing.catch-only-in-competition", '.')
            // Config Version 1 - Add fishing.catch-enabled config
            .addCustomLogic("1", document -> {
                if (!document.contains("vanilla-fishing")) {
                    return;
                }
                boolean vanillaFishing = document.getBoolean("vanilla-fishing");
                document.set("fishing.catch-enabled", !vanillaFishing);
                document.remove("vanilla-fishing");
            })
            // Config Version 2 - Rework NBT Rods
            .addRelocation("2", "require-nbt-rod", "fishing.require-custom-rod", '.')
            // Config Version 4:
            // Rename all item protection configs to use "prevent" rather than "block".
            // Move disable-db-verbose to database.disable-verbose.
            // Move give-straight-to-inventory to fishing.give-straight-to-inventory.
            .addRelocations("4", Map.of(
                "item-protection.block-crafting", "item-protection.prevent-crafting",
                "item-protection.block-consume", "item-protection.prevent-consume",
                "item-protection.block-furnace-burn", "item-protection.prevent-furnace-burn",
                "item-protection.block-cooking", "item-protection.prevent-cooking",
                "item-protection.block-placing", "item-protection.prevent-placing",
                "disable-db-verbose", "database.disable-verbose",
                "give-straight-to-inventory", "fishing.give-straight-to-inventory"
            ), '.')
            // Config Version 5: Rename AureliumSkills setting to AuraSkills
            .addRelocation("5", "disable-aurelium-skills", "disable-aura-skills", '.')
            .build();
    }

    public boolean hasCredentials() {
        return MainConfig.getInstance().getUsername() != null &&
                MainConfig.getInstance().getPassword() != null &&
                MainConfig.getInstance().getAddress() != null &&
                MainConfig.getInstance().getDatabase() != null;
    }

    // Bait configs

    public double getBaitBoostRate() {
        return getConfig().getDouble("bait.boost", 1.5);
    }

    public boolean getBaitCompetitionDisable() {
        return getConfig().getBoolean("bait.competition-disable", true);
    }

    public boolean getBaitAddToLore() {
        return getConfig().getBoolean("bait.add-to-lore", true);
    }

    public double getBaitCatchPercentage() {
        return getConfig().getDouble("bait.catch-percentage", 2.5);
    }

    public int getBaitsPerRod() {
        return getConfig().getInt("bait.baits-per-rod", 7);
    }

    public boolean getBaitShowUnusedSlots() {
        return getConfig().getBoolean("bait.show-unused-slots", true);
    }


}
