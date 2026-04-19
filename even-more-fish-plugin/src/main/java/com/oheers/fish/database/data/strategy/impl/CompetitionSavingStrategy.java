package com.oheers.fish.database.data.strategy.impl;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.data.strategy.BufferedSavingStrategy;
import com.oheers.fish.database.model.CompetitionReport;

public class CompetitionSavingStrategy extends BufferedSavingStrategy<CompetitionReport> {
    public CompetitionSavingStrategy() {
        super(
            competition -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().updateCompetition(competition),
            competitions -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().batchUpdateCompetitions(competitions),
            true
        );
    }
}
