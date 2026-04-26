package com.oheers.fish.baits.configs;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaitFileUpdatesTest {

    @Test
    void migratesLegacyBaitSectionsToModifierSections() throws IOException {
        YamlDocument config = yaml("""
            fish:
              Common:
                - "Carp"
                - "Bluefish"
              Rare:
                - "Nemo"
            rarities:
              - "Epic"
              - "Legendary"
            """);

        boolean changed = BaitFileUpdates.migrateLegacyBaitModifiers(config, 1.5D);

        assertTrue(changed);
        assertNull(config.get("fish"));
        assertNull(config.get("rarities"));
        assertEquals("*1.5", config.getString("rarity-modifiers.Epic"));
        assertEquals("*1.5", config.getString("rarity-modifiers.Legendary"));
        assertEquals("*1.5", config.getString("fish-modifiers.Common.Carp"));
        assertEquals("*1.5", config.getString("fish-modifiers.Common.Bluefish"));
        assertEquals("*1.5", config.getString("fish-modifiers.Rare.Nemo"));
    }

    @Test
    void removesLegacySectionsWithoutOverwritingExistingModifierSections() throws IOException {
        YamlDocument config = yaml("""
            fish:
              Common:
                - "Carp"
            rarities:
              - "Epic"
            rarity-modifiers:
              Epic: "+50"
            fish-modifiers:
              Common:
                Carp: "+25"
            """);

        boolean changed = BaitFileUpdates.migrateLegacyBaitModifiers(config, 1.5D);

        assertTrue(changed);
        assertNull(config.get("fish"));
        assertNull(config.get("rarities"));
        assertEquals("+50", config.getString("rarity-modifiers.Epic"));
        assertEquals("+25", config.getString("fish-modifiers.Common.Carp"));
    }

    @Test
    void doesNothingWhenNoLegacySectionsExist() throws IOException {
        YamlDocument config = yaml("""
            rarity-modifiers:
              Epic: "+50"
            fish-modifiers:
              Common:
                Carp: "+25"
            """);

        boolean changed = BaitFileUpdates.migrateLegacyBaitModifiers(config, 1.5D);

        assertFalse(changed);
        assertEquals("+50", config.getString("rarity-modifiers.Epic"));
        assertEquals("+25", config.getString("fish-modifiers.Common.Carp"));
    }

    private static YamlDocument yaml(String content) throws IOException {
        return YamlDocument.create(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }
}
