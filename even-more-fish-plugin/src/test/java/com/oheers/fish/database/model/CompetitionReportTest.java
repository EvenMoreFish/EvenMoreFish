package com.oheers.fish.database.model;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.competition.CompetitionEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompetitionReportTest {

    @Test
    void reportUsesTheCapturedFinalOrderAndScores() {
        UUID winner = UUID.randomUUID();
        UUID runnerUp = UUID.randomUUID();
        IRarity rarity = mock(IRarity.class);
        IFish fish = mock(IFish.class);
        CompetitionEntry first = mock(CompetitionEntry.class);
        CompetitionEntry second = mock(CompetitionEntry.class);
        when(rarity.getId()).thenReturn("legendary");
        when(fish.getId()).thenReturn("cod");
        when(fish.getRarity()).thenReturn(rarity);
        when(first.getPlayer()).thenReturn(winner);
        when(first.getValue()).thenReturn(12.5F);
        when(first.getFish()).thenReturn(fish);
        when(second.getPlayer()).thenReturn(runnerUp);

        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 12, 0);
        LocalDateTime end = start.plusMinutes(5);
        CompetitionReport report = new CompetitionReport("contest", List.of(first, second), start, end);

        assertEquals("contest", report.getCompetitionConfigId());
        assertEquals(winner, report.getWinnerUuid());
        assertEquals("cod.legendary", report.getWinnerFish());
        assertEquals(12.5F, report.getWinnerScore());
        assertEquals(List.of(winner, runnerUp), report.getContestants());
    }

}
