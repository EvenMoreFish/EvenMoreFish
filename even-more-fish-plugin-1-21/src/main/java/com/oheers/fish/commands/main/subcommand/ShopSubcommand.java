package com.oheers.fish.commands.main.subcommand;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.main.MainCommand;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.permissions.UserPerms;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.strokkur.commands.annotations.DefaultExecutes;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Permission(UserPerms.SHOP)
public class ShopSubcommand {

    @DefaultExecutes
    public void onDefault(CommandSender sender) {
        MainCommand.sendHelpMessage(sender);
    }

    @Executes
    public void execute(CommandSender sender, @Executor Player player, Player target) {
        execute(player, target);
    }

    @Executes
    public void execute(CommandSender sender, Player target) {
        performCommand(sender, target);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Executes
    public void execute(CommandSender sender) throws CommandSyntaxException {
        if (!(sender instanceof Player executor)) {
            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                Component.text("This command requires a player executor!")
            )).create();
        }
        performCommand(executor, executor);
    }

    private void performCommand(@NotNull CommandSender sender, @NotNull Player target) {
        if (!Economy.getInstance().isEnabled()) {
            ConfigMessage.ECONOMY_DISABLED.getMessage().send(sender);
            return;
        }
        new SellGui(target, SellGui.SellState.NORMAL, null).open();

        if (!target.equals(sender)) {
            EMFMessage message = ConfigMessage.ADMIN_OPEN_FISH_SHOP.getMessage();
            message.setPlayer(target);
            message.send(sender);
        }
    }

}
