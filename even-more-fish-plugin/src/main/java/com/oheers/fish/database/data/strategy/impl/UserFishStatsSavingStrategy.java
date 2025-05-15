package com.oheers.fish.database.data.strategy.impl;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.data.strategy.HybridSavingStrategy;
import com.oheers.fish.database.model.user.UserFishStats;

import java.util.concurrent.TimeUnit;


public class UserFishStatsSavingStrategy extends HybridSavingStrategy<UserFishStats> {
    public UserFishStatsSavingStrategy(long interval) {
        super(
                stats -> EvenMoreFish.getInstance().getDatabase().upsertUserFishStats(stats),
                stats ->  EvenMoreFish.getInstance().getDatabase().batchUpdateUserFishStats(stats),
                interval,
                TimeUnit.valueOf(MainConfig.getInstance().getSaveIntervalUnit())
        );
    }

}
