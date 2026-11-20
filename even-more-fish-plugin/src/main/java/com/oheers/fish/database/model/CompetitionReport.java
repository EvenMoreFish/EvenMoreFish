package com.oheers.fish.database.model;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.database.data.FishRarityKey;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CompetitionReport {

    private final UUID runId;
    private final String competitionConfigId;
    private final String winnerFish;
    private final UUID winnerUuid;
    private final List<UUID> contestants;

    private final float winnerScore;

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;


    public CompetitionReport(String competitionConfigID, String winnerFish, String winnerUUIDString, float winnerScore, String contestants, LocalDateTime startTime, LocalDateTime endTime) {
        this(UUID.randomUUID(), competitionConfigID, winnerFish, winnerUUIDString, winnerScore, contestants, startTime, endTime);
    }

    public CompetitionReport(UUID runId, String competitionConfigID, String winnerFish, String winnerUUIDString, float winnerScore, String contestants, LocalDateTime startTime, LocalDateTime endTime) {
        this.runId = runId;
        this.competitionConfigId = competitionConfigID;
        this.winnerFish = winnerFish;
        this.winnerUuid = UUID.fromString(winnerUUIDString);
        this.winnerScore = winnerScore;
        this.contestants = new ArrayList<>();
        for (String contestant : contestants.split(",")) {
            this.contestants.add(UUID.fromString(contestant));
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public CompetitionReport(Competition competition, LocalDateTime startTime, LocalDateTime endTime) {
        this(competition.getCompetitionName(), competition.getLeaderboard().getEntries(), startTime, endTime);
    }

    public CompetitionReport(String competitionName, List<CompetitionEntry> finalEntries, LocalDateTime startTime, LocalDateTime endTime) {
        this.runId = UUID.randomUUID();
        this.competitionConfigId = competitionName;
        CompetitionEntry topEntry = finalEntries.isEmpty() ? null : finalEntries.getFirst();
        if (topEntry == null) {
            this.winnerFish = FishRarityKey.empty().toStringDefault();
            this.winnerScore = 0F;
            this.winnerUuid = null;
            this.contestants = Collections.emptyList();
        } else {
            this.winnerFish = FishRarityKey.of(topEntry.getFish()).toString();
            this.winnerScore = topEntry.getValue();
            this.winnerUuid = topEntry.getPlayer();
            this.contestants = finalEntries.stream().map(CompetitionEntry::getPlayer).toList();
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getCompetitionConfigId() {
        return competitionConfigId;
    }

    public UUID getRunId() {
        return runId;
    }

    public String getWinnerFish() {
        return winnerFish;
    }

    public UUID getWinnerUuid() {
        return winnerUuid;
    }

    public List<UUID> getContestants() {
        return contestants;
    }

    public float getWinnerScore() {
        return winnerScore;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Timestamp getStartTimestamp() {
        return Timestamp.valueOf(startTime);
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Timestamp getEndTimestamp() {
        return Timestamp.valueOf(endTime);
    }

}
