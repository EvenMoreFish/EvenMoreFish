package com.oheers.fish.competition;

import com.oheers.fish.api.fishing.items.IFish;

import java.time.Instant;
import java.util.UUID;

public class CompetitionEntry {

    private final UUID player;
    private final IFish fish;
    protected long time;
    protected float value;
    private final CompetitionType type;

    public CompetitionEntry(UUID player, IFish fish, CompetitionType type) {
        this.player = player;
        this.fish = fish;
        this.time = Instant.now().toEpochMilli();
        this.type = type;

        if (type.getStrategy().shouldUseFishLength()) {
            this.value = fish.getLength();
        } else {
            this.value = 1;
        }
    }

    /**
     * Increases the player's "score" by a set amount. The time that the entry was made will always be set to the current
     * epoch millisecond and will be unaffected by an increaseAmount that is not 1.
     *
     * @param increaseAmount The amount to increase the player's score by.
     */
    public void incrementValue(float increaseAmount) {
        this.value += Math.abs(increaseAmount);
        this.time = Instant.now().toEpochMilli();
    }

    public IFish getFish() {
        return fish;
    }

    public long getTime() {
        return time;
    }

    public int getHash() {
        return this.hashCode();
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public UUID getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "CompetitionEntry[" + this.player + ", " + value + ", " + time + "]";
    }
}
