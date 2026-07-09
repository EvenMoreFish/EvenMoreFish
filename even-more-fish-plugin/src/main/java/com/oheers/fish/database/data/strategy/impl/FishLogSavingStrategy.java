package com.oheers.fish.database.data.strategy.impl;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.data.strategy.ImmediateSavingStrategy;
import com.oheers.fish.database.model.fish.FishLog;

import java.util.Collection;
import java.util.concurrent.Executor;

public class FishLogSavingStrategy extends ImmediateSavingStrategy<Collection<FishLog>> {
    public FishLogSavingStrategy(Executor executor) {
        super(logs -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().batchInsertFishLogs(logs), executor);
    }
}
