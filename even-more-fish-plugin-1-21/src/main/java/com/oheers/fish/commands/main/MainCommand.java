package com.oheers.fish.commands.main;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oheers.fish.Checks;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.CommandUtils;
import com.oheers.fish.commands.HelpMessageBuilder;
import com.oheers.fish.commands.MainCommandProvider;
import com.oheers.fish.commands.admin.AdminCommand;
import com.oheers.fish.commands.main.subcommand.JournalSubcommand;
import com.oheers.fish.commands.main.subcommand.ShopSubcommand;
import com.oheers.fish.commands.main.subcommand.ToggleSubcommand;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.gui.guis.ApplyBaitsGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.PrefixType;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.permissions.UserPerms;
import com.oheers.fish.selling.SellHelper;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// Safe to suppress - Newer API versions are identical and stable.
@SuppressWarnings("UnstableApiUsage")
public class MainCommand extends MainCommandProvider<CommandNode<CommandSourceStack>, ArgumentBuilder<CommandSourceStack, ?>> {

    public @NotNull LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal(commandName())
            .executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                if (!(sender instanceof Player player)) {
                    sendHelpMessage(sender);
                    return 1;
                }
                if (!player.hasPermission(UserPerms.GUI) || MainConfig.getInstance().useOldBaseCommandBehavior()) {
                    sendHelpMessage(player);
                    return 1;
                }
                new MainMenuGui(player).open();
                return 1;
            })
            .then(admin())
            .then(shop())
            .then(journal())
            .then(toggle())
            .then(next())
            .then(help())
            .then(gui())
            .then(top())
            .then(sellAll())
            .then(applyBaits())
            .build();
    }

    @Override
    protected @NotNull ArgumentBuilder<CommandSourceStack, ?> admin() {
        return new AdminCommand(adminName()).getAsArgument();
    }

    @Override
    protected @NotNull ArgumentBuilder<CommandSourceStack, ?> shop() {
        return new ShopSubcommand(shopName()).get();
    }

    @Override
    protected @NotNull ArgumentBuilder<CommandSourceStack, ?> journal() {
        return new JournalSubcommand(journalName()).get();
    }

    @Override
    protected @NotNull ArgumentBuilder<CommandSourceStack, ?> toggle() {
        return new ToggleSubcommand(toggleName()).get();
    }

    @Override
    public @NotNull ArgumentBuilder<CommandSourceStack, ?> next() {
        return Commands.literal(nextName())
            .requires(stack -> stack.getSender().hasPermission(UserPerms.NEXT))
            .executes(ctx -> {
                EMFMessage message = Competition.getNextCompetitionMessage();
                message.prependMessage(PrefixType.DEFAULT.getPrefix());
                message.send(ctx.getSource().getSender());
                return 1;
            });
    }

    @Override
    public @NotNull ArgumentBuilder<CommandSourceStack, ?> help() {
        return Commands.literal(helpName())
            .requires(stack -> stack.getSender().hasPermission(UserPerms.HELP))
            .executes(ctx -> {
                sendHelpMessage(ctx.getSource().getSender());
                return 1;
            });
    }

    @Override
    public @NotNull ArgumentBuilder<CommandSourceStack, ?> gui() {
        return Commands.literal(guiName())
            .requires(stack -> stack.getSender().hasPermission(UserPerms.GUI))
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                new MainMenuGui(player).open();
                return 1;
            });
    }

    @Override
    public @NotNull ArgumentBuilder<CommandSourceStack, ?> top() {
        return Commands.literal(topName())
            .requires(stack -> stack.getSender().hasPermission(UserPerms.TOP))
            .executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                Competition active = Competition.getCurrentlyActive();
                if (active == null) {
                    ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(sender);
                    return 1;
                }
                active.sendLeaderboard(sender);
                return 1;
            });
    }

    @Override
    public @NotNull ArgumentBuilder<CommandSourceStack, ?> sellAll() {
        return Commands.literal(sellAllName())
            .requires(stack -> stack.getSender().hasPermission(UserPerms.SELL_ALL))
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                if (CommandUtils.isEconomyEnabled(player)) {
                    new SellHelper(player.getInventory(), player).sell();
                }
                return 1;
            });
    }

    @Override
    public @NotNull ArgumentBuilder<CommandSourceStack, ?> applyBaits() {
        return Commands.literal(applyBaitsName())
            .requires(stack -> stack.getSender().hasPermission(UserPerms.APPLYBAITS))
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                if (!Checks.canUseRod(player.getInventory().getItemInMainHand())) {
                    ConfigMessage.BAIT_INVALID_ROD.getMessage().send(player);
                    return 1;
                }
                new ApplyBaitsGui(player, null).open();
                return 1;
            });
    }

}
