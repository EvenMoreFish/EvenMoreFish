package com.oheers.fish.commands;

import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class HelpMessage {

    private final String subcommand;
    private final List<HelpMessageEntry> entries = new ArrayList<>();

    private Predicate<CommandSender> defaultRequirement = sender -> true;

    private HelpMessage(@NonNull String subcommand) {
        this.subcommand = subcommand;
    }

    /**
     * Creates a HelpMessageBuilder instance
     */
    public static HelpMessage helpMessage() {
        return new HelpMessage("");
    }

    public static HelpMessage helpMessage(@NonNull String subcommand) {
        return new HelpMessage(subcommand);
    }

    /**
     * Adds a usage to this builder
     */
    public HelpMessage addEntry(@NonNull String name, @NonNull Supplier<EMFMessage> description, @NonNull Predicate<CommandSender> requirement) {
        this.entries.add(new HelpMessageEntry(name, description, requirement));
        return this;
    }

    /**
     * Adds a usage to this builder
     */
    public HelpMessage addEntry(@NonNull String name, @NonNull Supplier<EMFMessage> description, @NonNull String permissionRequirement) {
        Predicate<CommandSender> requirement = sender -> sender.hasPermission(permissionRequirement);
        this.entries.add(new HelpMessageEntry(name, description, requirement));
        return this;
    }

    /**
     * Adds a usage to this builder
     */
    public HelpMessage addEntry(@NonNull String name, @NonNull Supplier<EMFMessage> description) {
        this.entries.add(new HelpMessageEntry(name, description, defaultRequirement));
        return this;
    }

    public HelpMessage setDefaultRequirement(@NonNull Predicate<CommandSender> requirement) {
        this.defaultRequirement = requirement;
        return this;
    }

    public HelpMessage setDefaultRequirement(@NonNull String permissionRequirement) {
        this.defaultRequirement = sender -> sender.hasPermission(permissionRequirement);
        return this;
    }

    /**
     * Adds "/[commandname] " and the relevant subcommand name to the start of the provided entry.
     */
    private String correctCommand(@NonNull String name) {
        StringBuilder builder = new StringBuilder("/");
        builder.append(MainConfig.getInstance().getMainCommandName()).append(" ");
        if (!subcommand.isEmpty()) {
            builder.append(subcommand).append(" ");
        }
        builder.append(name);
        return builder.toString();
    }

    public void send(@NonNull CommandSender sender) {
        final EMFListMessage message = ConfigMessage.HELP_GENERAL_TITLE.getMessage().toListMessage();
        entries.forEach(entry -> {
            if (!entry.requirement().test(sender)) {
                return;
            }
            EMFMessage description = entry.description().get();
            if (description == null || description.isEmpty()) {
                return;
            }
            EMFMessage usage = ConfigMessage.HELP_FORMAT.getMessage();
            usage.setVariable("{command}", correctCommand(entry.name()));
            usage.setVariable("{description}", description);
            message.appendMessage(usage);
        });
        message.send(sender);
    }

    record HelpMessageEntry(
        @NonNull String name,
        @NonNull Supplier<EMFMessage> description,
        @NonNull Predicate<CommandSender> requirement
    ) {}

}
