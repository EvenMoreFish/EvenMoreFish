package com.oheers.fish.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class FishArgument implements CustomArgumentType.Converted<String, String> {

    public static final DynamicCommandExceptionType INVALID_FISH = new DynamicCommandExceptionType(
        obj -> MessageComponentSerializer.message().serialize(Component.text("Invalid Fish: " + obj))
    );

    @NotNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NotNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        Rarity rarity;
        try {
            rarity = context.getLastChild().getArgument("rarity", Rarity.class);
        } catch (Exception exception) {
            return builder.buildFuture();
        }
        rarity.getOriginalFishList().stream()
            .map(fish -> fish.getName().replace(" ", "_"))
            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
            .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public String convert(String nativeType) {
        return nativeType;
    }

    public static @Nullable Fish resolveFish(@NotNull Rarity rarity, @NotNull String fishStr) {
        Fish fish = rarity.getFish(fishStr);
        if (fish == null) {
            fish = rarity.getFish(fishStr.replace("_", " "));
        }
        return fish;
    }

}
