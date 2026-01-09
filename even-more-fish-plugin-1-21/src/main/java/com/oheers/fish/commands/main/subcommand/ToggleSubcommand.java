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
            .requires(stack -> stack.getSender().hasPermission(UserPerms.TOGGLE))
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                EvenMoreFish.getInstance().getToggle().performFishToggle(player);
                return 1;
            })
            .then(fishing())
            .then(bossbar());
    }

    private ArgumentBuilder<CommandSourceStack, ?> fishing() {
        return Commands.literal("fishing")
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                EvenMoreFish.getInstance().getToggle().performFishToggle(player);
                return 1;
            });
    }

    private ArgumentBuilder<CommandSourceStack, ?> bossbar() {
        return Commands.literal("bossbar")
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                EvenMoreFish.getInstance().getToggle().performBossBarToggle(player);
                return 1;
            });
    }

}
