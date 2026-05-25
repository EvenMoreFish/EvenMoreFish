package com.oheers.fish.commands;

import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.permissions.AdminPerms;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @param <C> The command class.
 * @param <A> The argument class.
 */
public abstract class AdminCommandProvider<C, A> {

    public static final HelpMessage HELP_MESSAGE = HelpMessage.helpMessage(MainConfig.getInstance().getAdminSubCommandName())
        .setDefaultRequirement(AdminPerms.ADMIN)
        .addEntry("fish", ConfigMessage.HELP_ADMIN_FISH::getMessage)
        .addEntry("custom-rod", ConfigMessage.HELP_ADMIN_CUSTOMROD::getMessage)
        .addEntry("bait", ConfigMessage.HELP_ADMIN_BAIT::getMessage)
        .addEntry("clearbaits", ConfigMessage.HELP_ADMIN_CLEARBAITS::getMessage)
        .addEntry("reload", ConfigMessage.HELP_ADMIN_RELOAD::getMessage)
        .addEntry("version", ConfigMessage.HELP_ADMIN_VERSION::getMessage)
        .addEntry("migrate", ConfigMessage.HELP_ADMIN_MIGRATE::getMessage, AdminPerms.MIGRATE)
        .addEntry("rawItem", ConfigMessage.HELP_ADMIN_RAWITEM::getMessage)
        .addEntry("debug", () -> EMFSingleMessage.fromString("Shows debug information for some features."))
        .addEntry("help", ConfigMessage.HELP_GENERAL_HELP::getMessage)
        .addEntry("competition", ConfigMessage.HELP_ADMIN_COMPETITION::getMessage)
        .addEntry("database", ConfigMessage.HELP_ADMIN_DATABASE::getMessage, AdminPerms.DATABASE);

    public abstract @NotNull C get();

    public abstract @NotNull A getAsArgument();

    protected abstract @NotNull A database();

    protected abstract @NotNull A fish();

    protected abstract @NotNull A list();

    protected abstract @NotNull A competition();

    protected abstract @NotNull A customRod();

    protected abstract @NotNull A debug();

    protected abstract @NotNull A bait();

    protected abstract @NotNull A clearBaits();

    protected abstract @NotNull A reload();

    protected abstract @NotNull A version();

    protected abstract @NotNull A rawItem();

    protected abstract @NotNull A migrate();

    protected abstract @NotNull A help();

    public static void sendHelpMessage(CommandSender sender) {
        HELP_MESSAGE.send(sender);
    }

}
