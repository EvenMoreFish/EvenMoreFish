package com.oheers.fish.messages;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;

public class EMFMessage {

    public static final MiniMessage MINIMESSAGE = MiniMessage.builder()
        .postProcessor(component -> component)
        .build();
    public static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private final Map<String, String> liveVariables = new LinkedHashMap<>();

    private String message;
    private boolean canSilent = false;
    private OfflinePlayer relevantPlayer;
    protected boolean perPlayer = true;

    private EMFMessage(@NotNull String message) {
        this.message = message;
    }

    public static EMFMessage empty() {
        return new EMFMessage("");
    }

    public static EMFMessage of(@NotNull Component component) {
        return new EMFMessage(MINIMESSAGE.serialize(component));
    }

    public static EMFMessage ofList(@NotNull List<Component> components) {
        List<String> strings = components.stream().map(MINIMESSAGE::serialize).toList();
        return new EMFMessage(String.join("\n<reset>", strings));
    }

    public static EMFMessage fromString(@NotNull String string) {
        return new EMFMessage(formatColours(string));
    }

    public static EMFMessage fromStringList(@NotNull List<String> strings) {
        strings = strings.stream().map(EMFMessage::formatColours).toList();
        return new EMFMessage(String.join("\n<reset>", strings));
    }

    /**
     * Replaces all section symbols with ampersands so MiniMessage doesn't explode
     */
    private static String formatColours(@NotNull String message) {
        return message.replace('§', '&');
    }

    public void send(@NotNull CommandSender target) {
        if (getRawMessage().isEmpty() || silentCheck()) {
            return;
        }

        String originalMessage = getRawMessage();

        if (perPlayer && target instanceof Player player) {
            setPlayer(player);
        }

        target.sendMessage(getComponentMessage());

        setMessage(originalMessage);
    }

    public void sendActionBar(@NotNull CommandSender target) {
        if (getRawMessage().isEmpty() || silentCheck()) {
            return;
        }

        String originalMessage = getRawMessage();

        if (perPlayer && target instanceof Player player) {
            setPlayer(player);
        }

        target.sendActionBar(getComponentMessage());

        setMessage(originalMessage);
    }

    /**
     * @return The stored String in its raw form, with no colors or variables applied.
     */
    public @NotNull String getRawMessage() {
        return this.message;
    }

    /**
     * @return The stored String in its raw list form, with no colors or variables applied.
     */
    public @NotNull List<String> getRawListMessage() {
        return Arrays.asList(this.message.split("\n"));
    }

    public @NotNull Component getComponentMessage() {
        formatVariables();
        formatPlaceholderAPI();
        Component component = MINIMESSAGE.deserialize(getRawMessage());
        return removeDefaultItalics(component);
    }

    public @NotNull List<Component> getComponentListMessage() {
        formatVariables();
        formatPlaceholderAPI();
        return getRawListMessage().stream()
            .map(raw -> {
                Component component = MINIMESSAGE.deserialize(raw);
                return removeDefaultItalics(component);
            })
            .toList();
    }

    private @NotNull Component removeDefaultItalics(@NotNull Component component) {
        TextDecoration decoration = TextDecoration.ITALIC;
        TextDecoration.State oldState = component.decoration(decoration);
        if (oldState == TextDecoration.State.NOT_SET) {
            return component.decoration(decoration, TextDecoration.State.FALSE);
        }
        return component;
    }

    public @NotNull String getLegacyMessage() {
        return LEGACY_SERIALIZER.serialize(getComponentMessage());
    }

    public @NotNull List<String> getLegacyListMessage() {
        return Arrays.asList(getLegacyMessage().split("\n"));
    }

    public String getPlainTextMessage() {
        return PlainTextComponentSerializer.plainText().serialize(getComponentMessage());
    }

    /**
     * @return The formatted message as a plain text string list. All formatting will be removed.
     */
    public @NotNull List<String> getPlainTextListMessage() {
        return Arrays.asList(getPlainTextMessage().split("\n"));
    }

    public void formatPlaceholderAPI() {
        if (!isPAPIEnabled()) {
            return;
        }
        String message = getRawMessage();
        Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(message);
        while (matcher.find()) {
            // Find matched String
            String matched = matcher.group();
            // Convert to Legacy Component and into a MiniMessage String
            String parsed = MINIMESSAGE.serialize(
                LEGACY_SERIALIZER.deserialize(
                    PlaceholderAPI.setPlaceholders(getRelevantPlayer(), matched)
                )
            );
            // Escape matched String so we don't have issues
            String safeMatched = Matcher.quoteReplacement(matched);
            // Replace all instances of the matched String with the parsed placeholder.
            message = message.replaceAll(safeMatched, parsed);
        }
        setMessage(message);
    }

    public void setMessage(@NotNull String message) {
        this.message = formatColours(message);
    }

    public void setMessage(@NotNull EMFMessage message) {
        this.message = message.message;
    }

    private boolean isPAPIEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * Formats all variables in {@link #liveVariables}
     */
    public void formatVariables() {
        for (Map.Entry<String, String> entry : liveVariables.entrySet()) {
            String variable = entry.getKey();
            String replacement = formatColours(entry.getValue());
            this.message = this.message.replace(variable, replacement);
        }
    }

    public void setPerPlayer(boolean perPlayer) {
        this.perPlayer = perPlayer;
    }

    /**
     * Sends this message to the entire server.
     */
    public void broadcast() {
        send(Bukkit.getConsoleSender());
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    /**
     * Sends this message to the provided list of targets.
     * @param targets The targets of this message.
     */
    public void send(@NotNull List<CommandSender> targets) {
        targets.forEach(this::send);
    }

    /**
     * Sends this message to the entire server as an action bar.
     */
    public void broadcastActionBar() {
        Bukkit.getOnlinePlayers().forEach(this::sendActionBar);
    }

    /**
     * Sends this message to the provided list of targets as an action bar.
     * @param targets The targets of this message.
     */
    public void sendActionBar(@NotNull List<CommandSender> targets) {
        targets.forEach(this::sendActionBar);
    }

    /**
     * Adds the provided string to the end of this message.
     * @param message The string to append
     */
    public void appendString(@NotNull String message) {
        this.message = this.message + formatColours(message);
    }

    /**
     * Adds the provided component to the end of this message.
     * @param message The component to append
     */
    public void appendComponent(@NotNull Component message) {
        this.message = this.message + MINIMESSAGE.serialize(message);
    }

    /**
     * Adds the provided message to the end of this message.
     * @param message The message to append
     */
    public void appendMessage(@NotNull EMFMessage message) {
        message.formatVariables();
        appendString(message.getRawMessage());
    }

    /**
     * Adds the provided strings to the end of this message.
     * @param messages The strings to append
     */
    public void appendStringList(@NotNull List<String> messages) {
        this.message = this.message + String.join("\n", messages.stream().map(EMFMessage::formatColours).toList());
    }

    /**
     * Adds the provided components to the end of this message.
     * @param messages The strings to append
     */
    public void appendComponentList(@NotNull List<Component> messages) {
        this.message = this.message + String.join("\n", messages.stream().map(MINIMESSAGE::serialize).toList());
    }

    /**
     * Adds the provided messages to the end of this message.
     * @param messages The messages to append
     */
    public void appendMessageList(@NotNull List<EMFMessage> messages) {
        StringBuilder newMessage = new StringBuilder(this.message);
        for (EMFMessage message : messages) {
            message.formatVariables();
            newMessage.append(message.getRawMessage());
        }
        this.message = newMessage.toString();
    }

    /**
     * Adds the provided string to the start of this message.
     * @param message The string to prepend
     */
    public void prependString(@NotNull String message) {
        this.message = formatColours(message) + this.message;
    }

    /**
     * Adds the provided component to the start of this message.
     * @param message The component to prepend
     */
    public void prependComponent(@NotNull Component message) {
        this.message = MINIMESSAGE.serialize(message) + this.message;
    }

    /**
     * Adds the provided message to the start of this message.
     * @param message The message to prepend
     */
    public void prependMessage(@NotNull EMFMessage message) {
        message.formatVariables();
        prependString(message.getRawMessage());
    }

    /**
     * Adds the provided strings to the start of this message.
     * @param messages The strings to prepend
     */
    public void prependStringList(@NotNull List<String> messages) {
        this.message = String.join("\n", messages.stream().map(EMFMessage::formatColours).toList()) + this.message;
    }

    /**
     * Adds the provided components to the start of this message.
     * @param messages The components to prepend
     */
    public void prependComponentList(@NotNull List<Component> messages) {
        this.message = String.join("\n", messages.stream().map(MINIMESSAGE::serialize).toList()) + this.message;
    }

    /**
     * Adds the provided messages to the start of this message.
     * @param messages The messages to prepend
     */
    public void prependMessageList(@NotNull List<EMFMessage> messages) {
        StringBuilder newMessage = new StringBuilder();
        for (EMFMessage message : messages) {
            message.formatVariables();
            newMessage.append(message.getRawMessage());
        }
        this.message = newMessage + this.message;
    }

    /**
     * @param canSilent Should this message support -s?
     */
    public void setCanSilent(boolean canSilent) {
        this.canSilent = canSilent;
    }

    /**
     * @return Does this message support -s?
     */
    public boolean isCanSilent() {
        return this.canSilent;
    }

    /**
     * @return Should this message be silent?
     */
    public boolean silentCheck() {
        return canSilent && this.message.endsWith(" -s");
    }

    /**
     * Sets the relevant player for this message.
     * @param player The relevant player.
     */
    public void setRelevantPlayer(@Nullable OfflinePlayer player) {
        this.relevantPlayer = player;
    }

    /**
     * @return This message's relevant player, or null if not available.
     */
    public @Nullable OfflinePlayer getRelevantPlayer() {
        return this.relevantPlayer;
    }

    /**
     * Adds a variable to be formatted when {@link #formatVariables()} is called.
     * @param variable The variable.
     * @param replacement The replacement for the variable.
     */
    public void setVariable(@NotNull final String variable, @NotNull final Object replacement) {
        if (replacement instanceof EMFMessage emfMessage) {
            emfMessage.formatVariables();
            this.liveVariables.put(variable, emfMessage.getRawMessage());
        } else {
            this.liveVariables.put(variable, String.valueOf(replacement));
        }
    }

    /**
     * Adds a map of variables to be formatted when {@link #formatVariables()} is called.
     * @param variableMap The map of variables and their replacements.
     */
    public void setVariables(@Nullable Map<String, ?> variableMap) {
        if (variableMap == null || variableMap.isEmpty()) {
            return;
        }
        variableMap.forEach((variable, obj) -> {
            if (obj instanceof EMFMessage emfMessage) {
                setVariable(variable, emfMessage);
            } else {
                setVariable(variable, obj.toString());
            }
        });
    }

    /**
     * The colour used by the fish's rarity to apply a clean format for the fish, to replace the {rarity_colour} variable.
     *
     * @param colour The &_ code for the colour.
     */
    public void setRarityColour(@NotNull final Object colour) {
        setVariable("{rarity_colour}", colour);
    }

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
     * Sets the colour of the position for the {pos_colour} variable in the /emf top leaderboard.
     *
     * @param positionColour The position.
     */
    public void setPositionColour(@NotNull final Object positionColour) {
        setVariable("{pos_colour}", positionColour);
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
        setVariable("{bait}", bait);
    }

    /**
     * Defines the theme of the bait to be used throughout the message to replace the {bait_theme} variable.
     *
     * @param baitTheme The bait colour theme.
     */
    public void setBaitTheme(@NotNull final Object baitTheme) {
        setVariable("{bait_theme}", baitTheme);
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

}