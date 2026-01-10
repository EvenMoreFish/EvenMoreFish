package com.oheers.fish.addons;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.addons.external.requirement.PermissionRequirementType;
import com.oheers.fish.addons.external.reward.AuraSkillsXPRewardType;
import com.oheers.fish.addons.external.reward.GPClaimBlocksRewardType;
import com.oheers.fish.addons.external.reward.McMMOXPRewardType;
import com.oheers.fish.addons.external.reward.MoneyRewardType;
import com.oheers.fish.addons.external.reward.PermissionRewardType;
import com.oheers.fish.addons.external.reward.PlayerPointsRewardType;
import com.oheers.fish.addons.internal.item.Head64ItemAddon;
import com.oheers.fish.addons.internal.requirement.BiomeRequirementType;
import com.oheers.fish.addons.internal.requirement.BiomeSetRequirementType;
import com.oheers.fish.addons.internal.requirement.DisabledRequirementType;
import com.oheers.fish.addons.internal.requirement.GroupRequirementType;
import com.oheers.fish.addons.internal.requirement.IRLTimeRequirementType;
import com.oheers.fish.addons.internal.requirement.InGameTimeRequirementType;
import com.oheers.fish.addons.internal.requirement.MoonPhaseRequirementType;
import com.oheers.fish.addons.internal.requirement.NearbyPlayersRequirementType;
import com.oheers.fish.addons.internal.requirement.PlaceholderRequirementType;
import com.oheers.fish.addons.internal.requirement.RegionRequirementType;
import com.oheers.fish.addons.internal.requirement.WeatherRequirementType;
import com.oheers.fish.addons.internal.requirement.WorldRequirementType;
import com.oheers.fish.addons.internal.reward.CommandRewardType;
import com.oheers.fish.addons.internal.reward.EXPRewardType;
import com.oheers.fish.addons.internal.reward.EffectRewardType;
import com.oheers.fish.addons.internal.reward.HealthRewardType;
import com.oheers.fish.addons.internal.reward.HungerRewardType;
import com.oheers.fish.addons.internal.reward.ItemRewardType;
import com.oheers.fish.addons.internal.reward.MessageRewardType;
import com.oheers.fish.addons.internal.reward.SoundRewardType;
import com.oheers.fish.api.addons.AddonLoader;
import com.oheers.fish.api.addons.AddonMetadata;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.plugin.DependencyManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;

public class InternalAddonLoader extends AddonLoader {
    public InternalAddonLoader(EMFPlugin plugin) {
        super(plugin, null);
    }
    @Override
    public AddonMetadata getAddonMetadata() {
        return new AddonMetadata(
                "Internal Addons",
                "1.0.0",
                List.of("EvenMoreFish"),
                "Bundled internal addons",
                "https://github.com/EvenMoreFish/EvenMoreFish",
                List.of("")); //many dependencies
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
        new SoundRewardType().register();
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
        Permission permission = EvenMoreFish.getInstance().getDependencyManager().getPermission();
        if (permission != null) {
            new GroupRequirementType(permission).register();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderRequirementType().register();
        }
    }

    private void loadExternalRewardTypes() {
        DependencyManager dependencyManager = EvenMoreFish.getInstance().getDependencyManager();
        if (dependencyManager.isUsingPlayerPoints()) {
            new PlayerPointsRewardType().register();
        }
        if (dependencyManager.isUsingGriefPrevention()) {
            new GPClaimBlocksRewardType().register();
        }
        if (dependencyManager.isUsingAuraSkills()) {
            new AuraSkillsXPRewardType().register();
        }
        if (dependencyManager.isUsingMcMMO()) {
            new McMMOXPRewardType().register();
        }
        // Only enable the PERMISSION type if Vault perms is found.
        Permission permission = EvenMoreFish.getInstance().getDependencyManager().getPermission();
        if (permission != null && permission.isEnabled()) {
            new PermissionRewardType().register();
        }
        // Only enable the Money RewardType is Vault Economy is enabled.
        if (dependencyManager.isUsingVault()) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                new MoneyRewardType().register();
            }
        }
    }

}
