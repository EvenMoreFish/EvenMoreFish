package com.oheers.fish.database.data.strategy.impl;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.data.strategy.BufferedSavingStrategy;
import com.oheers.fish.database.model.user.UserFishStats;

import java.util.concurrent.Executor;

public class UserFishStatsSavingStrategy extends BufferedSavingStrategy<UserFishStats> {
    public UserFishStatsSavingStrategy(Executor executor) {
        super(
            stats -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().upsertUserFishStats(stats),
            stats -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().batchUpdateUserFishStats(stats),
            true,
            executor
        );
    }
}
