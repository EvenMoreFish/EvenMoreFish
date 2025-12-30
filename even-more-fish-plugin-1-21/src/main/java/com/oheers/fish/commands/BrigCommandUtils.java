package com.oheers.fish.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class BrigCommandUtils {

    public static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize(Component.text("No players selected."))
    );

    private static final SimpleCommandExceptionType PLAYER_REQUIRED = new SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize(Component.text("Only players can use this command."))
    );

    public static @NotNull Player requirePlayer(@Nullable CommandSourceStack source) throws CommandSyntaxException {
        if (source == null) {
            throw PLAYER_REQUIRED.create();
        }
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            throw PLAYER_REQUIRED.create();
        }
        return player;
    }

    public static @NotNull Player requirePlayer(@Nullable CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context == null) {
            throw PLAYER_REQUIRED.create();
        }
        return requirePlayer(context.getSource());
    }

}
