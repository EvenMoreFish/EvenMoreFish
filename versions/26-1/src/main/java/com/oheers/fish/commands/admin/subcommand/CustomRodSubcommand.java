package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.FishUtils;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.CommandUtils;
import com.oheers.fish.commands.arguments.CustomRodArgument;
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
public class CustomRodSubcommand {

    private final String name;

    public CustomRodSubcommand(@NotNull String name) {
        this.name = name;
    }

    public LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("custom-rod")
            .then(
                Commands.argument("rod", new CustomRodArgument())
                    // [rod]
                    .executes(ctx -> execute(ctx, false))
                    .then(
                        Commands.argument("targets", ArgumentTypes.players())
                            // [rod] [targets]
                            .executes(ctx -> execute(ctx, true))
                    )
            );
    }

    private int execute(@NotNull CommandContext<CommandSourceStack> ctx, boolean allowConsole) throws CommandSyntaxException {
        CommandSender sender = allowConsole ? ctx.getSource().getSender() : BrigCommandUtils.requirePlayer(ctx);
        CustomRod rod = ctx.getArgument("rod", CustomRod.class);
        PlayerSelectorArgumentResolver targets = BrigCommandUtils.getArgumentOrNull(ctx, "targets", PlayerSelectorArgumentResolver.class);
        return execute(
            sender,
            rod,
            BrigCommandUtils.resolvePlayers(ctx, targets)
        );
    }

    private int execute(@NotNull CommandSender sender, @NotNull CustomRod rod, @NotNull List<Player> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) {
            if (!(sender instanceof Player player)) {
                throw BrigCommandUtils.ERROR_NO_PLAYERS.create();
            }
            targets = List.of(player);
        }

        ItemStack rodItem = rod.create();

        for (Player player : targets) {
            FishUtils.giveItems(List.of(rodItem), player);
        }

        EMFMessage giveMessage = ConfigMessage.ADMIN_CUSTOM_ROD_GIVEN.getMessage();
        giveMessage.setVariable("{player}", CommandUtils.getPlayersVariable(targets));

        giveMessage.send(sender);
        return 1;
    }

}
