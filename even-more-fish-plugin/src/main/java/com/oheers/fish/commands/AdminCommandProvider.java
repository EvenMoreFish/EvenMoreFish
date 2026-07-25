package com.oheers.fish.commands;

import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.permissions.AdminPerms;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

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

    public abstract @NonNull C get();

    public abstract @NonNull A getAsArgument();

    protected abstract @NonNull A database();

    protected abstract @NonNull A fish();

    protected abstract @NonNull A randomFish();

    protected abstract @NonNull A list();

    protected abstract @NonNull A competition();

    protected abstract @NonNull A customRod();

    protected abstract @NonNull A debug();

    protected abstract @NonNull A bait();

    protected abstract @NonNull A clearBaits();

    protected abstract @NonNull A reload();

    protected abstract @NonNull A version();

    protected abstract @NonNull A rawItem();

    protected abstract @NonNull A migrate();

    protected abstract @NonNull A help();

    public static void sendHelpMessage(CommandSender sender) {
        HELP_MESSAGE.send(sender);
    }

}
