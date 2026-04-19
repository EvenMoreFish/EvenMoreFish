package com.oheers.fish.events;

import com.gmail.nossr50.events.McMMOReplaceVanillaTreasureEvent;
import com.gmail.nossr50.events.skills.fishing.McMMOPlayerFishingTreasureEvent;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.Toggle;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class McMMOTreasureEvent implements Listener {

    private static final McMMOTreasureEvent mcmmoEvent = new McMMOTreasureEvent();

    private McMMOTreasureEvent() {}

    public static McMMOTreasureEvent getInstance() {
        return mcmmoEvent;
    }

    @EventHandler
    public void mcmmoTreasure(McMMOReplaceVanillaTreasureEvent event) {
        if (!MainConfig.getInstance().disableMcMMOTreasure()) {
            return;
        }
        Toggle toggle = EvenMoreFish.getInstance().getToggle();
        Player causingPlayer = event.getCausingPlayer();
        if (causingPlayer != null && toggle.isCustomFishingDisabled(causingPlayer)) {
            return;
        }
        if (MainConfig.getInstance().isFishCatchOnlyInCompetition() && !Competition.isActive()) {
            return;
        }
        event.setReplacementItemStack(event.getOriginalItem().getItemStack());
    }

    @EventHandler
    public void mcmmoTreasure(McMMOPlayerFishingTreasureEvent event) {
        if (!MainConfig.getInstance().disableMcMMOTreasure()) {
            return;
        }
        Toggle toggle = EvenMoreFish.getInstance().getToggle();
        if (toggle.isCustomFishingDisabled(event.getPlayer())) {
            return;
        }
        if (MainConfig.getInstance().isFishCatchOnlyInCompetition() && !Competition.isActive()) {
            return;
        }
        event.setTreasure(null);
    }
}
