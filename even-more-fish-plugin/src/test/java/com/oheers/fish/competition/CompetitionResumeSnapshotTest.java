package com.oheers.fish.competition;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CompetitionResumeSnapshotTest {

    @Test
    void versionedSnapshotRoundTripsTheResolvedConditionAndStartTime() throws Exception {
        YamlDocument config = YamlDocument.create(InputStream.nullInputStream());
        CompetitionResumeSnapshot saved = new CompetitionResumeSnapshot(
            "random", "specific_fish", 4, 1_766_000_000_000L, true,
            Map.of("selected-fish", "legendary:golden_cod")
        );

        saved.write(config);

        assertEquals(saved, CompetitionResumeSnapshot.read(config));
    }

    @Test
    void aLaterBackupReplacesPreviousTypeState() throws Exception {
        YamlDocument config = YamlDocument.create(InputStream.nullInputStream());
        new CompetitionResumeSnapshot("random", "specific_fish", 4, 1_766_000_000_000L,
            true, Map.of("selected-fish", "legendary:golden_cod")).write(config);
        CompetitionResumeSnapshot latest = new CompetitionResumeSnapshot("random", "specific_rarity", 2,
            1_766_000_100_000L, true, Map.of("selected-rarity", "epic"));

        latest.write(config);

        assertEquals(latest, CompetitionResumeSnapshot.read(config));
    }

    @Test
    void unversionedBackupIsNotAutomaticallyInterpreted() throws Exception {
        YamlDocument config = YamlDocument.create(InputStream.nullInputStream());
        config.set("comp-id", "contest");
        config.set("start-time", 1_766_000_000L);

        assertNull(CompetitionResumeSnapshot.read(config));
    }
}
