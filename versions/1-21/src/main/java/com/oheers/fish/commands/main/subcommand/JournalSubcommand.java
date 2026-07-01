package com.oheers.fish.commands.main.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.arguments.RarityArgument;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.guis.FishJournalGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.permissions.UserPerms;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class JournalSubcommand {

    private final String name;

    public JournalSubcommand(@NotNull String name) {
        this.name = name;
    }

    public ArgumentBuilder<CommandSourceStack, ?> get() {
        return Commands.literal(name)
            .requires(stack -> stack.getSender().hasPermission(UserPerms.JOURNAL))
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                execute(player, null);
                return 1;
            })
            .then(withRarity());
    }

    private ArgumentBuilder<CommandSourceStack, ?> withRarity() {
        return Commands.argument("rarity", new RarityArgument())
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                Rarity rarity = ctx.getArgument("rarity", Rarity.class);
                execute(player, rarity);
                return 1;
            });
    }

    private void execute(@NotNull Player player, @Nullable Rarity rarity) {
        if (!DatabaseUtil.isDatabaseOnline()) {
            ConfigMessage.JOURNAL_DISABLED.getMessage().send(player);
            return;
        }
        new FishJournalGui(player, rarity).open();
    }

}
