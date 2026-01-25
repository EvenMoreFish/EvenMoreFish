package com.oheers.fish.plugin;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.SkullSaver;
import com.oheers.fish.baits.BaitApplicationListener;
import com.oheers.fish.competition.JoinChecker;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.events.FishEatEvent;
import com.oheers.fish.events.FishInteractEvent;
import com.oheers.fish.fishing.EMFFishListener;
import com.oheers.fish.fishing.FishingProcessor;
import com.oheers.fish.fishing.HuntingProcessor;
import com.oheers.fish.recipe.RecipeListener;
import com.oheers.fish.update.UpdateNotify;
import com.oheers.fish.utils.ItemProtectionListener;
import org.bukkit.plugin.PluginManager;

public class EventManager {

    private final EvenMoreFish plugin;
    private final PluginManager pm;

    public EventManager(EvenMoreFish plugin) {
        this.plugin = plugin;
        this.pm = plugin.getServer().getPluginManager();
    }

    public void registerCoreListeners() {
        // Database-related listeners
        if (DatabaseUtil.isDatabaseOnline()) {
            pm.registerEvents(plugin.getPluginDataManager().getUserManager(), plugin);
            pm.registerEvents(new EMFFishListener(), plugin);
        }

        // Always-registered listeners
        pm.registerEvents(new JoinChecker(), plugin);
        pm.registerEvents(new FishingProcessor(), plugin);
        pm.registerEvents(new HuntingProcessor(), plugin);
        pm.registerEvents(new SkullSaver(), plugin);
        pm.registerEvents(new UpdateNotify(), plugin);
        pm.registerEvents(new BaitApplicationListener(), plugin);
        pm.registerEvents(new ItemProtectionListener(), plugin);
        pm.registerEvents(new FishInteractEvent(), plugin);
        pm.registerEvents(new RecipeListener(), plugin);
    }

    public void registerOptionalListeners() {
        plugin.getDependencyManager().checkOptionalDependencies();
    }

}