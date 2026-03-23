package com.oheers.fish.competition.leaderboard;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

public class Leaderboard implements LeaderboardHandler {

    private final CompetitionType type;
    private final TreeSet<CompetitionEntry> entries;

    public Leaderboard(CompetitionType type) {
        this.type = type;
        this.entries = new TreeSet<>(createComparator());
    }

    @Override
    public List<CompetitionEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    @Override
    public void addEntry(@NotNull UUID player, @NotNull Fish fish) {
        CompetitionEntry entry = new CompetitionEntry(player, fish, type);
        addEntry(entry);
    }

    @Override
    public void addEntry(@NotNull CompetitionEntry entry) {
        CompetitionEntry initialTopEntry = getTopEntry();

        entries.add(entry);

        CompetitionEntry newTopEntry = getTopEntry();

        // Check for a new first place
        if (initialTopEntry == null || newTopEntry == null) {
            return;
        }

        UUID initialPlayer = initialTopEntry.getPlayer();
        UUID newPlayer = newTopEntry.getPlayer();

        if (newPlayer.equals(initialPlayer)) {
            return;
        }

        Fish newFish = newTopEntry.getFish();

        EMFMessage message = ConfigMessage.NEW_FIRST_PLACE_NOTIFICATION.getMessage();
        message.setPerPlayer(false);

        message.setPlayer(Bukkit.getOfflinePlayer(newPlayer));
        message.setLength(Float.toString(newFish.getLength()));
        message.setFishCaught(newFish.getName());
        message.setRarity(newFish.getRarity().getDisplayName());

        if (Competition.isDoingFirstPlaceActionBar()) {
            message.broadcastActionBar();
        } else {
            message.broadcast();
        }
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public boolean contains(CompetitionEntry entry) {
        return entries.contains(entry);
    }

    @Override
    public CompetitionEntry getEntry(UUID player) {
        for (CompetitionEntry entry : entries) {
            if (entry.getPlayer().equals(player)) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public CompetitionEntry getEntry(int place) {
        try {
            return getEntries().get(place - 1);
        } catch (IndexOutOfBoundsException exception) {
            return null;
        }
    }

    @Override
    public int getSize() {
        return entries.size();
    }

    @Override
    public boolean hasEntry(UUID player) {
        return getEntry(player) != null;
    }

    @Override
    public void removeEntry(CompetitionEntry entry) {
        entries.remove(entry);
    }

    @Override
    public CompetitionEntry getTopEntry() {
        return getEntries().isEmpty() ? null : getEntries().getFirst();
    }

    /**
     * Tracks a fish caught by the player in the competition. The value is increased based on the competition type's
     * strategy. If the strategy uses fish length, the value will be increased by the fish's length; otherwise, it will be
     * increased by 1.
     * @param entry The entry to track the fish for.
     * @param fish The fish to track.
     * @return The new competition entry with the updated values.
     */
    @Override
    public CompetitionEntry trackFish(@NotNull CompetitionEntry entry, @NotNull Fish fish) {
        CompetitionEntry newEntry = new CompetitionEntry(entry.getPlayer(), fish, type);
        float value = entry.getValue();
        if (type.getStrategy().shouldUseFishLength()) {
            value += fish.getLength();
        } else {
            value += 1;
        }
        newEntry.setValue(value);

        // Add the new entry and remove the old one.
        // It has to be in this order so the new-first message works.
        addEntry(newEntry);
        removeEntry(entry);

        return newEntry;
    }

    private Comparator<CompetitionEntry> createComparator() {
        Comparator<CompetitionEntry> comparator = Comparator
            // First compare the entry values
            .comparingDouble(CompetitionEntry::getValue)
            // If a tie, compare by the time of the most recent catch
            .thenComparing(Comparator.comparingLong(CompetitionEntry::getTime).reversed());

        return type.shouldReverseLeaderboard() ? comparator : comparator.reversed();
    }

}
