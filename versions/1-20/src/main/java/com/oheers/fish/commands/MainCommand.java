package com.oheers.fish.commands;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.Toggle;
import com.oheers.fish.commands.arguments.ArgumentHelper;
import com.oheers.fish.commands.arguments.RarityArgument;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.guis.ApplyBaitsGui;
import com.oheers.fish.gui.guis.FishJournalGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.PrefixType;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import com.oheers.fish.api.economy.selling.SellHelper;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.entity.Player;

import java.util.Objects;

import static com.oheers.fish.commands.MainCommandProvider.sendHelpMessage;

public class MainCommand {

    private final CommandAPICommand command;

    public MainCommand() {
        // Add the admin command to the help message
        String adminName = MainConfig.getInstance().getAdminSubCommandName();

        this.command = new CommandAPICommand(MainConfig.getInstance().getMainCommandName())
                .withAliases(MainConfig.getInstance().getMainCommandAliases().toArray(String[]::new))
                .withSubcommands(
                        getNext(),
                        getToggle(),
                        getGui(),
                        getHelp(),
                        getTop(),
                        getShop(),
                        getSellAll(),
                        getApplyBaits(),
                        getJournal(),
                        new AdminCommand(adminName).getCommand()
                )
                .executesPlayer(info -> {
                    if (!info.sender().hasPermission(UserPerms.GUI) || MainConfig.getInstance().useOldBaseCommandBehavior()) {
                        sendHelpMessage(info.sender());
                    } else {
                        new MainMenuGui(info.sender()).open();
                    }
                })
                .executes(info -> {
                    sendHelpMessage(info.sender());
                });
    }

    public CommandAPICommand getCommand() {
        return command;
    }

    private CommandAPICommand getNext() {
        String name = MainConfig.getInstance().getNextSubCommandName();
        return new CommandAPICommand(name)
            .withRequirement(sender -> sender.hasPermission(UserPerms.NEXT) && EvenMoreFish.getInstance().getCompetitionQueue().hasTimings())
            .withPermission(UserPerms.NEXT)
            .executes(info -> {
                EMFMessage message = Competition.getNextCompetitionMessage();
                message.prependMessage(PrefixType.DEFAULT.getPrefix());
                message.send(info.sender());
            });
    }

    private CommandAPICommand getToggle() {
        String name = MainConfig.getInstance().getToggleSubCommandName();
        return new CommandAPICommand(name)
            .withRequirement(UserPerms::checkTogglePerms)
            .withArguments(new MultiLiteralArgument("toggle", "fishing", "bossbar", "catchMessage").setOptional(true))
            .executesPlayer(info -> {
                Toggle toggle = EvenMoreFish.getInstance().getToggle();
                String toggleType = info.args().getUnchecked("toggle");
                switch (toggleType) {
                    case "bossbar" -> toggle.performBossBarToggle(info.sender());
                    case "catchMessage" -> toggle.performCatchMessageToggle(info.sender());
                    case null, default -> toggle.performFishToggle(info.sender());
                }
            });
    }

    private CommandAPICommand getGui() {
        String name = MainConfig.getInstance().getGuiSubCommandName();
        return new CommandAPICommand(name)
            .withPermission(UserPerms.GUI)
            .executesPlayer(info -> {
                new MainMenuGui(info.sender()).open();
            });
    }

    private CommandAPICommand getHelp() {
        String name = MainConfig.getInstance().getHelpSubCommandName();
        return new CommandAPICommand(name)
            .withPermission(UserPerms.HELP)
            .executes(info -> {
                sendHelpMessage(info.sender());
            });
    }

    private CommandAPICommand getTop() {
        String name = MainConfig.getInstance().getTopSubCommandName();
        return new CommandAPICommand(name)
            .withPermission(UserPerms.TOP)
            .executes(info -> {
                Competition active = Competition.getCurrentlyActive();
                if (active == null) {
                    ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(info.sender());
                    return;
                }
                active.sendLeaderboard(info.sender());
            });
    }


    private CommandAPICommand getShop() {
        String name = MainConfig.getInstance().getShopSubCommandName();
        return new CommandAPICommand(name)
            .withPermission(UserPerms.SHOP)
            .withArguments(
                ArgumentHelper.getPlayerArgument("target").setOptional(true)
            )
            .executes((sender, args) -> {
                Player player = args.getUnchecked("target");
                if (player == null) {
                    if (!(sender instanceof Player p)) {
                        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
                        return;
                    }
                    player = p;
                }
                if (CommandUtils.isEconomyDisabled(player)) {
                    return;
                }
                if (sender == player) {
                    new SellGui(player, SellGui.SellState.NORMAL, null).open();
                    return;
                }
                if (!sender.hasPermission(AdminPerms.ADMIN)) {
                    ConfigMessage.NO_PERMISSION.getMessage().send(sender);
                    return;
                }
                new SellGui(player, SellGui.SellState.NORMAL, null).open();
                EMFMessage message = ConfigMessage.ADMIN_OPEN_FISH_SHOP.getMessage();
                message.setPlayer(player);
                message.send(sender);
            });
    }


    private CommandAPICommand getSellAll() {
        String name = MainConfig.getInstance().getSellAllSubCommandName();
        return new CommandAPICommand(name)
            .withPermission(UserPerms.SELL_ALL)
            .withArguments(new EntitySelectorArgument.OnePlayer("target").setOptional(false))
            .executesPlayer(info -> {
                Player player = info.sender();
                if (CommandUtils.isEconomyEnabled(player)) {
                    new SellHelper(player.getInventory(), player).sell();
                }
            })
            .executes(info -> {
                if (!info.sender().hasPermission(AdminPerms.ADMIN)) {
                    return;
                }
                Player player = Objects.requireNonNull(info.args().getUnchecked("target"));
                if (CommandUtils.isEconomyEnabled(info.sender())) {
                    new SellHelper(player.getInventory(), player).sell();
                }
            });
    }


    private CommandAPICommand getApplyBaits() {
        String name = MainConfig.getInstance().getApplyBaitsSubCommandName();
        return new CommandAPICommand(name)
            .withPermission(UserPerms.APPLYBAITS)
            .executesPlayer(info -> {
                Player player = info.sender();
                if (!Checks.canUseRod(player.getInventory().getItemInMainHand())) {
                    ConfigMessage.BAIT_INVALID_ROD.getMessage().send(player);
                    return;
                }
                new ApplyBaitsGui(player, null).open();
            });
    }


    private CommandAPICommand getJournal() {
        String name = MainConfig.getInstance().getJournalSubCommandName();
        return new CommandAPICommand(name)
            .withPermission(UserPerms.JOURNAL)
            .withArguments(
                RarityArgument.create().setOptional(true)
            )
            .executesPlayer(info -> {
                if (!DatabaseUtil.isDatabaseOnline()) {
                    ConfigMessage.JOURNAL_DISABLED.getMessage().send(info.sender());
                    return;
                }
                Rarity rarity = info.args().getUnchecked("rarity"); // This is allowed to be null.
                new FishJournalGui(info.sender(), rarity).open();
            });
    }

}
