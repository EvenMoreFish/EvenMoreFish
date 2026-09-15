package org.evenmorefish.fish.commands.arguments;

import com.oheers.fish.api.registry.RegistryItem;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.CompetitionTypeRegistry;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

import java.util.Arrays;

public class CompetitionTypeArgument {

    public static Argument<CompetitionType> create() {
        return new CustomArgument<>(new StringArgument("competitionType"), info -> {
            CompetitionType type = CompetitionTypeRegistry.getInstance().get(info.input());
            if (type == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                        new CustomArgument.MessageBuilder("Unknown competition type: ").appendArgInput()
                );
            }
            return type;
        }).replaceSuggestions(ArgumentSuggestions.strings(
            CompetitionTypeRegistry.getInstance().getRegistry().values().stream()
                .map(RegistryItem::getKey)
                .toArray(String[]::new)
        ));
    }

}
