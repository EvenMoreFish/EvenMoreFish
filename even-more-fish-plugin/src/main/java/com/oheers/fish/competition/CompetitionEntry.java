package com.oheers.fish.competition;

import com.oheers.fish.api.fishing.items.IFish;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.UUID;

public class CompetitionEntry {

    private final UUID player;
    private final IFish fish;
    protected long time;
    protected float value = 0.0F;
    private final CompetitionType type;

    public CompetitionEntry(UUID player, IFish fish, CompetitionType type) {
        this.player = player;
        this.fish = fish;
        this.time = Instant.now().toEpochMilli();
        this.type = type;
        trackFish(fish);
    }

    /**
     * Creates a copy of an existing {@link CompetitionEntry} with an updated timestamp.
     */
    public CompetitionEntry(@NonNull CompetitionEntry entry) {
        this.player = entry.player;
        this.fish = entry.fish;
        this.time = Instant.now().toEpochMilli();
        this.value = entry.value;
        this.type = entry.type;
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

    public void trackFish(@NonNull IFish fish) {
        if (type.useFishLength()) {
            if (!fish.isLengthless()) {
                value += fish.getLength();
            }
        } else {
            value += 1;
        }
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
