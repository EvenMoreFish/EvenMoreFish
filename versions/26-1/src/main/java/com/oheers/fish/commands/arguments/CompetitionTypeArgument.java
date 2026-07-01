package com.oheers.fish.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.oheers.fish.competition.CompetitionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class CompetitionTypeArgument implements CustomArgumentType.Converted<CompetitionType, String> {
    private static final DynamicCommandExceptionType UNKNOWN_TYPE = new DynamicCommandExceptionType(
            obj -> MessageComponentSerializer.message().serialize(Component.text(obj + " is not a valid competition type!"))
    );

    @Override
    public CompetitionType convert(String nativeType) throws CommandSyntaxException {
        CompetitionType type = CompetitionType.getType(nativeType);
        if (type == null) {
            throw UNKNOWN_TYPE.create(nativeType);
        }
        return type;
    }

    @NotNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NotNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        Arrays.stream(CompetitionType.values())
                .map(CompetitionType::toString)
                .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
