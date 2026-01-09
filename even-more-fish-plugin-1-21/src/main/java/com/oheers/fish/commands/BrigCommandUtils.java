package com.oheers.fish.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.oheers.fish.messages.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class BrigCommandUtils {

    public static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize(Component.text("No players selected."))
    );

    public static final SimpleCommandExceptionType CANT_BE_CONSOLE = new SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize(
            ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().getComponentMessage()
        )
    );

    public static @NonNull Player requirePlayer(@Nullable CommandSourceStack source) throws CommandSyntaxException {
        if (source == null) {
            throw CANT_BE_CONSOLE.create();
        }
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            throw CANT_BE_CONSOLE.create();
        }
        return player;
    }

    public static @NonNull Player requirePlayer(@Nullable CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context == null) {
            throw CANT_BE_CONSOLE.create();
        }
        return requirePlayer(context.getSource());
    }

    public static <T> @NonNull T getArgumentOrDefault(@NonNull CommandContext<CommandSourceStack> ctx, @NonNull String name, @NonNull Class<T> clazz, @NonNull T def) {
        try {
            return ctx.getArgument(name, clazz);
        } catch (Exception exception) {
            return def;
        }
    }

    public static <T> @Nullable T getArgumentOrNull(@NonNull CommandContext<CommandSourceStack> ctx, @NonNull String name, @NonNull Class<T> clazz) {
        try {
            return ctx.getArgument(name, clazz);
        } catch (Exception exception) {
            return null;
        }
    }

    public static @NotNull List<Player> resolvePlayers(@NonNull CommandContext<CommandSourceStack> ctx, @Nullable PlayerSelectorArgumentResolver resolver) throws CommandSyntaxException {
        if (resolver == null) {
            return List.of();
        }
        return resolver.resolve(ctx.getSource());
    }

}
