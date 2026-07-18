package com.oheers.fish.gui;

import com.oheers.fish.fishing.items.Rarity;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class GuiUtilsTest {

    @Test
    void openJournalFromMainMenuDoesNotUseExpectedGuiGuard() {
        Player player = mock(Player.class);
        CapturingJournalOpener opener = new CapturingJournalOpener();

        GuiUtils.openJournalFromMainMenu(player, opener);

        assertSame(player, opener.player);
        assertNull(opener.rarity);
        assertNull(opener.expectedOpenGui);
    }

    private static class CapturingJournalOpener implements GuiUtils.JournalOpener {
        private Player player;
        private Rarity rarity;
        private InventoryGui expectedOpenGui;

        @Override
        public void open(Player player, Rarity rarity, InventoryGui expectedOpenGui) {
            this.player = player;
            this.rarity = rarity;
            this.expectedOpenGui = expectedOpenGui;
        }
    }

}
