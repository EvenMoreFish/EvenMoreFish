package com.oheers.fish.commands.arguments;

import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ArgumentHelper {

    public static Argument<String> getAsyncStringsArgument(@NonNull String name, @NonNull Function<SuggestionInfo<CommandSender>, String[]> function) {
        return new StringArgument(name).includeSuggestions(getAsyncSuggestions(function));
    }

    public static ArgumentSuggestions<CommandSender> getAsyncSuggestions(@NonNull Function<SuggestionInfo<CommandSender>, String[]> function) {
        return ArgumentSuggestions.stringsAsync(
                info -> CompletableFuture.supplyAsync(() -> function.apply(info))
        );
    }

    /**
     * Creates a new EntitySelectorArgument.OnePlayer with only the player names suggested.
     * We need to use this argument because the other two make Mojang API calls.
     */
    public static Argument<Player> getPlayerArgument(@NonNull String name) {
        return new EntitySelectorArgument.OnePlayer(name)
                .replaceSuggestions(getAsyncSuggestions(info ->
                        Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)
                ));
    }

}
