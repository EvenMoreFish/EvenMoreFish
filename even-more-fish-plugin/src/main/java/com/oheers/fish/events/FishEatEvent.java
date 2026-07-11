package com.oheers.fish.events;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class FishEatEvent {

    private static final FishEatEvent eatEvent = new FishEatEvent();

    private FishEatEvent() {}

    public static FishEatEvent getInstance() {
        return eatEvent;
    }

    public boolean checkEatEvent(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        // Checks if the eaten item is a fish
        if (FishManager.getInstance().isFish(item)) {
            // Creates a replica of the fish we can use
            IFish fish = FishManager.getInstance().getFish(item);
            if (fish == null) {
                return false;
            }
            if (fish.hasEatRewards()) {
                // Runs through each eat-event
                fish.getActionRewards().forEach(r -> r.rewardPlayer(event.getPlayer(), null));
                return true;
            }
        }
        return false;
    }
}
