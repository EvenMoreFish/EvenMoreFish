package com.oheers.fish.database.data.strategy.impl;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.data.strategy.BufferedSavingStrategy;
import com.oheers.fish.database.model.CompetitionReport;

import java.util.concurrent.Executor;

public class CompetitionSavingStrategy extends BufferedSavingStrategy<CompetitionReport> {
    public CompetitionSavingStrategy(Executor executor) {
        super(
            competition -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().updateCompetition(competition),
            competitions -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().batchUpdateCompetitions(competitions),
            true,
            executor
        );
    }
}
