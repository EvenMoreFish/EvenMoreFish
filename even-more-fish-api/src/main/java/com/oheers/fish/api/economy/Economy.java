package com.oheers.fish.api.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Economy {

    private final List<EconomyType> registeredEconomies;
    private static Economy instance = null;

    private Economy() {
        registeredEconomies = new ArrayList<>();
    }

    public static Economy getInstance() {
        if (instance == null) {
            instance = new Economy();
        }
        return instance;
    }

    public List<EconomyType> getRegisteredEconomies() { return List.copyOf(registeredEconomies); }

    /**
     * @return True if any registered economy is available.
     */
    public boolean isEnabled() {
        for (EconomyType economyType : registeredEconomies) {
            if (economyType.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public void deposit(@NotNull OfflinePlayer player, double amount, boolean applyMultiplier) {
        registeredEconomies.forEach(type -> type.deposit(player, amount, applyMultiplier));
    }

    public void withdraw(@NotNull OfflinePlayer player, double amount, boolean applyMultiplier) {
        registeredEconomies.forEach(type -> type.withdraw(player, amount, applyMultiplier));
    }

    public Map<EconomyType, Double> get(@NotNull OfflinePlayer player) {
        Map<EconomyType, Double> valuesMap = new HashMap<>();
        registeredEconomies.forEach(type -> valuesMap.put(type, type.get(player)));
        return valuesMap;
    }

    public @Nullable EconomyType getEconomyType(@NotNull String identifier) {
        for (EconomyType type : registeredEconomies) {
            if (type.isAvailable() && type.getIdentifier().equalsIgnoreCase(identifier)) {
                return type;
            }
        }
        return null;
    }

    public @NotNull Component getWorthFormat(double value, boolean applyMultiplier) {
        List<Component> components = registeredEconomies.stream()
                .map(type -> type.formatWorth(value, applyMultiplier))
                .filter(Objects::nonNull)
                .toList();
        return Component.join(JoinConfiguration.commas(true), components);
    }

    public boolean registerEconomyType(@NotNull EconomyType economyType) {
        if (getEconomyType(economyType.getIdentifier()) != null) {
            return false;
        }
        return registeredEconomies.add(economyType);
    }

}
