package com.oheers.fish.commands;

import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @param <C> The command class.
 * @param <A> The argument class.
 */
public abstract class MainCommandProvider<C, A> {

    private static final HelpMessageBuilder HELP_MESSAGE = HelpMessageBuilder.create()
        .addUsage(MainConfig.getInstance().getAdminSubCommandName(), ConfigMessage.HELP_GENERAL_ADMIN::getMessage)
        .addUsage(MainConfig.getInstance().getHelpSubCommandName(), ConfigMessage.HELP_GENERAL_HELP::getMessage)
        .addUsage(MainConfig.getInstance().getGuiSubCommandName(), ConfigMessage.HELP_GENERAL_GUI::getMessage)
        .addUsage(MainConfig.getInstance().getTopSubCommandName(), ConfigMessage.HELP_GENERAL_TOP::getMessage)
        .addUsage(MainConfig.getInstance().getSellAllSubCommandName(), ConfigMessage.HELP_GENERAL_SELLALL::getMessage)
        .addUsage(MainConfig.getInstance().getApplyBaitsSubCommandName(), ConfigMessage.HELP_GENERAL_APPLYBAITS::getMessage)
        .addUsage(MainConfig.getInstance().getJournalSubCommandName(), ConfigMessage.HELP_GENERAL_JOURNAL::getMessage)
        .addUsage(MainConfig.getInstance().getNextSubCommandName(), ConfigMessage.HELP_GENERAL_NEXT::getMessage)
        .addUsage(MainConfig.getInstance().getToggleSubCommandName(), ConfigMessage.HELP_GENERAL_TOGGLE::getMessage)
        .addUsage(MainConfig.getInstance().getShopSubCommandName(), ConfigMessage.HELP_GENERAL_SHOP::getMessage);

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
        HELP_MESSAGE.sendMessage(sender);
    }

}
