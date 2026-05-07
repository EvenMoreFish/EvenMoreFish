package com.oheers.fish.commands;

import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.permissions.UserPerms;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @param <C> The command class.
 * @param <A> The argument class.
 */
public abstract class MainCommandProvider<C, A> {

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

    public abstract @NotNull C get();

    protected @NotNull String commandName() {
        return MainConfig.getInstance().getMainCommandName();
    }

    protected abstract @NotNull A admin();

    protected @NotNull String adminName() {
        return MainConfig.getInstance().getAdminSubCommandName();
    }

    protected abstract @NotNull A shop();

    protected @NotNull String shopName() {
        return MainConfig.getInstance().getShopSubCommandName();
    }

    protected abstract @NotNull A journal();

    protected @NotNull String journalName() {
        return MainConfig.getInstance().getJournalSubCommandName();
    }

    protected abstract @NotNull A toggle();

    protected @NotNull String toggleName() {
        return MainConfig.getInstance().getToggleSubCommandName();
    }

    protected abstract @NotNull A next();

    protected @NotNull String nextName() {
        return MainConfig.getInstance().getNextSubCommandName();
    }

    protected abstract @NotNull A help();

    protected @NotNull String helpName() {
        return MainConfig.getInstance().getHelpSubCommandName();
    }

    protected abstract @NotNull A gui();

    protected @NotNull String guiName() {
        return MainConfig.getInstance().getGuiSubCommandName();
    }

    protected abstract @NotNull A top();

    protected @NotNull String topName() {
        return MainConfig.getInstance().getTopSubCommandName();
    }

    protected abstract @NotNull A sellAll();

    protected @NotNull String sellAllName() {
        return MainConfig.getInstance().getSellAllSubCommandName();
    }

    protected abstract @NotNull A applyBaits();

    protected @NotNull String applyBaitsName() {
        return MainConfig.getInstance().getApplyBaitsSubCommandName();
    }

    public static void sendHelpMessage(CommandSender sender) {
        HELP_MESSAGE.send(sender);
    }

}
