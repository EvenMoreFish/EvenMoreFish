package com.oheers.fish.api.economy;

import com.oheers.fish.api.plugin.EMFPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Economy {

    private final Set<EconomyType> registeredEconomies;
    private static Economy instance = null;

    private Economy() {
        this.registeredEconomies = new HashSet<>();
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
        if (registeredEconomies.isEmpty()) {
            EMFPlugin.getInstance().getLogger().warning("There are no registered economies.");
            return false;
        }

        return registeredEconomies.stream().anyMatch(EconomyType::isAvailable);
    }

    public void deposit(@NotNull OfflinePlayer player, double amount, boolean applyMultiplier) {
        registeredEconomies.forEach(type -> type.deposit(player, amount, applyMultiplier));
    }

    public void withdraw(@NotNull OfflinePlayer player, double amount, boolean applyMultiplier) {
        registeredEconomies.forEach(type -> type.withdraw(player, amount, applyMultiplier));
    }

    public boolean has(@NotNull OfflinePlayer player, double amount) {
        return registeredEconomies.stream()
            .allMatch(type -> type.has(player, amount));
    }

    public Map<EconomyType, Double> get(@NotNull OfflinePlayer player) {
        Map<EconomyType, Double> valuesMap = new HashMap<>();
        registeredEconomies.forEach(type -> valuesMap.put(type, type.get(player)));
        return valuesMap;
    }

    public @NotNull Optional<EconomyType> getEconomyType(@NotNull String identifier) {
        return registeredEconomies.stream()
                .filter(EconomyType::isAvailable)
                .filter(type -> type.getIdentifier().equalsIgnoreCase(identifier))
                .findFirst();
    }

    public @NotNull Component getWorthFormat(double value, boolean applyMultiplier) {
        List<Component> components = registeredEconomies.stream()
                .map(type -> type.formatWorth(value, applyMultiplier))
                .filter(Objects::nonNull)
                .toList();
        return Component.join(JoinConfiguration.commas(true), components);
    }

    public boolean registerEconomyType(@NotNull EconomyType economyType) {
        if (getEconomyType(economyType.getIdentifier()).isPresent()) {
            // guessing this one is the problem
            return false;
        }

        return registeredEconomies.add(economyType);
    }

}
