package com.oheers.fish;

import com.oheers.fish.commands.admin.AdminCommand;
import com.oheers.fish.commands.main.MainCommand;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.ItemConfigResolver;
import com.oheers.fish.items.configs.FireResistantItemConfig;
import com.oheers.fish.items.configs.HideTooltipItemConfig;
import com.oheers.fish.items.configs.ItemRarityItemConfig;
import com.oheers.fish.items.configs.ModernGlowingItemConfig;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EMFModule extends EvenMoreFish {

    @Override
    public void onLoad() {
        registerItemConfigs();
        super.onLoad();
    }

    private void registerItemConfigs() {
        ItemConfigResolver inst = ItemConfigResolver.getInstance();
        inst.setGlowingResolver(ModernGlowingItemConfig::new);
        inst.setFireResistantResolver(FireResistantItemConfig::new);
        inst.setHideTooltipResolver(HideTooltipItemConfig::new);
        inst.setItemRarityResolver(ItemRarityItemConfig::new);
    }

    @Override
    public void loadCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            event.registrar().register(new MainCommand().get(), MainConfig.getInstance().getMainCommandAliases());
            if (MainConfig.getInstance().isAdminShortcutCommandEnabled()) {
                String shortcut = MainConfig.getInstance().getAdminShortcutCommandName();
                event.registrar().register(new AdminCommand(shortcut).get());
            }
        }));
    }

    @Override
    public void enableCommands() {
        //nothing
    }

    @Override
    public void registerCommands() {
        //nothing, we register in onLoad()
    }

    @Override
    public void resendCommands() {
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    @Override
    public void disableCommands() {
        //nothing
    }

}
