package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompetitionSingleWinnerRewardTest {

    @TempDir
    Path directory;

    @Test
    void missingFirstPlaceRewardsDoNotInterruptWinnerAnnouncement() throws Exception {
        Reward participation = mock(Reward.class);
        Reward secondPlace = mock(Reward.class);

        runWinner(Map.of(), null);
        runWinner(Map.of(-1, List.of(participation)), participation);
        runWinner(Map.of(2, List.of(secondPlace)), secondPlace);
    }

    @Test
    void configuredFirstPlaceRewardIsGivenOnce() throws Exception {
        Reward firstPlace = mock(Reward.class);
        runWinner(Map.of(1, List.of(firstPlace)), firstPlace);
    }

    private void runWinner(Map<Integer, List<Reward>> configured, Reward listedReward) throws Exception {
        UUID winner = UUID.randomUUID();
        OfflinePlayer player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(winner);
        EvenMoreFish pluginInstance = mock(EvenMoreFish.class);
        when(pluginInstance.getDataFolder()).thenReturn(directory.toFile());
        CompetitionType type = mock(CompetitionType.class);
        when(type.isSingleReward()).thenReturn(true);
        when(type.getTypeVariable(org.mockito.ArgumentMatchers.any())).thenReturn(Component.empty());
        CompetitionEntry entry = mock(CompetitionEntry.class);
        when(entry.getPlayer()).thenReturn(winner);

        try (MockedStatic<EvenMoreFish> plugin = mockStatic(EvenMoreFish.class);
             MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedStatic<DatabaseUtil> database = mockStatic(DatabaseUtil.class)) {
            plugin.when(EvenMoreFish::getInstance).thenReturn(pluginInstance);
            bukkit.when(() -> Bukkit.getOfflinePlayer(winner)).thenReturn(player);
            database.when(DatabaseUtil::isDatabaseOnline).thenReturn(false);

            TestCompetition competition = new TestCompetition(type);
            competition.setSingleWinner(winner);
            rewardsOf(competition).putAll(configured);
            Method processRewards = Competition.class.getDeclaredMethod("processRewards", List.class);
            processRewards.setAccessible(true);

            assertDoesNotThrow(() -> processRewards.invoke(competition, List.of(entry)));
            verify(competition.message).broadcast();
            if (listedReward != null) {
                if (configured.containsKey(1)) {
                    verify(listedReward).give(player, null);
                } else {
                    verify(listedReward, never()).give(player, null);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, List<Reward>> rewardsOf(Competition competition) throws Exception {
        Field field = Competition.class.getDeclaredField("rewards");
        field.setAccessible(true);
        return (Map<Integer, List<Reward>>) field.get(competition);
    }

    private static final class TestCompetition extends Competition {
        private final EMFMessage message = mock(EMFMessage.class);

        private TestCompetition(CompetitionType type) {
            super(60, type);
        }

        @Override
        public EMFMessage format(ConfigMessage configMessage) {
            return message;
        }
    }
}
