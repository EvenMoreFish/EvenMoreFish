package com.oheers.fish.messages.abstracted;

import com.oheers.fish.FishUtils;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class EMFMessage {

    public static final MiniMessage MINIMESSAGE = MiniMessage.builder()
        .postProcessor(component -> component)
        .build();
    public static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    public static final LegacyComponentSerializer LEGACY_SERIALIZER_INPUT = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();
    public static final PlainTextComponentSerializer PLAINTEXT_SERIALIZER = PlainTextComponentSerializer.plainText();
    public static final Component EMPTY = Component.empty();

    protected boolean perPlayer = true;
    protected boolean canSilent = false;
    protected OfflinePlayer relevantPlayer = null;

    protected EMFMessage() {}

    public abstract EMFMessage createCopy();

    public static Component formatString(@NotNull String message) {
        if (FishUtils.isLegacyString(message)) {
            return LEGACY_SERIALIZER_INPUT.deserialize(message);
        } else {
            return MINIMESSAGE.deserialize(
                message.replace('§', '&')
            );
        }
    }

    public static @NotNull Component removeDefaultItalics(@NotNull Component component) {
        return FishUtils.decorateIfAbsent(component, TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public abstract void send(@NotNull Audience target);

    public void send(@NotNull Collection<? extends Audience> targets) {
        targets.forEach(this::send);
    }

    public abstract void sendActionBar(@NotNull Audience target);

    public void sendActionBar(@NotNull Collection<? extends Audience> targets) {
        targets.forEach(this::sendActionBar);
    }

    public abstract @NotNull Component getComponentMessage();

    public abstract @NotNull List<Component> getComponentListMessage();

    public @NotNull String getLegacyMessage() {
        return LEGACY_SERIALIZER.serialize(getComponentMessage());
    }

    public @NotNull List<String> getLegacyListMessage() {
        return getComponentListMessage().stream().map(LEGACY_SERIALIZER::serialize).toList();
    }

    public @NotNull String getPlainTextMessage() {
        return PLAINTEXT_SERIALIZER.serialize(getComponentMessage());
    }

    public @NotNull List<String> getPlainTextListMessage() {
        return getComponentListMessage().stream().map(PLAINTEXT_SERIALIZER::serialize).toList();
    }

    public abstract void formatPlaceholderAPI();

    public void setPerPlayer(boolean perPlayer) {
        this.perPlayer = perPlayer;
    }

    public void setCanSilent(boolean canSilent) {
        this.canSilent = canSilent;
    }

    public boolean isCanSilent() {
        return this.canSilent;
    }

    protected boolean silentCheck(@NotNull Component component) {
        return isCanSilent() && PLAINTEXT_SERIALIZER.serialize(component).endsWith(" -s");
    }

    public @Nullable OfflinePlayer getRelevantPlayer() {
        return this.relevantPlayer;
    }

    public abstract boolean isEmpty();

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

    public abstract void appendString(@NotNull String string);

    public void appendStringList(@NotNull List<String> strings) {
        strings.forEach(this::appendString);
    }

    public abstract void appendMessage(@NotNull EMFMessage message);

    public void appendMessageList(@NotNull List<EMFMessage> messages) {
        messages.forEach(this::appendMessage);
    }

    public abstract void appendComponent(@NotNull Component component);

    public void appendComponentList(@NotNull List<Component> components) {
        components.forEach(this::appendComponent);
    }

    // Prepend

    public abstract void prependString(@NotNull String string);

    public void prependStringList(@NotNull List<String> strings) {
        strings.forEach(this::prependString);
    }

    public abstract void prependMessage(@NotNull EMFMessage message);

    public void prependMessageList(@NotNull List<EMFMessage> messages) {
        messages.forEach(this::prependMessage);
    }

    public abstract void prependComponent(@NotNull Component component);

    public void prependComponentList(@NotNull List<Component> components) {
        components.forEach(this::prependComponent);
    }

    public abstract void decorateIfAbsent(@NotNull TextDecoration decoration, @NotNull TextDecoration.State state);

    public abstract void colorIfAbsent(@NotNull TextColor color);

    // Variables

    /**
     * Formats the provided variable.
     * @param variable The variable.
     * @param replacement The replacement for the variable.
     */
    public void setVariable(@NotNull final String variable, @NotNull final Object replacement) {
        if (replacement instanceof EMFMessage emfMessage) {
            setEMFMessageVariable(variable, emfMessage);
        } else if (replacement instanceof Component component) {
            setComponentVariable(variable, component);
        } else {
            setComponentVariable(variable, formatString(String.valueOf(replacement)));
        }
    }

    /**
     * Formats the provided map of variables.
     * @param variableMap The map of variables and their replacements.
     */
    public void setVariables(@Nullable Map<String, ?> variableMap) {
        if (variableMap == null || variableMap.isEmpty()) {
            return;
        }
        variableMap.forEach(this::setVariable);
    }

    protected abstract void setEMFMessageVariable(@NotNull final String variable, @NotNull final EMFMessage replacement);

    protected abstract void setComponentVariable(@NotNull final String variable, @NotNull final Component replacement);

    /**
     * The player's name to replace the {player} variable. Also sets the relevantPlayer variable to this player.
     *
     * @param player The player.
     */
    public void setPlayer(@NotNull final OfflinePlayer player) {
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
        EMFSingleMessage message = EMFSingleMessage.of(getComponentMessage());
        message.perPlayer = this.perPlayer;
        message.canSilent = this.canSilent;
        message.relevantPlayer = this.relevantPlayer;
        return message;
    }

    public EMFListMessage toListMessage() {
        EMFListMessage message = EMFListMessage.ofList(getComponentListMessage());
        message.perPlayer = this.perPlayer;
        message.canSilent = this.canSilent;
        message.relevantPlayer = this.relevantPlayer;
        return message;
    }

}
