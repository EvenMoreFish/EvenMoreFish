package com.oheers.fish;

import dev.watchwolf.tester.AbstractTest;
import dev.watchwolf.tester.TesterConnector;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WatchWolfSmokeTest.class)
class WatchWolfSmokeTest extends AbstractTest {
    @Override
    public String getConfigFile() {
        return "src/testWatchWolf/resources/watchwolf.yaml";
    }

    @ParameterizedTest
    @ArgumentsSource(WatchWolfSmokeTest.class)
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void loadsEvenMoreFish(TesterConnector connector) throws IOException {
        String response = connector.server.runCommand("version EvenMoreFish");
        assertTrue(response.toLowerCase(Locale.ROOT).contains("evenmorefish"),
            () -> "Expected Paper to report EvenMoreFish as loaded, but got: " + response);

        connector.server.runCommand("emf admin reload");
        String afterReload = connector.server.runCommand("version EvenMoreFish");
        assertTrue(afterReload.toLowerCase(Locale.ROOT).contains("evenmorefish"),
            () -> "Expected EvenMoreFish to remain available after reload, but got: " + afterReload);

        connector.server.runCommand("emf admin competition test");
        String alreadyRunning = connector.server.runCommand("emf admin competition test");
        assertTrue(alreadyRunning.toLowerCase(Locale.ROOT).contains("already"),
            () -> "Expected the second start to find an active competition, but got: " + alreadyRunning);

        connector.server.runCommand("emf admin competition end");
        String noCompetition = connector.server.runCommand("emf admin competition end");
        assertTrue(noCompetition.toLowerCase(Locale.ROOT).contains("no competition"),
            () -> "Expected the competition to be ended, but got: " + noCompetition);
    }
}
