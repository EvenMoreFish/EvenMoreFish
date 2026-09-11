package com.oheers.fish.commands.main;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.commands.EMFCommand;
import com.oheers.fish.commands.HelpMessage;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.gui.guis.ApplyBaitsGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.PrefixType;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.oheers.fish.commands.admin.AdminCommand;
import com.oheers.fish.commands.main.subcommand.JournalSubcommand;
import com.oheers.fish.commands.main.subcommand.SellAllSubcommand;
import com.oheers.fish.commands.main.subcommand.ShopSubcommand;
import com.oheers.fish.commands.main.subcommand.ToggleSubcommand;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.command.CommandUtils;

// Safe to suppress - Newer API versions are identical and stable.
@SuppressWarnings("UnstableApiUsage")
public class MainCommand implements EMFCommand {

    private static final HelpMessage HELP_MESSAGE = HelpMessage.helpMessage()
        .addEntry(MainConfig.getInstance().getAdminSubCommandName(), ConfigMessage.HELP_GENERAL_ADMIN::getMessage, AdminPerms.ADMIN)
        .addEntry(MainConfig.getInstance().getHelpSubCommandName(), ConfigMessage.HELP_GENERAL_HELP::getMessage, UserPerms.HELP)
        .addEntry(MainConfig.getInstance().getGuiSubCommandName(), ConfigMessage.HELP_GENERAL_GUI::getMessage, UserPerms.GUI)
        .addEntry(MainConfig.getInstance().getTopSubCommandName(), ConfigMessage.HELP_GENERAL_TOP::getMessage, UserPerms.TOP)
        .addEntry(MainConfig.getInstance().getSellAllSubCommandName(), ConfigMessage.HELP_GENERAL_SELLALL::getMessage, UserPerms.SELL_ALL)
        .addEntry(MainConfig.getInstance().getApplyBaitsSubCommandName(), ConfigMessage.HELP_GENERAL_APPLYBAITS::getMessage, UserPerms.APPLYBAITS)
        .addEntry(MainConfig.getInstance().getJournalSubCommandName(), ConfigMessage.HELP_GENERAL_JOURNAL::getMessage, UserPerms.JOURNAL)
        .addEntry(MainConfig.getInstance().getNextSubCommandName(), ConfigMessage.HELP_GENERAL_NEXT::getMessage, UserPerms.NEXT)
        .addEntry(MainConfig.getInstance().getToggleSubCommandName(), ConfigMessage.HELP_GENERAL_TOGGLE::getMessage, UserPerms.TOGGLE)
        .addEntry(MainConfig.getInstance().getShopSubCommandName(), ConfigMessage.HELP_GENERAL_SHOP::getMessage, UserPerms.SHOP);

    public @NonNull LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal(MainConfig.getInstance().getMainCommandName())
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

    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> admin() {
        return new AdminCommand(MainConfig.getInstance().getAdminSubCommandName()).getAsArgument();
    }

    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> shop() {
        return new ShopSubcommand(MainConfig.getInstance().getShopSubCommandName()).get();
    }

    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> journal() {
        return new JournalSubcommand(MainConfig.getInstance().getJournalSubCommandName()).get();
    }

    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> toggle() {
        return new ToggleSubcommand(MainConfig.getInstance().getToggleSubCommandName()).get();
    }

    public @NonNull ArgumentBuilder<CommandSourceStack, ?> next() {
        return Commands.literal(MainConfig.getInstance().getNextSubCommandName())
            .requires(stack -> stack.getSender().hasPermission(UserPerms.NEXT) && EvenMoreFish.getInstance().getCompetitionQueue().hasTimings())
            .executes(ctx -> {
                EMFMessage message = Competition.getNextCompetitionMessage();
                message.prependMessage(PrefixType.DEFAULT.getPrefix());
                message.send(ctx.getSource().getSender());
                return 1;
            });
    }

    public @NonNull ArgumentBuilder<CommandSourceStack, ?> help() {
        return Commands.literal(MainConfig.getInstance().getHelpSubCommandName())
            .requires(stack -> stack.getSender().hasPermission(UserPerms.HELP))
            .executes(ctx -> {
                sendHelpMessage(ctx.getSource().getSender());
                return 1;
            });
    }

    public @NonNull ArgumentBuilder<CommandSourceStack, ?> gui() {
        return Commands.literal(MainConfig.getInstance().getGuiSubCommandName())
            .requires(stack -> stack.getSender().hasPermission(UserPerms.GUI))
            .executes(ctx -> {
                Player player = CommandUtils.requirePlayer(ctx);
                new MainMenuGui(player).open();
                return 1;
            });
    }

    public @NonNull ArgumentBuilder<CommandSourceStack, ?> top() {
        return Commands.literal(MainConfig.getInstance().getTopSubCommandName())
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

    public @NonNull ArgumentBuilder<CommandSourceStack, ?> sellAll() {
        return new SellAllSubcommand(MainConfig.getInstance().getSellAllSubCommandName()).get();
    }

    public @NonNull ArgumentBuilder<CommandSourceStack, ?> applyBaits() {
        return Commands.literal(MainConfig.getInstance().getApplyBaitsSubCommandName())
            .requires(stack -> stack.getSender().hasPermission(UserPerms.APPLYBAITS))
            .executes(ctx -> {
                Player player = CommandUtils.requirePlayer(ctx);
                if (!Checks.canUseRod(player.getInventory().getItemInMainHand())) {
                    ConfigMessage.BAIT_INVALID_ROD.getMessage().send(player);
                    return 1;
                }
                new ApplyBaitsGui(player, null).open();
                return 1;
            });
    }

    public static void sendHelpMessage(CommandSender sender) {
        HELP_MESSAGE.send(sender);
    }

}
