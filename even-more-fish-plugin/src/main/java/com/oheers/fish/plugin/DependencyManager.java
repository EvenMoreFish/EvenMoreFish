package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.economy.GriefPreventionEconomyType;
import com.oheers.fish.economy.PlayerPointsEconomyType;
import com.oheers.fish.events.AuraSkillsFishingEvent;
import com.oheers.fish.events.DeprecatedEventListener;
import com.oheers.fish.events.McMMOTreasureEvent;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.placeholders.PlaceholderReceiver;
import com.oheers.fish.utils.HeadDBIntegration;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.firedev.daisylib.DaisyLib;
import uk.firedev.daisylib.messages.MessageSettings;
import uk.firedev.daisylib.messages.ObjectProcessor;

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
    }

    public void checkDependencies() {
        PluginManager pm = Bukkit.getPluginManager();

        this.usingGriefPrevention = pm.isPluginEnabled("GriefPrevention");
        this.usingPlayerPoints = pm.isPluginEnabled("PlayerPoints");
        this.usingMcMMO = pm.isPluginEnabled("mcMMO");
        this.usingHeadsDB = pm.isPluginEnabled("HeadDatabase") && FishUtils.classExists("me.arcaniax.hdb.api.HeadDatabaseAPI");
        this.usingPAPI = pm.isPluginEnabled("PlaceholderAPI");
        this.usingAuraSkills = pm.isPluginEnabled("AuraSkills");

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

    public @Nullable Permission getPermission() {
        return permission;
    }

    public @Nullable HeadDatabaseAPI getHdbapi() {
        return hdbapi;
    }

    public boolean isUsingAuraSkills() {
        return usingAuraSkills;
    }

    public boolean isEconomyAvailable() {
        return usingVault || usingPlayerPoints || usingGriefPrevention;
    }

    public boolean isHeadsDBLoaded() {
        return usingHeadsDB && hdbapi != null;
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

}
