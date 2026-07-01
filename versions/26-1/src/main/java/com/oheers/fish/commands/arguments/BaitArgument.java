package com.oheers.fish.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.manager.BaitManager;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BaitArgument implements CustomArgumentType.Converted<BaitHandler, String>{
    private static final DynamicCommandExceptionType NOT_BAIT = new DynamicCommandExceptionType(
            obj -> MessageComponentSerializer.message().serialize(Component.text(obj + " is not a valid bait!"))
    );

    @Override
    public BaitHandler convert(String nativeType) throws CommandSyntaxException {
        BaitHandler bait = BaitManager.getInstance().getBait(nativeType);
        if (bait == null) {
            bait = BaitManager.getInstance().getBait(nativeType.replace("_", " "));
        }
        if (bait == null) {
            throw NOT_BAIT.create(nativeType);
        }
        return bait;
    }

    @NotNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NotNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        BaitManager.getInstance().getItemMap().keySet().stream()
                .map(s -> s.replace(" ", "_"))
                .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
