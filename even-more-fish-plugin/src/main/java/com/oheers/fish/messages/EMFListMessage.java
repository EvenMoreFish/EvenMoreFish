package com.oheers.fish.messages;

import com.oheers.fish.FishUtils;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EMFListMessage extends EMFMessage {

    private ArrayList<Component> message = new ArrayList<>();

    private EMFListMessage(@Nullable List<Component> message) {
        super();
        if (message != null) {
            message.forEach(line -> {
                if (line == null) {
                    return;
                }
                this.message.add(EMPTY.append(line));
            });
        }
    }

    @Override
    public EMFListMessage createCopy() {
        return toListMessage();
    }

    // Factory methods

    public static EMFListMessage empty() {
        return new EMFListMessage(null);
    }

    public static EMFListMessage of(@NotNull Component component) {
        return new EMFListMessage(List.of(component));
    }

    public static EMFListMessage ofList(@NotNull List<Component> components) {
        if (components.isEmpty()) {
            return empty();
        }
        return new EMFListMessage(components);
    }

    public static EMFListMessage fromString(@NotNull String string) {
        return of(formatString(string));
    }

    public static EMFListMessage fromStringList(@NotNull List<String> strings) {
        if (strings.isEmpty()) {
            return empty();
        }
        return ofList(strings.stream().map(EMFListMessage::formatString).toList());
    }

    // Class methods

    @Override
    public void send(@NotNull Audience target) {
        target.sendMessage(getComponentMessage());
    }

    @Override
    public void sendActionBar(@NotNull Audience target) {
        target.sendActionBar(getComponentMessage());
    }

    /**
     * @return The stored components in their original form, with no variables applied.
     */
    public @NotNull List<Component> getRawMessage() {
        return this.message;
    }

    @Override
    public @NotNull Component getComponentMessage() {
        return Component.join(JoinConfiguration.newlines(), getComponentListMessage());
    }

    @Override
    public @NotNull List<Component> getComponentListMessage() {
        formatPlaceholderAPI();
        return this.message.stream().map(EMFMessage::removeDefaultItalics).toList();
    }

    @Override
    public void formatPlaceholderAPI() {
        this.message = this.message.stream()
            .map(line -> FishUtils.parsePlaceholderAPI(line, relevantPlayer))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean isEmpty() {
        return message.isEmpty();
    }

    @Override
    public boolean containsString(@NotNull String string) {
        return this.message.stream().anyMatch(line -> FishUtils.componentContainsString(line, string));
    }

    @Override
    public void appendString(@NotNull String string) {
        this.message.add(formatString(string));
    }

    @Override
    public void appendMessage(@NotNull EMFMessage message) {
        this.message.addAll(message.getComponentListMessage());
    }

    @Override
    public void appendComponent(@NotNull Component component) {
        this.message.add(component);
    }

    @Override
    public void prependString(@NotNull String string) {
        this.message.add(0, formatString(string));
    }

    @Override
    public void prependMessage(@NotNull EMFMessage message) {
        this.message.addAll(0, message.getComponentListMessage());
    }

    @Override
    public void prependComponent(@NotNull Component component) {
        this.message.add(0, component);
    }

    @Override
    public void decorateIfAbsent(@NotNull TextDecoration decoration, TextDecoration.@NotNull State state) {
        this.message = this.message.stream()
            .map(line -> FishUtils.decorateIfAbsent(line, decoration, state))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void colorIfAbsent(@NotNull TextColor color) {
        this.message = this.message.stream()
            .map(line -> line.colorIfAbsent(color))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Formats an EMFMessage replacement.
     */
    @Override
    protected void setEMFMessageVariable(@NotNull String variable, @NotNull EMFMessage replacement) {
        if (replacement instanceof EMFSingleMessage singleMessage) {
            setComponentVariable(variable, singleMessage.getComponentMessage());
        } else if (replacement instanceof EMFListMessage listMessage) {
            formatListVariable(variable, listMessage);
        }
    }

    /**
     * Formats a Component replacement.
     */
    @Override
    protected void setComponentVariable(@NotNull String variable, @NotNull Component replacement) {
        TextReplacementConfig trc = TextReplacementConfig.builder()
            .matchLiteral(variable)
            .replacement(replacement)
            .build();
        this.message = this.message.stream()
            .map(line -> line.replaceText(trc))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private void formatListVariable(@NotNull String variable, @NotNull EMFListMessage replacement) {
        this.message = this.message.stream()
            .flatMap(line -> {
                // If the variable is present in the line, replace it
                if (FishUtils.componentContainsString(line, variable)) {
                    // If the replacement is empty, return an empty stream to remove the line
                    if (replacement.isEmpty()) {
                        return Stream.empty();
                    }
                    return replacement.getComponentListMessage().stream();
                // If not, return the original line
                } else {
                    return Stream.of(line);
                }
            })
            // Ensure it's returned as an ArrayList
            .collect(Collectors.toCollection(ArrayList::new));
    }

}
