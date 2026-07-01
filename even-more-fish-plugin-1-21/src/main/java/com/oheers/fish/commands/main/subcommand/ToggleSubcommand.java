package com.oheers.fish.commands.main.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.permissions.UserPerms;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class ToggleSubcommand {

    private final String name;

    public ToggleSubcommand(@NotNull String name) {
        this.name = name;
    }

    public ArgumentBuilder<CommandSourceStack, ?> get() {
        return Commands.literal(name)
            .requires(stack -> UserPerms.checkTogglePerms(stack.getSender()))
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                EvenMoreFish.getInstance().getToggle().performFishToggle(player);
                return 1;
            })
            .then(fishing())
            .then(bossbar())
            .then(catchMessage());
    }

    private ArgumentBuilder<CommandSourceStack, ?> fishing() {
        return Commands.literal("fishing")
            .requires(stack -> stack.getSender().hasPermission(UserPerms.TOGGLE_FISHING))
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                EvenMoreFish.getInstance().getToggle().performFishToggle(player);
                return 1;
            });
    }

    private ArgumentBuilder<CommandSourceStack, ?> bossbar() {
        return Commands.literal("bossbar")
            .requires(stack -> stack.getSender().hasPermission(UserPerms.TOGGLE_BOSSBAR))
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                EvenMoreFish.getInstance().getToggle().performBossBarToggle(player);
                return 1;
            });
    }

    private ArgumentBuilder<CommandSourceStack, ?> catchMessage() {
        return Commands.literal("catchMessage")
            .requires(stack -> stack.getSender().hasPermission(UserPerms.TOGGLE_CATCH_MESSAGE))
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                EvenMoreFish.getInstance().getToggle().performCatchMessageToggle(player);
                return 1;
            });
    }

}
