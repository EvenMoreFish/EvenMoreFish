package com.oheers.fish.competition.configs;

import com.oheers.fish.FishUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CompetitionFileUpdates {

    public static void update(@NotNull CompetitionFile competitionFile) {
        YamlDocument config = competitionFile.getConfig();

        // leaderboard.position-colours -> leaderboard
        if (config.contains("leaderboard.position-colours")) {
            List<String> colours = config.getStringList("leaderboard.position-colours");

            List<String> updatedColours = colours.stream().map(FishUtils::getFormat).toList();
            config.set("leaderboard", updatedColours);
        }

        competitionFile.save();
    }

}
