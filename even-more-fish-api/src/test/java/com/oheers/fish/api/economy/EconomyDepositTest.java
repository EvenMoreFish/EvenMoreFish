package com.oheers.fish.api.economy;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EconomyDepositTest {

    @Test
    void depositsToEveryAvailableProviderAndSkipsUnavailableProviders() {
        EconomyType first = mock(EconomyType.class);
        EconomyType unavailable = mock(EconomyType.class);
        EconomyType second = mock(EconomyType.class);
        OfflinePlayer player = mock(OfflinePlayer.class);

        when(first.isAvailable()).thenReturn(true);
        when(unavailable.isAvailable()).thenReturn(false);
        when(second.isAvailable()).thenReturn(true);

        Economy.economy(List.of(first, unavailable, second)).deposit(player, 42.5, true);

        verify(first).deposit(player, 42.5, true);
        verify(second).deposit(player, 42.5, true);
        verify(unavailable).isAvailable();
        verify(unavailable, never()).deposit(player, 42.5, true);
    }

    @Test
    void failedProviderReversesEarlierDeposit() {
        EconomyType first = mock(EconomyType.class);
        EconomyType failing = mock(EconomyType.class);
        OfflinePlayer player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(first.isAvailable()).thenReturn(true);
        when(failing.isAvailable()).thenReturn(true);
        when(first.deposit(player, 20.0, true)).thenReturn(true);
        when(failing.deposit(player, 20.0, true)).thenReturn(false);
        when(first.withdraw(player, 20.0, true)).thenReturn(true);

        Economy.DepositResult result = Economy.economy(List.of(first, failing)).depositForSale(player, 20.0, true);

        assertEquals(Economy.DepositResult.FAILED, result);
        verify(first).withdraw(player, 20.0, true);
    }

    @Test
    void failedCompensationIsReportedAsPartial() {
        EconomyType first = mock(EconomyType.class);
        EconomyType failing = mock(EconomyType.class);
        OfflinePlayer player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(first.isAvailable()).thenReturn(true);
        when(failing.isAvailable()).thenReturn(true);
        when(first.deposit(player, 20.0, true)).thenReturn(true);
        when(failing.deposit(player, 20.0, true)).thenReturn(false);
        when(first.withdraw(player, 20.0, true)).thenReturn(false);

        Economy.DepositResult result = Economy.economy(List.of(first, failing)).depositForSale(player, 20.0, true);

        assertEquals(Economy.DepositResult.PARTIAL, result);
        verify(first).withdraw(player, 20.0, true);
    }

    @Test
    void thrownDepositReversesEarlierDeposit() {
        EconomyType first = mock(EconomyType.class);
        EconomyType throwing = mock(EconomyType.class);
        OfflinePlayer player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(first.isAvailable()).thenReturn(true);
        when(throwing.isAvailable()).thenReturn(true);
        when(first.deposit(player, 20.0, true)).thenReturn(true);
        when(throwing.deposit(player, 20.0, true)).thenThrow(new IllegalStateException("provider down"));
        when(first.withdraw(player, 20.0, true)).thenReturn(true);

        Economy.DepositResult result = Economy.economy(List.of(first, throwing)).depositForSale(player, 20.0, true);

        assertEquals(Economy.DepositResult.FAILED, result);
        verify(first).withdraw(player, 20.0, true);
    }
}
