package com.oheers.fish.economy;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.EMFSingleMessage;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class VaultEconomyType implements EconomyType {

    private Economy economy;

    public VaultEconomyType() {
        if (!MainConfig.getInstance().isEconomyEnabled(this)) {
            return;
        }
        EvenMoreFish emf = EvenMoreFish.getInstance();
        if (!EvenMoreFish.getInstance().getDependencyManager().isUsingVault()) {
            EvenMoreFish.getInstance().debug("Attempting to register Vault but it is not available.. ignoring");
            return;
        }
        Logging.info("Economy attempting to hook into Vault.");
        RegisteredServiceProvider<Economy> rsp = emf.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Logging.warn("Could not obtain Economy service.");
            return;
        }
        economy = rsp.getProvider();
        emf.getLogger().log(Level.INFO, "Economy hooked into Vault.");
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
        return economy.has(player, amount);
    }

    @Override
    public double get(@NotNull OfflinePlayer player) {
        if (!isAvailable()) {
            return 0;
        }
        return economy.getBalance(player);
    }

    /**
     * Prepares a double for use with this economy type.
     *
     * @param value           The value to prepare.
     * @param applyMultiplier Should we apply the multiplier?
     * @return A prepared double for use with this economy type.
     */
    @Override
    public double prepareValue(double value, boolean applyMultiplier) {
        return applyMultiplier ? value * getMultiplier() : value;
    }

    /**
     * Creates a String to represent this value.
     *
     * @param totalWorth      The value to represent.
     * @param applyMultiplier Should the multiplier be applied to the value?
     * @return A String to represent this value.
     */
    @Override
    public @Nullable Component formatWorth(double totalWorth, boolean applyMultiplier) {
        if (!isAvailable()) {
            return null;
        }
        double worth = prepareValue(totalWorth, applyMultiplier);
        String worthFormatted = economy.format(worth);

        String display = MainConfig.getInstance().getEconomyDisplay(this);
        if (display == null) {
            return Component.text(worthFormatted);
        }
        EMFSingleMessage message = EMFSingleMessage.fromString(display);
        message.setVariable("{amount}", worthFormatted);
        return message.getComponentMessage();
    }

    @Override
    public boolean isAvailable() {
        return MainConfig.getInstance().isEconomyEnabled(this) && economy != null;
    }

}
