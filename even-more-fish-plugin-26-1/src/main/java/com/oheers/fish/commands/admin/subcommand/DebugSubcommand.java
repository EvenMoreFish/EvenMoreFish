package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.arguments.BaitArgument;
import com.oheers.fish.commands.arguments.EMFPlayerArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class DebugSubcommand {

    private final String name;

    public DebugSubcommand(@NotNull String name) {
        this.name = name;
    }

    public LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(name)
            .then(
                Commands.literal("bait")
                    .then(
                        Commands.argument("bait", new BaitArgument())
                            .executes(this::execute)
                            .then(
                                Commands.argument("target", new EMFPlayerArgument())
                                    .executes(this::execute)
                            )
                    )
            );
    }

    private int execute(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();
        BaitHandler bait = ctx.getArgument("bait", BaitHandler.class);
        Player target = BrigCommandUtils.getArgumentOrNull(ctx, "target", Player.class);
        if (target == null) {
            target = BrigCommandUtils.requirePlayer(ctx);
        }

        bait.createDebugMessages(target).forEach(sender::sendMessage);
        return 1;
    }

}
