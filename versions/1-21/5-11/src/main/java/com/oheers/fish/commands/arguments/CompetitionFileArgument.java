package com.oheers.fish.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.configs.CompetitionFile;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class CompetitionFileArgument implements CustomArgumentType.Converted<CompetitionFile, String> {

    private static final DynamicCommandExceptionType UNKNOWN_TYPE = new DynamicCommandExceptionType(
        obj -> MessageComponentSerializer.message().serialize(Component.text("Unknown CompetitionFile: " + obj))
    );

    @Override
    public CompetitionFile convert(String nativeType) throws CommandSyntaxException {
        CompetitionFile file = EvenMoreFish.getInstance().getCompetitionQueue().getItemMap().get(nativeType);
        if (file == null) {
            throw UNKNOWN_TYPE.create(nativeType);
        }
        return file;
    }

    @NotNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NotNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        EvenMoreFish.getInstance().getCompetitionQueue().getItemMap().keySet().stream()
            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
            .forEach(builder::suggest);
        return builder.buildFuture();
    }

}
