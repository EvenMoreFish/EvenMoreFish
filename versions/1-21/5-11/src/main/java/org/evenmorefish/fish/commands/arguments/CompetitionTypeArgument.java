package org.evenmorefish.fish.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.oheers.fish.api.registry.RegistryItem;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.CompetitionTypeRegistry;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class CompetitionTypeArgument implements CustomArgumentType.Converted<CompetitionType, String> {
    private static final DynamicCommandExceptionType UNKNOWN_TYPE = new DynamicCommandExceptionType(
        obj -> MessageComponentSerializer.message().serialize(Component.text(obj + " is not a valid competition type!"))
    );

    @Override
    public CompetitionType convert(String nativeType) throws CommandSyntaxException {
        CompetitionType type = CompetitionTypeRegistry.getInstance().get(nativeType);
        if (type == null) {
            throw UNKNOWN_TYPE.create(nativeType);
        }
        return type;
    }

    @NonNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NonNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, @NonNull SuggestionsBuilder builder) {
        CompetitionTypeRegistry.getInstance().getRegistry().values().stream()
            .map(RegistryItem::getKey)
            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
            .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
