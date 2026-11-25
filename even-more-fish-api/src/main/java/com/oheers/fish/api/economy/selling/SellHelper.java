package com.oheers.fish.api.economy.selling;

import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.events.EMFFishPreSaleEvent;
import com.oheers.fish.api.events.EMFFishSoldEvent;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class SellHelper {

    private static final SellHelper GLOBAL = new SellHelper();

    private final @NonNull Economy economy;

    public SellHelper() {
        this(Economy.getInstance());
    }

    /**
     * Returns a global instance of SellHelper using the global economy instance.
     */
    public static @NonNull SellHelper get() {
        return GLOBAL;
    }

    public SellHelper(@NonNull Economy economy) {
        this.economy = economy;
    }

    public void sell(@NonNull Inventory inventory, @NonNull Player player) {
        sell(inventory, player, f -> true);
    }

    public void sell(@NonNull Inventory inventory, @NonNull Player player, @NonNull Predicate<IFish> filter) {
        if (!economy.isEnabled()) {
            EMFPlugin.getInstance().sendMessage("ECONOMY_DISABLED", player);
            return;
        }

        List<SaleLine> lines = new ArrayList<>();
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack item = inventory.getItem(slot);
            SaleLine line = prepareLine(player, item, filter, slot);
            if (line != null) {
                lines.add(line);
            }
        }
        settle(player, lines, inventory);
    }

    public void sell(@Nullable ItemStack @NonNull[] itemStacks, @NonNull Player player) {
        sell(itemStacks, player, f -> true);
    }

    public void sell(@Nullable ItemStack @NonNull[] itemStacks, @NonNull Player player, @NonNull Predicate<IFish> filter) {
        if (!economy.isEnabled()) {
            EMFPlugin.getInstance().sendMessage("ECONOMY_DISABLED", player);
            return;
        }

        List<SaleLine> lines = new ArrayList<>();
        Set<ItemStack> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ItemStack item : itemStacks) {
            if (item == null || !seen.add(item)) {
                continue;
            }
            SaleLine line = prepareLine(player, item, filter, -1);
            if (line != null) {
                lines.add(line);
            }
        }
        settle(player, lines, null);
    }

    private @Nullable SaleLine prepareLine(Player player, @Nullable ItemStack item, Predicate<IFish> filter, int slot) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        SoldFish sold = SoldFish.get(player, item);
        if (sold == null || !filter.test(sold.getFish())) {
            return null;
        }
        ItemStack snapshot = item.clone();
        if (!new EMFFishPreSaleEvent(player, sold).callEvent() || sold.getValue() < 0) {
            return null;
        }
        return new SaleLine(item, snapshot, sold, slot);
    }

    private boolean matches(@Nullable ItemStack item, ItemStack snapshot) {
        return item != null && item.getAmount() == snapshot.getAmount() && item.isSimilar(snapshot);
    }

    private void settle(Player player, List<SaleLine> lines, @Nullable Inventory inventory) {
        if (lines.isEmpty()) {
            EMFPlugin.getInstance().sendMessage("NO_SELLABLE_FISH", player);
            return;
        }
        // Sale events can alter the inventory; only pay for fish still in their original slots.
        boolean unchanged = lines.stream().allMatch(line -> matches(
            inventory == null ? line.item() : inventory.getItem(line.slot()), line.snapshot()));
        if (!unchanged) {
            EMFPlugin.getInstance().sendMessage("FISH_SALE_FAILED", player);
            return;
        }
        double totalValue = lines.stream().mapToDouble(line -> line.sold().getFinalValue()).sum();
        int count = lines.stream().mapToInt(line -> line.sold().getQuantity()).sum();
        if (economy.depositForSale(player, totalValue, true) != Economy.DepositResult.SUCCESS) {
            EMFPlugin.getInstance().sendMessage("FISH_SALE_FAILED", player);
            return;
        }
        for (SaleLine line : lines) {
            if (inventory == null) {
                line.item().setAmount(0);
            } else {
                inventory.setItem(line.slot(), null);
            }
        }
        lines.forEach(line -> {
            SoldFish sold = line.sold();
            logSoldFish(sold);
            sold.getFish().getSellRewards().forEach(reward -> reward.give(player, player.getLocation()));
            new EMFFishSoldEvent(player, sold).callEvent();
        });
        EMFPlugin.getInstance().sendSoldMessage(totalValue, count, player);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.06f);
    }

    private record SaleLine(ItemStack item, ItemStack snapshot, SoldFish sold, int slot) {}

    private void logSoldFish(@NonNull SoldFish soldFish) {
        EMFPlugin.getInstance().logSoldFish(soldFish);
    }

}
