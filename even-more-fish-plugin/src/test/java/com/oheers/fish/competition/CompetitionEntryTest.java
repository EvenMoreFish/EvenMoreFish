package com.oheers.fish.competition;

import com.oheers.fish.api.fishing.items.IFish;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompetitionEntryTest {

    @Test
    void finalSnapshotKeepsScoreAndCatchTimeWhenLiveEntryChanges() {
        IFish fish = mock(IFish.class);
        CompetitionType type = mock(CompetitionType.class);
        when(type.useFishLength()).thenReturn(false);
        CompetitionEntry live = new CompetitionEntry(UUID.randomUUID(), fish, type);
        live.setValue(4.5F);

        CompetitionEntry finalEntry = live.snapshot();
        live.setValue(9F);

        assertEquals(4.5F, finalEntry.getValue());
        assertEquals(live.getTime(), finalEntry.getTime());
        assertEquals(live.getPlayer(), finalEntry.getPlayer());
    }

}
