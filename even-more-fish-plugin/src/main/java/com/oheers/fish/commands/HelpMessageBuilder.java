package com.oheers.fish.commands;

import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Supplier;

public class HelpMessageBuilder {

    private final HashMap<String, Supplier<EMFMessage>> usages;

    private HelpMessageBuilder(@NotNull HashMap<String, Supplier<EMFMessage>> usages) {
        this.usages = usages;
    }

    /**
     * Creates a HelpMessageBuilder instance
     */
    public static HelpMessageBuilder create() {
        return new HelpMessageBuilder(new HashMap<>());
    }

    /**
     * Creates a HelpMessageBuilder instance with the provided usages
     */
    public static HelpMessageBuilder create(@NotNull HashMap<String, Supplier<EMFMessage>> usages) {
        return new HelpMessageBuilder(usages);
    }

    /**
     * Adds a usage to this builder
     */
    public HelpMessageBuilder addUsage(@NotNull String name, @NotNull Supplier<EMFMessage> helpMessage) {
        this.usages.putIfAbsent(name, helpMessage);
        return this;
    }

    /**
     * Creates the final message.
     * @return The created help message
     */
    public EMFMessage buildMessage() {
        final EMFListMessage message = ConfigMessage.HELP_GENERAL_TITLE.getMessage().toListMessage();
        usages.forEach((key, value) -> {
            EMFMessage usage = ConfigMessage.HELP_FORMAT.getMessage();
            usage.setVariable("{command}", correctCommand(key));
            usage.setVariable("{description}", value.get());
            message.appendMessage(usage);
        });
        return message;
    }

    /**
     * Adds "/[commandname] " to the start of the provided usage.
     */
    private String correctCommand(@NotNull String key) {
        return "/" + MainConfig.getInstance().getMainCommandName() + " " + key;
    }

    /**
     * Sends the final message to the provided sender.
     * @param sender The sender to send the message to.
     */
    public void sendMessage(@NotNull CommandSender sender) {
        buildMessage().send(sender);
    }


}
