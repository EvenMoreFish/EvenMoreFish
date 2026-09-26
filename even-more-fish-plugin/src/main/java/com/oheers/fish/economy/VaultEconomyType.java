package com.oheers.fish.economy;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.EMFSingleMessage;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.firedev.daisylib.external.vault.VaultWrapper;

import java.util.logging.Level;

public class VaultEconomyType implements EconomyType {
    
    @Override
    public String getIdentifier() {
        return "Vault";
    }

    @Override
    public void load() {
        if (!MainConfig.getInstance().isEconomyEnabled(this)) {
            return;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            EvenMoreFish.getInstance().debug("Attempting to register Vault but it is not available.. ignoring");
            return;
        }
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
        return VaultWrapper.get().getEconomy().depositPlayer(player, prepareValue(amount, allowMultiplier)).transactionSuccess();
    }

    @Override
    public boolean withdraw(@NonNull OfflinePlayer player, double amount, boolean allowMultiplier) {
        if (!isAvailable()) {
            return false;
        }
        return VaultWrapper.get().getEconomy().withdrawPlayer(player, prepareValue(amount, allowMultiplier)).transactionSuccess();
    }

    @Override
    public boolean has(@NonNull OfflinePlayer player, double amount) {
        if (!isAvailable()) {
            return false;
        }
        return VaultWrapper.get().getEconomy().has(player, amount);
    }

    @Override
    public double get(@NonNull OfflinePlayer player) {
        if (!isAvailable()) {
            return 0;
        }
        return VaultWrapper.get().getEconomy().getBalance(player);
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
        String worthFormatted = VaultWrapper.get().getEconomy().format(worth);

        String display = MainConfig.getInstance().getEconomyDisplay(this);
        if (display == null) {
            return EMFSingleMessage.fromString(worthFormatted).getComponentMessage();
        }
        EMFSingleMessage message = EMFSingleMessage.fromString(display);
        message.setVariable("{amount}", worthFormatted);
        message.setVariable("{raw-amount}", FishUtils.roundDouble(worth, VaultWrapper.get().getEconomy().fractionalDigits()));
        return message.getComponentMessage();
    }

    @Override
    public boolean isAvailable() {
        if (!MainConfig.getInstance().isEconomyEnabled(this)) {
            return false;
        }
        if (!VaultWrapper.get().isEconomyAvailable()) {
            Logging.warn("There is no registered economy provider. Cannot use Vault EconomyType.");
            return false;
        }
        return true;
    }

}
