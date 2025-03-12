package com.oheers.fish.economy;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class VaultEconomyType implements EconomyType {

    private Economy economy = null;

    public VaultEconomyType() {
        EvenMoreFish emf = EvenMoreFish.getInstance();
        emf.getLogger().log(Level.INFO, "Economy attempting to hook into Vault.");
        if (EvenMoreFish.getInstance().isUsingVault()) {
            RegisteredServiceProvider<Economy> rsp = emf.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return;
            }
            economy = rsp.getProvider();
            emf.getLogger().log(Level.INFO, "Economy hooked into Vault.");
        }
    }

    @Override
    public String getIdentifier() {
        return "Vault";
    }

    @Override
    public double getMultiplier() {
        return MainConfig.getInstance().getEconomyMultiplier(this);
    }

    @Override
    public boolean deposit(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier) {
        if (!isAvailable()) {
            return false;
        }
        return economy.depositPlayer(player, prepareValue(amount, allowMultiplier)).transactionSuccess();
    }

    @Override
    public boolean withdraw(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier) {
        if (!isAvailable()) {
            return false;
        }
        return economy.withdrawPlayer(player, prepareValue(amount, allowMultiplier)).transactionSuccess();
    }

    @Override
    public boolean has(@NotNull OfflinePlayer player, double amount) {
        if (!isAvailable()) {
            return false;
        }
        return get(player) >= amount;
    }

    @Override
    public double get(@NotNull OfflinePlayer player) {
        if (!isAvailable()) {
            return 0;
        }
        return economy.getBalance(player);
    }

    @Override
    public double prepareValue(double value, boolean applyMultiplier) {
        double finalValue = value;
        if (applyMultiplier) {
            finalValue = value * getMultiplier();
        }
        return finalValue;
    }

    @Override
    public boolean isAvailable() {
        return (MainConfig.getInstance().isEconomyEnabled(this) && economy != null);
    }

    @Override
    public @Nullable String formatWorth(double totalWorth, boolean applyMultiplier) {
        if (!isAvailable()) {
            return null;
        }
        double worth = prepareValue(totalWorth, applyMultiplier);
        String display = MainConfig.getInstance().getEconomyDisplay(this);
        if (display != null) {
            EMFMessage message = EMFMessage.fromString(display);
            message.setVariable("{amount}", String.valueOf(worth));
            return message.getLegacyMessage();
        }
        return FishUtils.formatDouble(ConfigMessage.SELL_PRICE_FORMAT.getMessage().getLegacyMessage(), worth);
    }

}
