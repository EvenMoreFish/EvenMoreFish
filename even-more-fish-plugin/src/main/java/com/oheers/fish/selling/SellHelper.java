package com.oheers.fish.selling;

import com.devskiller.friendly_id.FriendlyId;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.EMFFishSellEvent;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.manager.DataManager;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SellHelper {

    private final @NotNull Player player;
    private final @NotNull List<SoldFish> fish;

    public int getCount() {
        return count;
    }

    public double getSaleValue() {
        return saleValue;
    }

    private final double saleValue;
    private final int count;

    public SellHelper(@NotNull Inventory inventory, @NotNull Player player, boolean removeFromInventory) {
        this.player = player;
        this.fish = fetchFish(inventory, removeFromInventory);
        this.saleValue = fish.stream().mapToDouble(SoldFish::getTotalValue).sum();
        this.count = fish.stream().mapToInt(SoldFish::getAmount).sum();
    }

    public SellHelper(@Nullable ItemStack @NotNull[] itemStacks, @NotNull Player player, boolean removeStacks) {
        this.player = player;
        this.fish = fetchFish(itemStacks, removeStacks);
        this.saleValue = fish.stream().mapToDouble(SoldFish::getTotalValue).sum();
        this.count = fish.stream().mapToInt(SoldFish::getAmount).sum();
    }

    public SellHelper(@NotNull Inventory inventory, @NotNull Player player) {
        this(inventory, player, true);
    }

    public SellHelper(@Nullable ItemStack @NotNull[] itemStacks, @NotNull Player player) {
        this(itemStacks, player, true);
    }

    /**
     * @deprecated use {@link #sell()} instead
     */
    @Deprecated(since = "2.1.4", forRemoval = true)
    public void sellFish() {
        sell();
    }

    public void sell() {
        if (!Economy.getInstance().isEnabled()) {
            ConfigMessage.ECONOMY_DISABLED.getMessage().send(player);
            return;
        }

        if (fish.isEmpty()) {
            ConfigMessage.NO_SELLABLE_FISH.getMessage().send(player);
            return;
        }

        // Fire the sell event and check for cancellation.
        EMFFishSellEvent event = new EMFFishSellEvent(fish, player, saleValue, LocalDateTime.now());
        if (!event.callEvent()) {
            // This could be the wrong message to send?
            ConfigMessage.NO_SELLABLE_FISH.getMessage().send(player);
            return;
        }

        // Give sell rewards
        fish.forEach(fish ->
            fish.getFish().getSellRewards().forEach(
                reward -> reward.rewardPlayer(player, player.getLocation())
            )
        );

        logSoldFish(fish, saleValue, count);

        // Give money
        Economy.getInstance().deposit(player, saleValue, true);

        // Send message
        EMFMessage message = ConfigMessage.FISH_SALE.getMessage();
        message.setSellPrice(Economy.getInstance().getWorthFormat(saleValue, true));
        message.setAmount(count);
        message.setPlayer(player);
        message.send(player);

        // Play sound
        player.playSound(this.player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.06f);
    }

    private static List<SoldFish> fetchFish(@NotNull Inventory inventory, boolean removeFromInventory) {
        List<SoldFish> list = new ArrayList<>();
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || hasEquipped(inventory, item)) {
                continue;
            }
            try {
                SoldFish fish = new SoldFish(item);
                list.add(fish);
                if (removeFromInventory) {
                    inventory.setItem(slot, null);
                }
            } catch (IllegalArgumentException exception) {
                EvenMoreFish.getInstance().debug(exception.getMessage(), exception);
            }
        }
        return list;
    }

    private static List<SoldFish> fetchFish(@Nullable ItemStack @NotNull[] itemStacks, boolean removeStack) {
        List<SoldFish> list = new ArrayList<>();

        for (ItemStack item : itemStacks) {
            if (item == null) {
                continue;
            }
            try {
                SoldFish fish = new SoldFish(item);
                list.add(fish);
                if (removeStack) {
                    item.setAmount(0);
                }
            } catch (IllegalArgumentException exception) {
                EvenMoreFish.getInstance().debug(exception.getMessage(), exception);
            }
        }
        return list;
    }

    private static boolean hasEquipped(@NotNull Inventory inventory, @NotNull ItemStack item) {
        if (!(inventory instanceof PlayerInventory playerInventory)) {
            return false;
        }
        return Arrays.stream(playerInventory.getArmorContents())
            .filter(Objects::nonNull)
            .anyMatch(armorItem -> armorItem.isSimilar(item));
    }

    private void logSoldFish(@NotNull List<SoldFish> soldFish, double totalWorth, int fishSold) {
        if (!DatabaseUtil.isDatabaseOnline()) {
            return;
        }
        final UUID uuid = player.getUniqueId();

        final int userId = EvenMoreFish.getInstance().getPluginDataManager().getUserManager().getUserId(uuid);
        final String transactionId = FriendlyId.createFriendlyId();
        final Timestamp timestamp = Timestamp.from(Instant.now());

        Database database = EvenMoreFish.getInstance().getPluginDataManager().getDatabase();

        database.createTransaction(transactionId, userId, timestamp);
        soldFish.forEach(fish -> database.createSale(
            transactionId,
            fish.getName(),
            fish.getRarity(),
            fish.getAmount(),
            fish.getLength(),
            fish.getTotalValue()
        ));

        final DataManager<UserReport> userReportDataManager = EvenMoreFish.getInstance().getPluginDataManager().getUserReportDataManager();
        final UserReport report = userReportDataManager.get(uuid.toString());
        report.incrementFishSold(fishSold);
        report.incrementMoneyEarned(totalWorth);

        userReportDataManager.update(uuid.toString(), report);
    }

    public static void sellInventory(@NotNull Inventory inventory, @NotNull HumanEntity humanEntity) {
        if (!(humanEntity instanceof Player player)) {
            return;
        }

        if (!Economy.getInstance().isEnabled()) {
            ConfigMessage.ECONOMY_DISABLED.getMessage().send(player);
            return;
        }

        new SellHelper(inventory, player).sell();
    }

    public static double calculateInventoryWorth(@NotNull Inventory inventory) {
        return fetchFish(inventory, false).stream().mapToDouble(SoldFish::getTotalValue).sum();
    }

}
