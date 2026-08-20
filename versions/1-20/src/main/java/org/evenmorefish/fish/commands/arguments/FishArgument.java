package org.evenmorefish.fish.commands.arguments;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

/**
 * Should only be used at any point AFTER a RarityArgument. It will not work otherwise.
 */
public class FishArgument {

    public static Argument<IFish> create() {
        return new CustomArgument<>(new StringArgument("fish"), info -> {
            IRarity rarity = info.previousArgs().getUnchecked("rarity");
            if (rarity == null) {
                throw CustomArgument.CustomArgumentException.fromString("Could not find a previous RarityArgument!");
            }
            IFish fish = rarity.getFish(info.input());
            if (fish == null) {
                fish = rarity.getFish(info.input().replace("_", " "));
            }
            if (fish == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                        new CustomArgument.MessageBuilder("Unknown fish: ").appendArgInput()
                );
            }
            return fish;
        }).replaceSuggestions(ArgumentHelper.getAsyncSuggestions(
                info -> {
                    IRarity rarity = info.previousArgs().getUnchecked("rarity");
                    if (rarity == null) {
                        return new String[0];
                    }
                    return rarity.getOriginalFishList().stream().map(fish -> fish.getId().replace(" ", "_")).toArray(String[]::new);
                }
        ));
    }

}
