package com.oheers.fish.database.data.strategy.impl;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.data.strategy.HybridSavingStrategy;
import com.oheers.fish.database.model.CompetitionReport;

import java.util.concurrent.TimeUnit;

public class CompetitionSavingStrategy extends HybridSavingStrategy<CompetitionReport> {
    public CompetitionSavingStrategy(long interval) {
        super(
                competition -> EvenMoreFish.getInstance().getDatabase().updateCompetition(competition),
                competitions -> EvenMoreFish.getInstance().getDatabase().batchUpdateCompetitions(competitions),
                interval,
                TimeUnit.valueOf(MainConfig.getInstance().getSaveIntervalUnit())
        );
    }
}
