package com.oheers.fish.events;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.Toggle;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.rods.CustomRod;
import dev.aurelium.auraskills.api.event.loot.LootDropEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
        Toggle toggle = EvenMoreFish.getInstance().getToggle();
        if (!causes.contains(event.getCause()) || !MainConfig.getInstance().disableAuraSkills() || toggle.isCustomFishingDisabled(event.getPlayer())) {
            return;
        }

        PlayerInventory inventory = event.getPlayer().getInventory();
        ItemStack main = inventory.getItemInMainHand();
        ItemStack off = inventory.getItemInOffHand();
        if (!Checks.canUseRod(main) && !Checks.canUseRod(off)) {
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
