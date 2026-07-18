package com.oheers.fish.gui.guis;

import com.oheers.fish.database.model.user.UserFishStats;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FishJournalGuiTest {

    @Test
    void getDiscovererReturnsUnknownWhenFishStatsAreMissing() {
        assertEquals("Unknown", FishJournalGui.getDiscoverer(null, "Unknown"));
    }

    @Test
    void getDiscoverDateReturnsUnknownWhenUserFishStatsAreMissing() {
        assertEquals("Unknown", FishJournalGui.getDiscoverDate(null, "Unknown"));
    }

    @Test
    void getDiscoverDateFormatsKnownFirstCatchDate() {
        UserFishStats stats = new UserFishStats(
            1,
            "Cod",
            "common",
            LocalDateTime.of(2026, 7, 18, 1, 1),
            10.0F,
            10.0F,
            1
        );

        assertEquals("2026-07-18", FishJournalGui.getDiscoverDate(stats, "Unknown"));
    }

}
