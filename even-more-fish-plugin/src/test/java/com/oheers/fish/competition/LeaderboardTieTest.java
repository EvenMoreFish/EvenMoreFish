package com.oheers.fish.competition;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaderboardTieTest {

    private static final UUID FIRST = new UUID(0, 1);
    private static final UUID SECOND = new UUID(0, 2);

    @Test
    void equalScoreAndTimeRetainBothPlayersInEitherDirection() {
        for (boolean reverse : List.of(false, true)) {
            Leaderboard board = new Leaderboard(type(reverse));
            CompetitionEntry second = entry(SECOND, 5, 100);
            CompetitionEntry first = entry(FIRST, 5, 100);

            assertTrue(board.restoreEntry(second));
            assertTrue(board.restoreEntry(first));
            assertEquals(List.of(FIRST, SECOND), board.getEntries().stream().map(CompetitionEntry::getPlayer).toList());
            assertEquals(2, board.getSize());
        }
    }

    @Test
    void scoreAndTimeKeepTheirExistingPriority() {
        Leaderboard largest = new Leaderboard(type(false));
        assertTrue(largest.restoreEntry(entry(FIRST, 4, 100)));
        assertTrue(largest.restoreEntry(entry(SECOND, 5, 200)));
        assertEquals(SECOND, largest.getTopEntry().getPlayer());

        Leaderboard shortest = new Leaderboard(type(true));
        assertTrue(shortest.restoreEntry(entry(FIRST, 4, 100)));
        assertTrue(shortest.restoreEntry(entry(SECOND, 5, 200)));
        assertEquals(FIRST, shortest.getTopEntry().getPlayer());

        Leaderboard equalLargest = new Leaderboard(type(false));
        assertTrue(equalLargest.restoreEntry(entry(FIRST, 5, 200)));
        assertTrue(equalLargest.restoreEntry(entry(SECOND, 5, 100)));
        assertEquals(SECOND, equalLargest.getTopEntry().getPlayer());

        Leaderboard equalShortest = new Leaderboard(type(true));
        assertTrue(equalShortest.restoreEntry(entry(FIRST, 5, 200)));
        assertTrue(equalShortest.restoreEntry(entry(SECOND, 5, 100)));
        assertEquals(FIRST, equalShortest.getTopEntry().getPlayer());
    }

    @Test
    void replacingSameComparatorKeyKeepsExactlyOneCurrentEntry() {
        Leaderboard board = new Leaderboard(type(false));
        CompetitionEntry original = entry(FIRST, 5, 100);
        CompetitionEntry replacement = entry(FIRST, 5, 100);
        assertTrue(board.restoreEntry(original));

        board.replaceEntry(original, replacement);

        assertEquals(1, board.getSize());
        assertSame(replacement, board.getEntry(FIRST));
    }

    @Test
    void replacingOneTiedPlayerDoesNotRemoveTheOther() {
        Leaderboard board = new Leaderboard(type(false));
        CompetitionEntry first = entry(FIRST, 5, 100);
        CompetitionEntry second = entry(SECOND, 5, 100);
        assertTrue(board.restoreEntry(first));
        assertTrue(board.restoreEntry(second));

        CompetitionEntry updated = entry(SECOND, 4, 101);
        board.replaceEntry(second, updated);

        assertEquals(2, board.getSize());
        assertSame(first, board.getEntry(FIRST));
        assertSame(updated, board.getEntry(SECOND));
    }

    private CompetitionType type(boolean reverse) {
        CompetitionType type = mock(CompetitionType.class);
        when(type.shouldReverseLeaderboard()).thenReturn(reverse);
        return type;
    }

    private CompetitionEntry entry(UUID player, float value, long time) {
        CompetitionType type = type(false);
        CompetitionEntry entry = new CompetitionEntry(player, mock(IFish.class), type);
        entry.value = value;
        entry.time = time;
        return entry;
    }
}
