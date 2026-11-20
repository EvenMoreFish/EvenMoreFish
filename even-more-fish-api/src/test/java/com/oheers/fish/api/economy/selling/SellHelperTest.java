package com.oheers.fish.api.economy.selling;

import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.events.EMFFishPreSaleEvent;
import com.oheers.fish.api.events.EMFFishSoldEvent;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SellHelperTest {

    @Test
    void failedPaymentKeepsInventoryFishAndDoesNotReportSale() {
        Economy economy = mock(Economy.class);
        Player player = mock(Player.class);
        Inventory inventory = mock(Inventory.class);
        IFish fish = mock(IFish.class);
        SoldFish sold = mock(SoldFish.class);
        EMFPlugin plugin = mock(EMFPlugin.class);
        ItemStack item = mock(ItemStack.class);
        ItemStack snapshot = mock(ItemStack.class);

        when(economy.isEnabled()).thenReturn(true);
        when(economy.depositForSale(player, 12.0, true)).thenReturn(Economy.DepositResult.FAILED);
        when(inventory.getSize()).thenReturn(1);
        when(inventory.getItem(0)).thenReturn(item);
        when(item.clone()).thenReturn(snapshot);
        when(item.getAmount()).thenReturn(2);
        when(snapshot.getAmount()).thenReturn(2);
        when(item.isSimilar(snapshot)).thenReturn(true);
        when(sold.getFish()).thenReturn(fish);
        when(sold.getValue()).thenReturn(6.0);
        when(sold.getFinalValue()).thenReturn(12.0);
        when(sold.getQuantity()).thenReturn(2);

        try (MockedStatic<SoldFish> soldFish = mockStatic(SoldFish.class);
             MockedStatic<EMFPlugin> emfPlugin = mockStatic(EMFPlugin.class);
             MockedConstruction<EMFFishPreSaleEvent> preSale = mockConstruction(
                 EMFFishPreSaleEvent.class,
                 (event, context) -> when(event.callEvent()).thenReturn(true)
             )) {
            soldFish.when(() -> SoldFish.get(player, item)).thenReturn(sold);
            emfPlugin.when(EMFPlugin::getInstance).thenReturn(plugin);

            new SellHelper(economy).sell(inventory, player);

            verify(inventory, never()).setItem(0, null);
            verify(plugin).sendMessage("FISH_SALE_FAILED", player);
            verify(plugin, never()).sendSoldMessage(anyDouble(), anyInt(), any(Player.class));
            assertEquals(1, preSale.constructed().size());
        }
    }

    @Test
    void failedPaymentKeepsArrayFish() {
        Economy economy = mock(Economy.class);
        Player player = mock(Player.class);
        IFish fish = mock(IFish.class);
        SoldFish sold = mock(SoldFish.class);
        EMFPlugin plugin = mock(EMFPlugin.class);
        ItemStack item = mock(ItemStack.class);
        ItemStack snapshot = mock(ItemStack.class);

        when(economy.isEnabled()).thenReturn(true);
        when(economy.depositForSale(player, 6.0, true)).thenReturn(Economy.DepositResult.FAILED);
        when(item.clone()).thenReturn(snapshot);
        when(item.getAmount()).thenReturn(1);
        when(snapshot.getAmount()).thenReturn(1);
        when(item.isSimilar(snapshot)).thenReturn(true);
        when(sold.getFish()).thenReturn(fish);
        when(sold.getValue()).thenReturn(6.0);
        when(sold.getFinalValue()).thenReturn(6.0);
        when(sold.getQuantity()).thenReturn(1);

        try (MockedStatic<SoldFish> soldFish = mockStatic(SoldFish.class);
             MockedStatic<EMFPlugin> emfPlugin = mockStatic(EMFPlugin.class);
             MockedConstruction<EMFFishPreSaleEvent> preSale = mockConstruction(
                 EMFFishPreSaleEvent.class,
                 (event, context) -> when(event.callEvent()).thenReturn(true)
             )) {
            soldFish.when(() -> SoldFish.get(player, item)).thenReturn(sold);
            emfPlugin.when(EMFPlugin::getInstance).thenReturn(plugin);

            new SellHelper(economy).sell(new ItemStack[]{item}, player);

            verify(item, never()).setAmount(0);
            verify(plugin).sendMessage("FISH_SALE_FAILED", player);
            verify(plugin, never()).sendSoldMessage(anyDouble(), anyInt(), any(Player.class));
            assertEquals(1, preSale.constructed().size());
        }
    }

    @Test
    void successfulPaymentConsumesFishAndReportsSale() {
        Economy economy = mock(Economy.class);
        Player player = mock(Player.class);
        Inventory inventory = mock(Inventory.class);
        IFish fish = mock(IFish.class);
        SoldFish sold = mock(SoldFish.class);
        EMFPlugin plugin = mock(EMFPlugin.class);
        ItemStack item = mock(ItemStack.class);
        ItemStack snapshot = mock(ItemStack.class);

        when(economy.isEnabled()).thenReturn(true);
        when(economy.depositForSale(player, 6.0, true)).thenReturn(Economy.DepositResult.SUCCESS);
        when(inventory.getSize()).thenReturn(1);
        when(inventory.getItem(0)).thenReturn(item);
        when(item.clone()).thenReturn(snapshot);
        when(item.getAmount()).thenReturn(1);
        when(snapshot.getAmount()).thenReturn(1);
        when(item.isSimilar(snapshot)).thenReturn(true);
        when(sold.getFish()).thenReturn(fish);
        when(sold.getValue()).thenReturn(6.0);
        when(sold.getFinalValue()).thenReturn(6.0);
        when(sold.getQuantity()).thenReturn(1);
        when(fish.getSellRewards()).thenReturn(List.of());

        try (MockedStatic<SoldFish> soldFish = mockStatic(SoldFish.class);
             MockedStatic<EMFPlugin> emfPlugin = mockStatic(EMFPlugin.class);
             MockedConstruction<EMFFishPreSaleEvent> preSale = mockConstruction(
                 EMFFishPreSaleEvent.class,
                 (event, context) -> when(event.callEvent()).thenReturn(true)
             );
             MockedConstruction<EMFFishSoldEvent> soldEvent = mockConstruction(EMFFishSoldEvent.class)) {
            soldFish.when(() -> SoldFish.get(player, item)).thenReturn(sold);
            emfPlugin.when(EMFPlugin::getInstance).thenReturn(plugin);

            new SellHelper(economy).sell(inventory, player);

            verify(inventory).setItem(0, null);
            verify(plugin).logSoldFish(sold);
            verify(plugin).sendSoldMessage(6.0, 1, player);
            assertEquals(1, preSale.constructed().size());
            assertEquals(1, soldEvent.constructed().size());
        }
    }
}
