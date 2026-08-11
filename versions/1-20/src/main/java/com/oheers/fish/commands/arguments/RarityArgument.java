package com.oheers.fish.commands.arguments;

import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.fishing.items.FishManager;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class RarityArgument {

    public static Argument<IRarity> create() {
        return new CustomArgument<>(new StringArgument("rarity"), info -> {
            IRarity rarity = FishManager.getInstance().getRarity(info.input());
            if (rarity == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                        new CustomArgument.MessageBuilder("Unknown rarity: ").appendArgInput()
                );
            } else {
                return rarity;
            }
        }).replaceSuggestions(ArgumentHelper.getAsyncSuggestions(
                info -> FishManager.getInstance().getRarityMap().keySet().toArray(String[]::new)
        ));
    }

}
