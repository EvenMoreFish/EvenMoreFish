package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.oheers.fish.FishUtils;
import com.oheers.fish.commands.arguments.CompetitionFileArgument;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class CompetitionSubcommand {

    private final String name;

    public CompetitionSubcommand(@NotNull String name) {
        this.name = name;
    }

    public LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(name)
            .then(start())
            .then(end())
            .then(test())
            .then(extend());
    }

    private ArgumentBuilder<CommandSourceStack, ?> start() {
        return Commands.literal("start")
            .then(
                Commands.argument("file", new CompetitionFileArgument())
                    // [competition]
                    .executes(ctx -> {
                        CompetitionFile file = ctx.getArgument("file", CompetitionFile.class);
                        start(ctx.getSource().getSender(), file, null);
                        return 1;
                    })
                    .then(
                        Commands.argument("duration", IntegerArgumentType.integer(1))
                            // [competition] [duration]
                            .executes(ctx -> {
                                CompetitionFile file = ctx.getArgument("file", CompetitionFile.class);
                                int duration = ctx.getArgument("duration", int.class);
                                start(ctx.getSource().getSender(), file, duration);
                                return 1;
                            })
                    )
            );
    }

    private void start(@NotNull CommandSender sender, @NotNull CompetitionFile file, @Nullable Integer duration) {
        if (Competition.isActive()) {
            ConfigMessage.COMPETITION_ALREADY_RUNNING.getMessage().send(sender);
            return;
        }
        Competition competition = new Competition(file);
        competition.setAdminStarted(true);
        if (duration != null) {
            competition.setMaxDuration(duration);
        }
        competition.begin();
    }

    private ArgumentBuilder<CommandSourceStack, ?> end() {
        return Commands.literal("end")
            .executes(ctx -> {
                Competition active = Competition.getCurrentlyActive();
                if (active == null) {
                    ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(ctx.getSource().getSender());
                    return 1;
                }
                active.end(false);
                return 1;
            });
    }

    private ArgumentBuilder<CommandSourceStack, ?> test() {
        return Commands.literal("test")
            // No args
            .executes(ctx -> {
                test(ctx.getSource().getSender(), 60, CompetitionType.LARGEST_FISH);
                return 1;
            })
            .then(
                Commands.argument("duration", IntegerArgumentType.integer(1))
                    // [duration]
                    .executes(ctx -> {
                        int duration = ctx.getArgument("duration", int.class);
                        test(ctx.getSource().getSender(), duration, CompetitionType.LARGEST_FISH);
                        return 1;
                    })
                    .then(
                        Commands.argument("type", new CompetitionFileArgument())
                            // [duration] [type]
                            .executes(ctx -> {
                                int duration = ctx.getArgument("duration", int.class);
                                CompetitionType type = ctx.getArgument("type", CompetitionType.class);
                                test(ctx.getSource().getSender(), duration, type);
                                return 1;
                            })
                    )
            );
    }

    private void test(CommandSender sender, int duration, CompetitionType type) {
        if (Competition.isActive()) {
            ConfigMessage.COMPETITION_ALREADY_RUNNING.getMessage().send(sender);
            return;
        }
        CompetitionFile file = new CompetitionFile("adminTest", type, duration);
        Competition competition = new Competition(file);
        competition.setAdminStarted(true);
        competition.begin();
    }

    private ArgumentBuilder<CommandSourceStack, ?> extend() {
        return Commands.literal("extend")
            .then(
                Commands.argument("duration", IntegerArgumentType.integer(1))
                    .executes(ctx -> {
                        int duration = ctx.getArgument("duration", int.class);
                        Competition active = Competition.getCurrentlyActive();
                        if (active == null) {
                            ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(ctx.getSource().getSender());
                            return 1;
                        }
                        active.addTime(duration);

                        EMFMessage message = ConfigMessage.COMPETITION_TIME_EXTENDED.getMessage();
                        message.setVariable("{duration}", FishUtils.timeFormat(duration));
                        message.broadcast();
                        return 1;
                    })
            );
    }

}
