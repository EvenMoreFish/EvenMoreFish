package com.oheers.fish.database.data.strategy.impl;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.data.strategy.ImmediateSavingStrategy;
import com.oheers.fish.database.model.user.UserReport;

import java.util.concurrent.Executor;


public class UserReportsSavingStrategy extends ImmediateSavingStrategy<UserReport> {
    public UserReportsSavingStrategy(Executor executor) {
        super(report -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().upsertUserReport(report), executor);
    }
}
