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
    }
}
