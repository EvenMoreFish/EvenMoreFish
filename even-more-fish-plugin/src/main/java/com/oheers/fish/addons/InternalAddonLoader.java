package com.oheers.fish.addons;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.addons.impl.Head64ItemAddon;
import com.oheers.fish.api.addons.AddonLoader;
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
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class InternalAddonLoader extends AddonLoader {

    @Override
    public @NotNull String getName() {
        return "Internal Addons";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @NotNull String getAuthor() {
        return "EvenMoreFish";
    }

    @Override
    public boolean canLoad() {
        return true;
    }

    @Override
    public void loadAddons() {
        loadItemAddons();
        loadRewardTypes();
        loadRequirementTypes();
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
        Permission permission = EvenMoreFish.getInstance().getPermission();
        if (permission != null) {
            new GroupRequirementType(permission).register();
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
        Permission permission = EvenMoreFish.getInstance().getPermission();
        if (permission != null && permission.isEnabled()) {
            new PermissionRewardType().register();
        }
        // Only enable the Money RewardType is Vault Economy is enabled.
        if (EvenMoreFish.getInstance().isUsingVault()) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                new MoneyRewardType().register();
            }
        }
    }

}
