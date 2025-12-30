package com.oheers.fish;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.ItemConfigResolver;
import com.oheers.fish.items.configs.FireResistantItemConfig;
import com.oheers.fish.items.configs.HideTooltipItemConfig;
import com.oheers.fish.items.configs.ItemRarityItemConfig;
import com.oheers.fish.items.configs.ModernGlowingItemConfig;
import com.oheers.fish.permissions.AdminPerms;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class EMFModule extends EvenMoreFish{

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
            registerMainCommand(event.registrar());
            registerAdminCommand(event.registrar());
        }));
    }

    private void registerMainCommand(@NotNull Commands registrar) {
        String altName = MainConfig.getInstance().getMainCommandName();
        List<String> aliases = new ArrayList<>(MainConfig.getInstance().getMainCommandAliases());
        if (altName != null) {
            aliases.add(altName);
        }
        // Register the main command
        LiteralCommandNode<CommandSourceStack> main = MainCommandBrigadier.create();
        registrar.register(main, null, aliases);
    }

    private void registerAdminCommand(@NotNull Commands registrar) {
        // Register the admin shortcut command
        if (!MainConfig.getInstance().isAdminShortcutCommandEnabled()) {
            return;
        }
        String adminShortcut = MainConfig.getInstance().getAdminShortcutCommandName();
        LiteralCommandNode<CommandSourceStack> redirect = Commands.literal(adminShortcut)
            .requires(stack -> stack.getSender().hasPermission(AdminPerms.ADMIN))
            .redirect(AdminCommandBrigadier.create())
            .build();
        registrar.register(redirect);
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
    public void disableCommands() {
        //nothing
    }
}
