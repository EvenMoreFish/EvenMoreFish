package com.oheers.fish.events;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.plugin.DependencyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author sarhatabaot
 */
public class EconomyServiceRegisterListener implements Listener {
    private final DependencyManager dependencyManager;

    public EconomyServiceRegisterListener(DependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
    }

    @EventHandler
    public void onEconomyServiceRegister(@NotNull ServiceRegisterEvent event) {
        if (EMFRegistry.ECONOMY_TYPE.get("Vault") != null) {
            // Do not overwrite if it already exists.
            return;
        }

        Logging.debug("A service has been registered.");

        if (!dependencyManager.isUsingVault()) {
            Logging.debug("Vault is not enabled.");
            return;
        }

        if (!(event.getProvider().getProvider() instanceof Economy economy)) {
            Logging.debug("Service was not an Economy. Ignoring.");
            return;
        }

        Logging.debug("Attempting to load Vault with new Economy: " + economy.getName());
        dependencyManager.loadVaultEconomy();
    }
}
