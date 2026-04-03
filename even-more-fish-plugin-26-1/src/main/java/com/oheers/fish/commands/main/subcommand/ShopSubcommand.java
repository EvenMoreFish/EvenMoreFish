package com.oheers.fish.commands.main.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.arguments.EMFPlayerArgument;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// Required branches:
// /emf shop - Opens the shop for the sender
// /emf shop [target] - Opens the shop for the target. Requires admin permissions.
@SuppressWarnings("UnstableApiUsage")
public class ShopSubcommand {

    private final String name;

    public ShopSubcommand(@NotNull String name) {
        this.name = name;
    }

    public ArgumentBuilder<CommandSourceStack, ?> get() {
        return Commands.literal(name)
            .requires(stack -> stack.getSender().hasPermission(UserPerms.SHOP))
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                execute(player, player);
                return 1;
            })
            .then(
                Commands.argument("target", new EMFPlayerArgument())
                    .requires(stack -> stack.getSender().hasPermission(AdminPerms.ADMIN))
                    .executes(ctx -> {
                        Player target = ctx.getArgument("target", Player.class);
                        execute(ctx.getSource().getSender(), target);
                        return 1;
                    })
            );
    }

    private void execute(@NotNull CommandSender sender, @NotNull Player target) {
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
