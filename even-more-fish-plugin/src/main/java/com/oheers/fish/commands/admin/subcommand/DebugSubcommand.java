package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.baits.BaitHandler;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.oheers.fish.commands.arguments.BaitArgument;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.command.CommandUtils;

@SuppressWarnings("UnstableApiUsage")
public class DebugSubcommand {

    private final String name;

    public DebugSubcommand(@NonNull String name) {
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
                                Commands.argument("target", ArgumentTypes.player())
                                    .executes(this::execute)
                            )
                    )
            );
    }

    private int execute(@NonNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();
        BaitHandler bait = ctx.getArgument("bait", BaitHandler.class);
        Player target;
        try {
            target = CommandUtils.parsePlayerArgument(
                ctx.getSource(),
                ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
            );
        } catch (CommandSyntaxException exception) {
            target = CommandUtils.requirePlayer(ctx);
        }

        RequirementContext context = new RequirementContext(
            target.getWorld(),
            target.getLocation(),
            target,
            null,
            null,
            null
        );
        bait.createDebugMessages(target, context).forEach(sender::sendMessage);
        return 1;
    }

}
