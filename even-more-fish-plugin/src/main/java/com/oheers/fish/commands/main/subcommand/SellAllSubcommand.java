package com.oheers.fish.commands.main.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.oheers.fish.api.economy.selling.SellHelper;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.commands.EMFCommand;
import com.oheers.fish.commands.arguments.RarityArgument;
import com.oheers.fish.fishing.items.Rarity;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.jspecify.annotations.Nullable;
import uk.firedev.daisylib.command.CommandUtils;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("UnstableApiUsage")
public class SellAllSubcommand implements EMFCommand {

    private final String name;

    public SellAllSubcommand(@NonNull String name) {
        this.name = name;
    }

    public ArgumentBuilder<CommandSourceStack, ?> get() {
        return Commands.literal(name)
            .requires(stack -> stack.getSender().hasPermission(UserPerms.SELL_ALL))
            .executes(ctx -> {
                Player player = CommandUtils.requirePlayer(ctx);
                execute(player, player, null);
                return 1;
            })
            .then(
                Commands.argument("rarity", new RarityArgument())
                    .executes(ctx -> {
                        Player player = CommandUtils.requirePlayer(ctx);
                        IRarity rarity = ctx.getArgument("rarity", IRarity.class);
                        execute(player, player, rarity);
                        return 1;
                    })
                    .then(
                        Commands.argument("target", ArgumentTypes.player())
                            .requires(stack -> stack.getSender().hasPermission(AdminPerms.ADMIN))
                            .executes(ctx -> {
                                Player target = CommandUtils.parsePlayerArgument(
                                    ctx.getSource(),
                                    ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
                                );
                                IRarity rarity = ctx.getArgument("rarity", IRarity.class);
                                execute(ctx.getSource().getSender(), target, rarity);
                                return 1;
                            })
                    )
            )
            .then(
                Commands.argument("target", ArgumentTypes.player())
                    .requires(stack -> stack.getSender().hasPermission(AdminPerms.ADMIN))
                    .executes(ctx -> {
                        Player target = CommandUtils.parsePlayerArgument(
                            ctx.getSource(),
                            ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
                        );
                        execute(ctx.getSource().getSender(), target, null);
                        return 1;
                    })
            );
    }

    private void execute(@NonNull CommandSender sender, @NonNull Player target, @Nullable IRarity rarity) {
        if (isEconomyEnabled(sender)) {
            SellHelper.get().sell(target.getInventory(), target, f -> rarity == null || f.getRarity().equals(rarity));
        }
    }

}
