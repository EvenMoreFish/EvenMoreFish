package com.oheers.fish.commands.main.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.oheers.fish.api.economy.selling.SellHelper;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.CommandUtils;
import com.oheers.fish.commands.arguments.EMFPlayerArgument;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class SellAllSubcommand {

    private final String name;

    public SellAllSubcommand(@NotNull String name) {
        this.name = name;
    }

    public ArgumentBuilder<CommandSourceStack, ?> get() {
        return Commands.literal(name)
            .requires(stack -> stack.getSender().hasPermission(UserPerms.SELL_ALL))
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
        if (CommandUtils.isEconomyEnabled(sender)) {
            new SellHelper(target.getInventory(), target).sell();
        }
    }

}
