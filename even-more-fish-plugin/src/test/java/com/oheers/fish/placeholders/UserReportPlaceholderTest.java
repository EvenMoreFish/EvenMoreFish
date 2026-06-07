package com.oheers.fish.placeholders;

import com.oheers.fish.database.model.user.EmptyUserReport;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.placeholders.abstracted.UserReportPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalCompetitionsJoinedPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalCompetitionsWonPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalFishCaughtPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalFishSoldPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalMoneyEarnedPlaceholder;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserReportPlaceholderTest {

    @Test
    void placeholdersReturnDefaultsWhenUserReportIsMissing() {
        UUID uuid = UUID.randomUUID();
        OfflinePlayer player = offlinePlayer(uuid);

        assertEquals("0", new StubTotalCompetitionsJoinedPlaceholder(null).parsePAPI(player, "total_competitions_joined_player"));
        assertEquals("0", new StubTotalCompetitionsWonPlaceholder(null).parsePAPI(player, "total_competitions_won_player"));
        assertEquals("0", new StubTotalFishCaughtPlaceholder(null).parsePAPI(player, "total_fish_caught_player"));
        assertEquals("0", new StubTotalFishSoldPlaceholder(null).parsePAPI(player, "total_fish_sold_player"));
        assertEquals("0.00", new StubTotalMoneyEarnedPlaceholder(null).parsePAPI(player, "total_money_earned_player"));
    }

    @Test
    void placeholdersReturnZeroValuesFromExistingEmptyReport() {
        UUID uuid = UUID.randomUUID();
        OfflinePlayer player = offlinePlayer(uuid);
        UserReport report = new EmptyUserReport(uuid);

        assertEquals("0", new StubTotalCompetitionsJoinedPlaceholder(report).parsePAPI(player, "total_competitions_joined_player"));
        assertEquals("0", new StubTotalCompetitionsWonPlaceholder(report).parsePAPI(player, "total_competitions_won_player"));
        assertEquals("0", new StubTotalFishCaughtPlaceholder(report).parsePAPI(player, "total_fish_caught_player"));
        assertEquals("0", new StubTotalFishSoldPlaceholder(report).parsePAPI(player, "total_fish_sold_player"));
        assertEquals("0.00", new StubTotalMoneyEarnedPlaceholder(report).parsePAPI(player, "total_money_earned_player"));
    }

    @Test
    void totalFishSoldUsesOfflinePlayerUuidForPlayerIdentifiers() {
        UUID uuid = UUID.randomUUID();
        OfflinePlayer player = offlinePlayer(uuid);
        AtomicReference<UUID> resolvedUuid = new AtomicReference<>();

        UserReportPlaceholder placeholder = new StubTotalFishSoldPlaceholder(new EmptyUserReport(uuid)) {
            @Override
            public UserReport fetchUserReport(UUID lookupUuid) {
                resolvedUuid.set(lookupUuid);
                return super.fetchUserReport(lookupUuid);
            }
        };

        placeholder.parsePAPI(player, "total_fish_sold_player");

        assertEquals(uuid, resolvedUuid.get());
    }

    @Test
    void totalFishSoldSupportsExplicitUuidIdentifiers() {
        UUID uuid = UUID.randomUUID();
        UserReport report = new EmptyUserReport(uuid);
        report.incrementFishSold(37);

        String result = new StubTotalFishSoldPlaceholder(report).parsePAPI(null, "total_fish_sold_" + uuid);

        assertEquals("37", result);
    }

    @Test
    void totalFishSoldReturnsNullForInvalidIdentifierSuffix() {
        String result = new StubTotalFishSoldPlaceholder(null).parsePAPI(null, "total_fish_sold_not-a-uuid");

        assertNull(result);
    }

    @Test
    void totalFishSoldHandlesVeryLargeAmounts() {
        UUID uuid = UUID.randomUUID();
        UserReport report = new EmptyUserReport(uuid);
        report.incrementFishSold(Integer.MAX_VALUE);

        String result = new StubTotalFishSoldPlaceholder(report).parsePAPI(null, "total_fish_sold_" + uuid);

        assertEquals(String.valueOf(Integer.MAX_VALUE), result);
    }

    @Test
    void totalMoneyEarnedFormatsLargeValuesToTwoDecimals() {
        UUID uuid = UUID.randomUUID();
        UserReport report = new EmptyUserReport(uuid);
        report.incrementMoneyEarned(123456789.987d);

        String result = new StubTotalMoneyEarnedPlaceholder(report).parsePAPI(null, "total_money_earned_" + uuid);

        assertEquals("123456789.99", result);
    }

    private static OfflinePlayer offlinePlayer(UUID uuid) {
        OfflinePlayer player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(uuid);
        return player;
    }

    private static final class StubTotalCompetitionsJoinedPlaceholder extends TotalCompetitionsJoinedPlaceholder {
        private final UserReport report;

        private StubTotalCompetitionsJoinedPlaceholder(UserReport report) {
            this.report = report;
        }

        @Override
        public UserReport fetchUserReport(UUID uuid) {
            return report;
        }

        @Override
        public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
            UUID uuid = fetchPlayerOrUUIDString(player, identifier.substring("total_competitions_joined_".length()));
            if (uuid == null) {
                return null;
            }
            UserReport resolvedReport = fetchUserReport(uuid);
            return resolvedReport == null ? "0" : String.valueOf(resolvedReport.getCompetitionsJoined());
        }
    }

    private static final class StubTotalCompetitionsWonPlaceholder extends TotalCompetitionsWonPlaceholder {
        private final UserReport report;

        private StubTotalCompetitionsWonPlaceholder(UserReport report) {
            this.report = report;
        }

        @Override
        public UserReport fetchUserReport(UUID uuid) {
            return report;
        }

        @Override
        public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
            UUID uuid = fetchPlayerOrUUIDString(player, identifier.substring("total_competitions_won_".length()));
            if (uuid == null) {
                return null;
            }
            UserReport resolvedReport = fetchUserReport(uuid);
            return resolvedReport == null ? "0" : String.valueOf(resolvedReport.getCompetitionsWon());
        }
    }

    private static final class StubTotalFishCaughtPlaceholder extends TotalFishCaughtPlaceholder {
        private final UserReport report;

        private StubTotalFishCaughtPlaceholder(UserReport report) {
            this.report = report;
        }

        @Override
        public UserReport fetchUserReport(UUID uuid) {
            return report;
        }

        @Override
        public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
            UUID uuid = fetchPlayerOrUUIDString(player, identifier.substring("total_fish_caught_".length()));
            if (uuid == null) {
                return null;
            }
            UserReport resolvedReport = fetchUserReport(uuid);
            return resolvedReport == null ? "0" : String.valueOf(resolvedReport.getNumFishCaught());
        }
    }

    private static class StubTotalFishSoldPlaceholder extends TotalFishSoldPlaceholder {
        private final UserReport report;

        private StubTotalFishSoldPlaceholder(UserReport report) {
            this.report = report;
        }

        @Override
        public UserReport fetchUserReport(UUID uuid) {
            return report;
        }

        @Override
        public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
            UUID uuid = fetchPlayerOrUUIDString(player, identifier.substring("total_fish_sold_".length()));
            if (uuid == null) {
                return null;
            }
            UserReport resolvedReport = fetchUserReport(uuid);
            return resolvedReport == null ? "0" : String.valueOf(resolvedReport.getFishSold());
        }
    }

    private static final class StubTotalMoneyEarnedPlaceholder extends TotalMoneyEarnedPlaceholder {
        private final UserReport report;

        private StubTotalMoneyEarnedPlaceholder(UserReport report) {
            this.report = report;
        }

        @Override
        public UserReport fetchUserReport(UUID uuid) {
            return report;
        }

        @Override
        public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
            UUID uuid = fetchPlayerOrUUIDString(player, identifier.substring("total_money_earned_".length()));
            if (uuid == null) {
                return null;
            }
            UserReport resolvedReport = fetchUserReport(uuid);
            return resolvedReport == null ? "0.00" : String.format("%.2f", resolvedReport.getMoneyEarned());
        }
    }
}
