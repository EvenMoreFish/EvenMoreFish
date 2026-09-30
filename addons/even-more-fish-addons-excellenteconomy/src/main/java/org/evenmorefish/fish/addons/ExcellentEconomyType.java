package com.oheers.fish.economy;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.EMFSingleMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import uk.firedev.daisylib.external.vault.VaultWrapper;

import java.util.logging.Level;

public class ExcellentEconomyType implements EconomyType {

    private ExcellentEconomyAPI economy = null;
    private ExcellentCurrency currency = null;

    @Override
    public String getIdentifier() {
        return "ExcellentEconomy";
    }

    @Override
    public void load() {
        if (!MainConfig.getInstance().isEconomyEnabled(this)) {
            return;
        }
        EvenMoreFish emf = EvenMoreFish.getInstance();
        emf.getLogger().log(Level.INFO, "Economy attempting to hook into ExcellentEconomy.");
        if (!Bukkit.getPluginManager().isPluginEnabled("ExcellentEconomy")) {
            Logging.warn("Failed to hook into ExcellentEconomy: Plugin is not enabled.");
            return;
        }
        RegisteredServiceProvider<ExcellentEconomyAPI> provider = Bukkit.getServer().getServicesManager().getRegistration(ExcellentEconomyAPI.class);
        if (provider == null) {
            Logging.warn("Failed to hook into ExcellentEconomy: Could not find API.");
            return;
        }
        String currencyName = MainConfig.getInstance().getExcellentEconomyCurrency();
        if (currencyName == null) {
            Logging.warn("Failed to hook into ExcellentEconomy: Invalid currency configured.");
            return;
        }

        this.economy = provider.getProvider();
        this.currency = economy.getCurrency(currencyName);
        Logging.info("Economy hooked into ExcellentEconomy.");
    }

    @Override
    public double getMultiplier() {
        return MainConfig.getInstance().getEconomyMultiplier(this);
    }

    @Override
    public boolean deposit(@NonNull OfflinePlayer player, double amount, boolean allowMultiplier) {
        if (!isAvailable()) {
            return false;
        }
        Player online = player.getPlayer();
        if (online == null) {
            Logging.warn("Failed to deposit ExcellentEconomy currency: Player is offline.");
            return false;
        }
        return economy.deposit(online, currency, prepareValue(amount, allowMultiplier));
    }

    @Override
    public boolean withdraw(@NonNull OfflinePlayer player, double amount, boolean allowMultiplier) {
        if (!isAvailable()) {
            return false;
        }
        Player online = player.getPlayer();
        if (online == null) {
            Logging.warn("Failed to withdraw ExcellentEconomy currency: Player is offline.");
            return false;
        }
        return economy.withdraw(online, currency, prepareValue(amount, allowMultiplier));
    }

    @Override
    public boolean has(@NonNull OfflinePlayer player, double amount) {
        if (!isAvailable()) {
            return false;
        }
        return get(player) >= amount;
    }

    @Override
    public double get(@NonNull OfflinePlayer player) {
        if (!isAvailable()) {
            return 0;
        }
        Player online = player.getPlayer();
        if (online == null) {
            Logging.warn("Failed to fetch ExcellentEconomy balance: Player is offline.");
            return 0.0;
        }
        return economy.getBalance(online, currency);
    }

    @Override
    public double prepareValue(double value, boolean applyMultiplier) {
        if (applyMultiplier) {
            return Math.floor(value * getMultiplier());
        }
        return Math.floor(value);
    }

    @Override
    public @Nullable Component formatWorth(double totalWorth, boolean applyMultiplier) {
        if (!isAvailable()) {
            return null;
        }
        double worth = prepareValue(totalWorth, applyMultiplier);
        String worthFormatted = currency.format(worth);

        String display = MainConfig.getInstance().getEconomyDisplay(this);
        if (display == null) {
            return EMFSingleMessage.fromString(worthFormatted).getComponentMessage();
        }
        EMFSingleMessage message = EMFSingleMessage.fromString(display);
        message.setVariable("{amount}", worthFormatted);
        message.setVariable("{raw-amount}", currency.formatRaw(worth));
        return message.getComponentMessage();
    }

    @Override
    public boolean isAvailable() {
        return (MainConfig.getInstance().isEconomyEnabled(this) && economy != null && currency != null);
    }

}

