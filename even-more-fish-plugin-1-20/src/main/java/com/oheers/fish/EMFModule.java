package com.oheers.fish;

import com.oheers.fish.commands.AdminCommand;
import com.oheers.fish.commands.MainCommand;
import com.oheers.fish.config.MainConfig;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;

public class EMFModule extends EvenMoreFish {
    @Override
    public void loadCommands() {
        CommandAPIBukkitConfig config = new CommandAPIBukkitConfig(this)
                .shouldHookPaperReload(true)
                .missingExecutorImplementationMessage("You are not able to use this command!");
        CommandAPI.onLoad(config);
    }

    @Override
    public void enableCommands() {
        CommandAPI.onEnable();
    }

    @Override
    public void registerCommands() {
        new MainCommand().getCommand().register(this);

        // Shortcut command for /emf admin
        if (MainConfig.getInstance().isAdminShortcutCommandEnabled()) {
            new AdminCommand(
                    MainConfig.getInstance().getAdminShortcutCommandName()
            ).getCommand().register(this);
        }
    }

    @Override
    public void disableCommands() {
        CommandAPI.onDisable();
    }
}
