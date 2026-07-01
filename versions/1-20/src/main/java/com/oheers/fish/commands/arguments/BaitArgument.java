package com.oheers.fish.commands.arguments;

import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.manager.BaitManager;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class BaitArgument {

    public static Argument<BaitHandler> create() {
        return new CustomArgument<>(new StringArgument("bait"), info -> {
            BaitHandler bait = BaitManager.getInstance().getBait(info.input());
            if (bait == null) {
                bait = BaitManager.getInstance().getBait(info.input().replace("_", " "));
            }
            if (bait == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                        new CustomArgument.MessageBuilder("Unknown bait: ").appendArgInput()
                );
            }
            return bait;
        }).replaceSuggestions(ArgumentHelper.getAsyncSuggestions(
                info -> BaitManager.getInstance().getItemMap().keySet().stream().map(s -> s.replace(" ", "_")).toArray(String[]::new)
        ));
    }

}
