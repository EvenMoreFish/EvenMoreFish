package com.oheers.fish.commands;

import com.oheers.fish.messages.ConfigMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @param <C> The command class.
 * @param <A> The argument class.
 */
public abstract class AdminCommandProvider<C, A> {

    private static final HelpMessageBuilder HELP_MESSAGE = HelpMessageBuilder.create()
        .addUsage("admin fish", ConfigMessage.HELP_ADMIN_FISH::getMessage)
        .addUsage("admin custom-rod", ConfigMessage.HELP_ADMIN_CUSTOMROD::getMessage)
        .addUsage("admin bait", ConfigMessage.HELP_ADMIN_BAIT::getMessage)
        .addUsage("admin clearbaits", ConfigMessage.HELP_ADMIN_CLEARBAITS::getMessage)
        .addUsage("admin reload", ConfigMessage.HELP_ADMIN_RELOAD::getMessage)
        .addUsage("admin version", ConfigMessage.HELP_ADMIN_VERSION::getMessage)
        .addUsage("admin migrate", ConfigMessage.HELP_ADMIN_MIGRATE::getMessage)
        .addUsage("admin rawItem", ConfigMessage.HELP_ADMIN_RAWITEM::getMessage)
        .addUsage("admin help", ConfigMessage.HELP_GENERAL_HELP::getMessage)
        .addUsage("admin competition", ConfigMessage.HELP_ADMIN_COMPETITION::getMessage);

    public abstract @NotNull C get();

    public abstract @NotNull A getAsArgument();

    protected abstract @NotNull A database();

    protected abstract @NotNull A fish();

    protected abstract @NotNull A list();

    protected abstract @NotNull A competition();

    protected abstract @NotNull A customRod();

    protected abstract @NotNull A bait();

    protected abstract @NotNull A clearBaits();

    protected abstract @NotNull A reload();

    protected abstract @NotNull A version();

    protected abstract @NotNull A rawItem();

    protected abstract @NotNull A migrate();

    protected abstract @NotNull A help();

    public static void sendHelpMessage(CommandSender sender) {
        HELP_MESSAGE.sendMessage(sender);
    }

}
