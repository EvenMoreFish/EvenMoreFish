package com.oheers.fish.commands.arguments;

import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.fishing.rods.RodManager;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class CustomRodArgument {

    public static Argument<CustomRod> create() {
        return new CustomArgument<>(new StringArgument("customRod"), info -> {
            CustomRod rod = RodManager.getInstance().getItem(info.input());
            if (rod == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                    new CustomArgument.MessageBuilder("Unknown custom rod: ").appendArgInput()
                );
            } else {
                return rod;
            }
        }).replaceSuggestions(ArgumentHelper.getAsyncSuggestions(
            info -> RodManager.getInstance().getItemMap().keySet().toArray(String[]::new)
        ));
    }

}
