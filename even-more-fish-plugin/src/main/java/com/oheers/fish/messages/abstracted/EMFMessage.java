package com.oheers.fish.messages.abstracted;

import com.oheers.fish.FishUtils;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.message.ComponentListMessage;
import uk.firedev.messagelib.message.ComponentMessage;
import uk.firedev.messagelib.message.ComponentSingleMessage;
import uk.firedev.messagelib.message.MessageType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class EMFMessage {

    protected boolean perPlayer = true;
    protected OfflinePlayer relevantPlayer = null;

    public static EMFMessage fromUnderlying(@NotNull ComponentMessage message) {
        if (message instanceof ComponentListMessage listMessage) {
            return EMFListMessage.ofUnderlying(listMessage);
        } else if (message instanceof ComponentSingleMessage singleMessage) {
            return EMFSingleMessage.ofUnderlying(singleMessage);
        } else {
            throw new IllegalArgumentException("Unknown ComponentMessage type");
        }
    }

    protected EMFMessage() {}

    public abstract EMFMessage createCopy();

    public abstract @NotNull ComponentMessage getUnderlying();

    public abstract void setUnderlying(@NotNull ComponentMessage message);

    public final void send(@NotNull Audience target) {
        if (target instanceof Player player) {
            OfflinePlayer relevant = relevantPlayer == null ? player : relevantPlayer;
            getUnderlying().replace("{player}", relevant.getName())
                .parsePlaceholderAPI(relevant)
                .send(player);
            return;
        }
        String name = FishUtils.getPlayerName(relevantPlayer);
        getUnderlying().replace("{player}", String.valueOf(name))
            .parsePlaceholderAPI(relevantPlayer)
            .send(target);
    }

    public final void send(@NotNull Collection<? extends Audience> targets) {
        targets.forEach(this::send);
    }

    public final void sendActionBar(@NotNull Audience target) {
        if (target instanceof Player player) {
            OfflinePlayer relevant = relevantPlayer == null ? player : relevantPlayer;
            getUnderlying().messageType(MessageType.ACTION_BAR)
                .replace("{player}", relevant.getName())
                .parsePlaceholderAPI(relevant)
                .send(player);
            return;
        }
        String name = FishUtils.getPlayerName(relevantPlayer);
        getUnderlying().messageType(MessageType.ACTION_BAR)
            .replace("{player}", String.valueOf(name))
            .parsePlaceholderAPI(relevantPlayer)
            .send(target);
    }

    public final void sendActionBar(@NotNull Collection<? extends Audience> targets) {
        targets.forEach(this::sendActionBar);
    }

    public final @NotNull Component getComponentMessage() {
        return getComponentMessage(null);
    }

    public abstract @NotNull Component getComponentMessage(@Nullable OfflinePlayer player);

    public final @NotNull List<Component> getComponentListMessage() {
        return getComponentListMessage(null);
    }

    public abstract @NotNull List<Component> getComponentListMessage(@Nullable OfflinePlayer player);

    public abstract @NotNull String getLegacyMessage();

    public abstract @NotNull List<String> getLegacyListMessage();

    public abstract @NotNull String getPlainTextMessage();

    public abstract @NotNull List<String> getPlainTextListMessage();

    public abstract void formatPlaceholderAPI();

    public final void setPerPlayer(boolean perPlayer) {
        this.perPlayer = perPlayer;
    }

    public final @Nullable OfflinePlayer getRelevantPlayer() {
        return this.relevantPlayer;
    }

    /**
     * Sets the relevant player for this message. If the relevant player exists, per-player placeholders will NOT be parsed.
     * @param relevantPlayer The relevant player for this message
     */
    public final void setRelevantPlayer(@NotNull OfflinePlayer relevantPlayer) {
        this.relevantPlayer = relevantPlayer;
    }

    public final boolean isEmpty() {
        return getUnderlying().isEmpty();
    }

    public abstract boolean containsString(@NotNull String string);

    // Broadcast

    public void broadcast() {
        send(Bukkit.getConsoleSender());
        send(Bukkit.getOnlinePlayers());
    }

    public void broadcastActionBar() {
        sendActionBar(Bukkit.getConsoleSender());
        sendActionBar(Bukkit.getOnlinePlayers());
    }

    // Append

    public final void appendString(@NotNull String string) {
        setUnderlying(
            getUnderlying().append(string)
        );
    }

    public final void appendStringList(@NotNull List<String> strings) {
        strings.forEach(this::appendString);
    }

    public final void appendMessage(@NotNull EMFMessage message) {
        setUnderlying(
            getUnderlying().append(message.getUnderlying())
        );
    }

    public final void appendMessageList(@NotNull List<EMFMessage> messages) {
        messages.forEach(this::appendMessage);
    }

    public final void appendComponent(@NotNull Component component) {
        setUnderlying(
            getUnderlying().append(component)
        );
    }

    public final void appendComponentList(@NotNull List<Component> components) {
        components.forEach(this::appendComponent);
    }

    // Prepend

    public final void prependString(@NotNull String string) {
        setUnderlying(
            getUnderlying().prepend(string)
        );
    }

    public final void prependStringList(@NotNull List<String> strings) {
        strings.forEach(this::prependString);
    }

    public final void prependMessage(@NotNull EMFMessage message) {
        setUnderlying(
            getUnderlying().prepend(message.getUnderlying())
        );
    }

    public final void prependMessageList(@NotNull List<EMFMessage> messages) {
        messages.forEach(this::prependMessage);
    }

    public final void prependComponent(@NotNull Component component) {
        setUnderlying(
            getUnderlying().prepend(component)
        );
    }

    public final void prependComponentList(@NotNull List<Component> components) {
        components.forEach(this::prependComponent);
    }

    // Variables

    /**
     * Formats the provided variable.
     * @param variable The variable.
     * @param replacement The replacement for the variable.
     */
    public final void setVariable(@NotNull final String variable, @NotNull final Object replacement) {
        setUnderlying(
            getUnderlying().replace(variable, replacement)
        );
    }

    /**
     * Formats the provided map of variables.
     * @param variableMap The map of variables and their replacements.
     */
    public final void setVariables(@Nullable Map<String, ?> variableMap) {
        if (variableMap == null || variableMap.isEmpty()) {
            return;
        }
        setUnderlying(
            getUnderlying().replace(variableMap)
        );
    }

    /**
     * The player's name to replace the {player} variable. Also sets the relevantPlayer variable to this player.
     *
     * @param player The player.
     */
    public void setPlayer(@Nullable final OfflinePlayer player) {
        if (player == null) {
            return;
        }
        this.relevantPlayer = player;
        setVariable("{player}", Objects.requireNonNullElse(player.getName(), "N/A"));
    }

    /**
     * The fish's length to replace the {length} variable.
     *
     * @param length The length of the fish.
     */
    public void setLength(@NotNull final Object length) {
        setVariable("{length}", length);
    }

    /**
     * The rarity of the fish to replace the {rarity} variable.
     *
     * @param rarity The fish's rarity.
     */
    public void setRarity(@NotNull final Object rarity) {
        setVariable("{rarity}", rarity);
        setVariable("{rarity_colour}", "");
    }

    /**
     * Sets the fish name to replace the {fish} variable.
     *
     * @param fish The fish's name.
     */
    public void setFishCaught(@NotNull final Object fish) {
        setVariable("{fish}", fish);
    }

    /**
     * The price after a fish has been sold in /emf shop to replace the {sell-price} variable.
     *
     * @param sellPrice The sell price of the fish.
     */
    public void setSellPrice(@NotNull final Object sellPrice) {
        setVariable("{sell-price}", sellPrice);
    }

    /**
     * The amount of whatever, used multiple times throughout the plugin to replace the {amount} variable.
     *
     * @param amount The amount of x.
     */
    public void setAmount(@NotNull final Object amount) {
        setVariable("{amount}", amount);
    }

    /**
     * Sets the position for the {position} variable in the /emf top leaderboard.
     *
     * @param position The position.
     */
    public void setPosition(@NotNull final Object position) {
        setVariable("{position}", position);
    }

    /**
     * Sets the formatted (Nh, Nm, Ns) time to replace the {time_formatted} variable.
     *
     * @param timeFormatted The formatted time.
     */
    public void setTimeFormatted(@NotNull final Object timeFormatted) {
        setVariable("{time_formatted}", timeFormatted);
    }

    /**
     * Sets the raw time (Nh:Nm:Ns) time to replace the {time_raw} variable.
     *
     * @param timeRaw The raw time.
     */
    public void setTimeRaw(@NotNull final Object timeRaw) {
        setVariable("{time_raw}", timeRaw);
    }

    /**
     * Sets the bait in the message to replace the {bait} variable.
     *
     * @param bait The name of the bait.
     */
    public void setBait(@NotNull final Object bait) {
        setVariable("{bait_theme}", "");
        setVariable("{bait}", bait);
    }

    /**
     * Defines how many days should replace the {days} variable.
     *
     * @param days The number of days.
     */
    public void setDays(@NotNull final Object days) {
        setVariable("{days}", days);
    }

    /**
     * Defines how many hours should replace the {hours} variable.
     *
     * @param hours The number of hours.
     */
    public void setHours(@NotNull final Object hours) {
        setVariable("{hours}", hours);
    }

    /**
     * Defines how many minutes should replace the {minutes} variable.
     *
     * @param minutes The number of minutes.
     */
    public void setMinutes(@NotNull final Object minutes) {
        setVariable("{minutes}", minutes);
    }

    /**
     * Defines the result for the toggle MSG to replace the {toggle_msg} variable.
     *
     * @param toggleMSG The applicable toggle msg.
     */
    public void setToggleMSG(@NotNull final Object toggleMSG) {
        setVariable("{toggle_msg}", toggleMSG);
    }

    /**
     * Defines the result for the toggle material to replace the {toggle_icon} variable.
     *
     * @param toggleIcon The applicable toggle material.
     */
    public void setToggleIcon(@NotNull final Object toggleIcon) {
        setVariable("{toggle_icon}", toggleIcon);
    }

    /**
     * Defines which day should replace the {day} variable.
     *
     * @param day The day number.
     */
    public void setDay(@NotNull final Object day) {
        setVariable("{day}", day);
    }

    /**
     * Defines the name of the fish to be used, alternate to {fish}.
     *
     * @param name The name of the fish or user
     */
    public void setName(@NotNull final Object name) {
        setVariable("{name}", name);
    }

    /**
     * Defines the number of fish caught in the user's fish reports.
     *
     * @param numCaught The number of fish caught.
     */
    public void setNumCaught(@NotNull final Object numCaught) {
        setVariable("{num_caught}", numCaught);
    }

    /**
     * Defines the largest fish caught by the user in their fish reports.
     *
     * @param largestSize The largest size of the fish.
     */
    public void setLargestSize(@NotNull final Object largestSize) {
        setVariable("{largest_size}", largestSize);
    }

    /**
     * The first fish to be caught by the user in their fish reports.
     *
     * @param firstCaught The first fish caught.
     */
    public void setFirstCaught(@NotNull final Object firstCaught) {
        setVariable("{first_caught}", firstCaught);
    }

    /**
     * The time remaining for the fish to be unlocked.
     *
     * @param timeRemaining The time remaining.
     */
    public void setTimeRemaining(@NotNull final Object timeRemaining) {
        setVariable("{time_remaining}", timeRemaining);
    }

    /**
     * Sets the competition type, checking against the values for each type stored in messages.yml to replace the {type}
     * variable.
     *
     * @param typeString The competition type.
     */
    public void setCompetitionType(@NotNull final Object typeString) {
        setVariable("{type}", typeString);
    }

    /**
     * The amount of baits currently applied to the item.
     *
     * @param currentBaits The amount of baits.
     */
    public void setCurrentBaits(@NotNull final Object currentBaits) {
        setVariable("{current_baits}", currentBaits);
    }

    /**
     * The max amount of baits that can be applied to the item.
     *
     * @param maxBaits The max amount of baits.
     */
    public void setMaxBaits(@NotNull final Object maxBaits) {
        setVariable("{max_baits}", maxBaits);
    }

    // Subclass things

    public EMFSingleMessage toSingleMessage() {
        EMFSingleMessage message = EMFSingleMessage.ofUnderlying(
            getUnderlying().toSingleMessage()
        );
        message.perPlayer = this.perPlayer;
        message.relevantPlayer = this.relevantPlayer;
        return message;
    }

    public EMFListMessage toListMessage() {
        EMFListMessage message = EMFListMessage.ofUnderlying(
            getUnderlying().toListMessage()
        );
        message.perPlayer = this.perPlayer;
        message.relevantPlayer = this.relevantPlayer;
        return message;
    }

}
