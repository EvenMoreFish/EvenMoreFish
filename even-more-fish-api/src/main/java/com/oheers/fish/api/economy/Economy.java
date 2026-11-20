package com.oheers.fish.api.economy;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.registry.RegistryItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Economy {

    private static final Logger LOGGER = Logger.getLogger(Economy.class.getName());

    public enum DepositResult {
        SUCCESS,
        FAILED,
        PARTIAL
    }

    public static Economy instance;

    private final List<EconomyType> relevantTypes;

    private Economy(@NonNull Collection<EconomyType> types) {
        this.relevantTypes = new ArrayList<>(types);
    }

    /**
     * Creates a new Economy instance with all registered EconomyTypes.
     */
    public static @NonNull Economy economy() {
        return new Economy(EMFRegistry.ECONOMY_TYPE.getRegistry().values());
    }

    /**
     * Creates a new Economy instance with a list of EconomyTypes.
     * @param types List of EconomyType to register.
     */
    public static @NonNull Economy economy(@NonNull Collection<EconomyType> types) {
        return new Economy(types);
    }

    public static @NonNull Economy getInstance() {
        if (instance == null) {
            instance = economy();
        }
        return instance;
    }

    /**
     * @deprecated Use {@link #getEconomyTypes()} instead.
     */
    @Deprecated(forRemoval = true)
    public List<EconomyType> getRegisteredEconomies() {
        return getEconomyTypes();
    }

    /**
     * Returns a copy of the EconomyTypes relevant to this Economy instance.
     * If no types were provided at creation, all registered EconomyTypes are returned.
     */
    public List<EconomyType> getEconomyTypes() {
        return List.copyOf(relevantTypes);
    }

    public void setEconomyTypes(@NonNull Collection<EconomyType> types) {
        relevantTypes.clear();
        relevantTypes.addAll(types);
    }

    /**
     * @return True if any registered economy is available.
     */
    public boolean isEnabled() {
        if (relevantTypes.isEmpty()) {
            Logging.debug("There are no EconomyTypes currently loaded into this Economy instance.");
            return false;
        }
        return relevantTypes.stream().anyMatch(EconomyType::isAvailable);
    }

    public void deposit(@NonNull OfflinePlayer player, double amount, boolean applyMultiplier) {
        relevantTypes.stream()
            .filter(EconomyType::isAvailable)
            .forEach(type -> type.deposit(player, amount, applyMultiplier));
    }

    /**
     * Deposits to every available economy. If one fails, previously successful
     * deposits are withdrawn. Independent providers cannot guarantee rollback,
     * so PARTIAL must be handled as a failed sale requiring reconciliation.
     */
    public @NonNull DepositResult depositForSale(@NonNull OfflinePlayer player, double amount, boolean applyMultiplier) {
        List<EconomyType> available = relevantTypes.stream().filter(EconomyType::isAvailable).toList();
        if (available.isEmpty()) {
            return DepositResult.FAILED;
        }

        List<EconomyType> credited = new ArrayList<>();
        for (EconomyType type : available) {
            try {
                if (type.deposit(player, amount, applyMultiplier)) {
                    credited.add(type);
                    continue;
                }
                LOGGER.warning("Sale deposit failed for economy " + type.getIdentifier() + " and player " + player.getUniqueId());
            } catch (RuntimeException exception) {
                LOGGER.log(Level.SEVERE, "Sale deposit threw for economy " + type.getIdentifier() + " and player " + player.getUniqueId(), exception);
            }
            return compensateSaleDeposit(player, amount, applyMultiplier, credited);
        }
        return DepositResult.SUCCESS;
    }

    private DepositResult compensateSaleDeposit(OfflinePlayer player, double amount, boolean applyMultiplier, List<EconomyType> credited) {
        boolean compensated = true;
        for (int index = credited.size() - 1; index >= 0; index--) {
            EconomyType type = credited.get(index);
            try {
                if (!type.withdraw(player, amount, applyMultiplier)) {
                    compensated = false;
                    LOGGER.severe("Could not reverse sale deposit for economy " + type.getIdentifier() + " and player " + player.getUniqueId());
                }
            } catch (RuntimeException exception) {
                compensated = false;
                LOGGER.log(Level.SEVERE, "Could not reverse sale deposit for economy " + type.getIdentifier() + " and player " + player.getUniqueId(), exception);
            }
        }
        return compensated ? DepositResult.FAILED : DepositResult.PARTIAL;
    }

    public void withdraw(@NonNull OfflinePlayer player, double amount, boolean applyMultiplier) {
        relevantTypes.forEach(type -> type.withdraw(player, amount, applyMultiplier));
    }

    public boolean has(@NonNull OfflinePlayer player, double amount) {
        return relevantTypes.stream()
            .filter(EconomyType::isAvailable)
            .allMatch(type -> type.has(player, amount));
    }

    /**
     * Gets the economy type registered to this instance with the given identifier.
     */
    public @NonNull Optional<EconomyType> getEconomyType(@NonNull String identifier) {
        for (EconomyType type : relevantTypes) {
            if (type.getIdentifier().equalsIgnoreCase(identifier)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public @NonNull Component getWorthFormat(double value, boolean applyMultiplier) {
        List<Component> components = getEconomyTypes().stream()
            .map(type -> type.formatWorth(value, applyMultiplier))
            .filter(Objects::nonNull)
            .toList();
        return Component.join(JoinConfiguration.commas(true), components);
    }

    /**
     * @deprecated Use {@link EconomyTypeRegistry#register(RegistryItem)} instead.
     */
    @Deprecated(forRemoval = true)
    public boolean registerEconomyType(@NonNull EconomyType economyType) {
        boolean registered = EMFRegistry.ECONOMY_TYPE.register(economyType);
        if (registered) {
            relevantTypes.add(economyType);
        }
        return registered;
    }

    /**
     * Checks if this Economy instance has any relevant EconomyTypes.
     * @return true if there are no relevant EconomyTypes.
     */
    public boolean isEmpty() {
        return relevantTypes.isEmpty();
    }

}
