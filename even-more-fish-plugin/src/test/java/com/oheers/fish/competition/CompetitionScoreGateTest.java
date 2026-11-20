package com.oheers.fish.competition;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompetitionScoreGateTest {

    @Test
    void queuedScoreAfterFinishCannotChangeFinalResult() {
        CompetitionScoreGate gate = new CompetitionScoreGate();
        List<String> board = new ArrayList<>();
        gate.start();

        assertTrue(gate.acceptScore(() -> true, () -> board.add("first")));
        List<String> result = gate.close(() -> List.copyOf(board));

        assertFalse(gate.acceptScore(() -> true, () -> board.add("late")));
        assertEquals(List.of("first"), result);
        assertEquals(result, board);
    }

    @Test
    void scoreIsRejectedAsSoonAsFinishBegins() {
        CompetitionScoreGate gate = new CompetitionScoreGate();
        List<String> board = new ArrayList<>();
        gate.start();

        List<String> result = gate.close(() -> {
            assertFalse(gate.acceptScore(() -> true, () -> board.add("during finish")));
            return List.copyOf(board);
        });

        assertTrue(result.isEmpty());
        assertTrue(board.isEmpty());
    }

    @Test
    void oldCompetitionCannotScoreAfterReplacement() {
        CompetitionScoreGate gate = new CompetitionScoreGate();
        AtomicBoolean stillActive = new AtomicBoolean(true);
        List<String> board = new ArrayList<>();
        gate.start();

        stillActive.set(false);
        assertFalse(gate.acceptScore(stillActive::get, () -> board.add("old competition")));
        assertTrue(board.isEmpty());
    }

    @Test
    void winningScoreCanCloseCompetitionOnce() {
        CompetitionScoreGate gate = new CompetitionScoreGate();
        List<String> board = new ArrayList<>();
        gate.start();

        assertTrue(gate.acceptScore(() -> true, () -> {
            board.add("winner");
            assertEquals(List.of("winner"), gate.close(() -> List.copyOf(board)));
        }));
        assertFalse(gate.acceptScore(() -> true, () -> board.add("next catch")));
        assertNull(gate.close(() -> List.copyOf(board)));
        gate.markEnded();
        assertFalse(gate.acceptScore(() -> true, () -> board.add("after payout")));
    }

    @Test
    void finishWaitsForAnAcceptedScoreBeforeTakingTheSnapshot() throws Exception {
        CompetitionScoreGate gate = new CompetitionScoreGate();
        List<String> board = new ArrayList<>();
        CountDownLatch scoring = new CountDownLatch(1);
        CountDownLatch releaseScore = new CountDownLatch(1);
        CountDownLatch finishing = new CountDownLatch(1);
        gate.start();

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> score = executor.submit(() -> gate.acceptScore(() -> true, () -> {
                scoring.countDown();
                try {
                    assertTrue(releaseScore.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(exception);
                }
                board.add("accepted");
            }));
            assertTrue(scoring.await(5, TimeUnit.SECONDS));

            Future<List<String>> result = executor.submit(() -> {
                finishing.countDown();
                return gate.close(() -> List.copyOf(board));
            });
            assertTrue(finishing.await(5, TimeUnit.SECONDS));
            assertFalse(result.isDone());
            releaseScore.countDown();

            assertTrue(score.get(5, TimeUnit.SECONDS));
            assertEquals(List.of("accepted"), result.get(5, TimeUnit.SECONDS));
        } finally {
            releaseScore.countDown();
        }
    }

}
