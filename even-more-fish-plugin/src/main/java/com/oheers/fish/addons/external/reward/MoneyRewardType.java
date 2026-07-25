package com.oheers.fish.addons.external.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class MoneyRewardType extends RewardType {

    @Override
    public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
        Economy economy = Economy.getInstance();
        int amount;
        try {
            amount = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        if (!economy.isEnabled()) {
            return;
        }
        EconomyType vault = EMFRegistry.ECONOMY_TYPE.get("vault");
        if (vault == null) {
            return;
        }
        vault.deposit(player, amount, false);
    }

    @Override
    public @NonNull String getIdentifier() {
        return "MONEY";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NonNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
