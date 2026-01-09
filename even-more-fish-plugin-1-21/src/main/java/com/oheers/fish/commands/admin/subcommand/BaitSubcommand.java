package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.FishUtils;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.CommandUtils;
import com.oheers.fish.commands.arguments.BaitArgument;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class BaitSubcommand {

    private final String name;

    public BaitSubcommand(@NotNull String name) {
        this.name = name;
    }

    public LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(name)
            .then(
                Commands.argument("bait", new BaitArgument())
                    // [bait]
                    .executes(ctx -> execute(ctx, false))
                    .then(
                        // [bait] [quantity]
                        Commands.argument("quantity", IntegerArgumentType.integer(1))
                            .executes(ctx -> execute(ctx, false))
                            .then(
                                Commands.argument("targets", ArgumentTypes.players())
                                    // [bait] [quantity] [targets]
                                    .executes(ctx -> execute(ctx, true))
                            )
                    )
            );
    }

    private int execute(@NotNull CommandContext<CommandSourceStack> ctx, boolean allowConsole) throws CommandSyntaxException {
        CommandSender sender = allowConsole ? ctx.getSource().getSender() : BrigCommandUtils.requirePlayer(ctx);
        BaitHandler bait = ctx.getArgument("bait", BaitHandler.class);
        int quantity = BrigCommandUtils.getArgumentOrDefault(ctx, "quantity", int.class, 1);
        PlayerSelectorArgumentResolver targets = BrigCommandUtils.getArgumentOrNull(ctx, "targets", PlayerSelectorArgumentResolver.class);
        return execute(
            sender,
            bait,
            quantity,
            BrigCommandUtils.resolvePlayers(ctx, targets)
        );
    }

    private int execute(CommandSender sender, BaitHandler bait, int quantity, List<Player> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) {
            if (!(sender instanceof Player player)) {
                throw BrigCommandUtils.ERROR_NO_PLAYERS.create();
            }
            targets = List.of(player);
        }
        for (Player target : targets) {
            ItemStack baitItem = bait.create(target);
            baitItem.setAmount(quantity);
            FishUtils.giveItems(List.of(baitItem), target);
        }
        EMFMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_BAIT.getMessage();
        message.setVariable("{player}", CommandUtils.getPlayersVariable(targets));
        message.setBait(bait.getId());
        message.send(sender);
        return 1;
    }

}
