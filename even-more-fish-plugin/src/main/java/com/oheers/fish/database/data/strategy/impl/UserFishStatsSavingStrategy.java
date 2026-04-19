package com.oheers.fish.database.data.strategy.impl;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.data.strategy.BufferedSavingStrategy;
import com.oheers.fish.database.model.user.UserFishStats;

public class UserFishStatsSavingStrategy extends BufferedSavingStrategy<UserFishStats> {
    public UserFishStatsSavingStrategy() {
        super(
            stats -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().upsertUserFishStats(stats),
            stats -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().batchUpdateUserFishStats(stats),
            true
        );
    }
}
