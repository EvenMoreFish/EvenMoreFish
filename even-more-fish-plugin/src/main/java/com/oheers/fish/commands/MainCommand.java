package com.oheers.fish.commands;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.commands.arguments.ArgumentHelper;
import com.oheers.fish.commands.arguments.RarityArgument;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.guis.journal.FishJournalGui;
import com.oheers.fish.gui.guis.ApplyBaitsGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.PrefixType;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import com.oheers.fish.selling.SellHelper;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MainCommand {

    private final HelpMessageBuilder helpMessageBuilder = HelpMessageBuilder.create();

    private final CommandAPICommand command;

    public MainCommand() {
        // Add the admin command to the help message
        String adminName = MainConfig.getInstance().getAdminSubCommandName();
        helpMessageBuilder.addUsage(
            adminName,
            ConfigMessage.HELP_GENERAL_ADMIN::getMessage
        );

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
                        new AdminCommand("admin").getCommand()
                )
                .executes(info -> {
                    sendHelpMessage(info.sender());
                });
        if (MainConfig.getInstance().isDatabaseOnline()) {
            this.command.withSubcommand(getJournal());
        }
    }

    public CommandAPICommand getCommand() {
        return command;
    }

    private CommandAPICommand getNext() {
        String name = MainConfig.getInstance().getNextSubCommandName();
        helpMessageBuilder.addUsage(
            name,
            ConfigMessage.HELP_GENERAL_NEXT::getMessage
        );
        return new CommandAPICommand(name)
            .withPermission(UserPerms.NEXT)
            .executes(info -> {
                EMFMessage message = Competition.getNextCompetitionMessage();
                message.prependMessage(PrefixType.DEFAULT.getPrefix());
                message.send(info.sender());
            });
    }

    private CommandAPICommand getToggle() {
        String name = MainConfig.getInstance().getToggleSubCommandName();
        helpMessageBuilder.addUsage(
            name,
            ConfigMessage.HELP_GENERAL_TOGGLE::getMessage
        );
        return new CommandAPICommand(name)
            .withPermission(UserPerms.TOGGLE)
            .executesPlayer(info -> {
                EvenMoreFish.getInstance().performFishToggle(info.sender());
            });
    }

    private CommandAPICommand getGui() {
        String name = MainConfig.getInstance().getGuiSubCommandName();
        helpMessageBuilder.addUsage(
            name,
            ConfigMessage.HELP_GENERAL_GUI::getMessage
        );
        return new CommandAPICommand(name)
            .withPermission(UserPerms.GUI)
            .executesPlayer(info -> {
                new MainMenuGui(info.sender()).open();
            });
    }

    private CommandAPICommand getHelp() {
        String name = MainConfig.getInstance().getHelpSubCommandName();
        helpMessageBuilder.addUsage(
            name,
            ConfigMessage.HELP_GENERAL_HELP::getMessage
        );
        return new CommandAPICommand(name)
            .withPermission(UserPerms.HELP)
            .executes(info -> {
                sendHelpMessage(info.sender());
            });
    }

    private CommandAPICommand getTop() {
        String name = MainConfig.getInstance().getTopSubCommandName();
        helpMessageBuilder.addUsage(
            name,
            ConfigMessage.HELP_GENERAL_TOP::getMessage
        );
        return new CommandAPICommand(name)
            .withPermission(UserPerms.TOP)
            .executesPlayer(info -> {
                Competition active = Competition.getCurrentlyActive();
                if (active == null) {
                    ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(info.sender());
                    return;
                }
                active.sendPlayerLeaderboard(info.sender());
            })
            .executes(info -> {
                Competition active = Competition.getCurrentlyActive();
                if (active == null) {
                    ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(info.sender());
                    return;
                }
                active.sendConsoleLeaderboard(info.sender());
            });
    }

    private CommandAPICommand getShop() {
        String name = MainConfig.getInstance().getShopSubCommandName();
        helpMessageBuilder.addUsage(
            name,
            ConfigMessage.HELP_GENERAL_SHOP::getMessage
        );
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
                if (!checkEconomy(player)) {
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
        helpMessageBuilder.addUsage(
            name,
            ConfigMessage.HELP_GENERAL_SELLALL::getMessage
        );
        return new CommandAPICommand(name)
            .withPermission(UserPerms.SELL_ALL)
            .executesPlayer(info -> {
                Player player = info.sender();
                if (checkEconomy(player)) {
                    new SellHelper(player.getInventory(), player).sellFish();
                }
            });
    }

    private CommandAPICommand getApplyBaits() {
        String name = MainConfig.getInstance().getApplyBaitsSubCommandName();
        helpMessageBuilder.addUsage(
            name,
            ConfigMessage.HELP_GENERAL_APPLYBAITS::getMessage
        );
        return new CommandAPICommand(name)
            .withPermission(UserPerms.APPLYBAITS)
            .executesPlayer(info -> {
                new ApplyBaitsGui(info.sender(), null).open();
            });
    }

    private CommandAPICommand getJournal() {
        helpMessageBuilder.addUsage(
            "journal",
            ConfigMessage.HELP_GENERAL_JOURNAL::getMessage
        );
        return new CommandAPICommand("journal")
            .withPermission(UserPerms.JOURNAL)
            .withArguments(
                RarityArgument.create().setOptional(true)
            )
            .executesPlayer(info -> {
                Rarity rarity = info.args().getUnchecked("rarity"); // This is allowed to be null.
                new FishJournalGui(info.sender(), rarity).open();
            });
    }

    private void sendHelpMessage(@NotNull CommandSender sender) {
        helpMessageBuilder.sendMessage(sender);
    }

    private boolean checkEconomy(@NotNull CommandSender sender) {
        if (!Economy.getInstance().isEnabled()) {
            ConfigMessage.ECONOMY_DISABLED.getMessage().send(sender);
            return false;
        }
        return true;
    }

}
