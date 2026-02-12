package com.oheers.fish.events;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import dev.aurelium.auraskills.api.event.loot.LootDropEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Set;

public class AuraSkillsFishingEvent implements Listener {

    private final Set<LootDropEvent.Cause> causes = Set.of(
        LootDropEvent.Cause.FISHING_LUCK,
        LootDropEvent.Cause.TREASURE_HUNTER,
        LootDropEvent.Cause.FISHING_OTHER_LOOT,
        LootDropEvent.Cause.EPIC_CATCH
    );

    @EventHandler
    public void fishCatch(LootDropEvent event) {
        if (!causes.contains(event.getCause())) {
            return;
        }
        if (!MainConfig.getInstance().disableAuraSkills()) {
            return;
        }
        if (!MainConfig.getInstance().isFishCatchOnlyInCompetition()) {
            event.setCancelled(true);
            return;
        }
        if (Competition.isActive()) {
            event.setCancelled(true);
        }
    }

}
