package com.oheers.fish.messages;

import com.oheers.fish.FishUtils;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EMFSingleMessage extends EMFMessage {

    private Component message;

    private EMFSingleMessage(@Nullable Component message) {
        super();
        if (message == null) {
            this.message = EMPTY;
        } else {
            this.message = EMPTY.append(message);
        }
    }

    @Override
    public EMFSingleMessage createCopy() {
        return toSingleMessage();
    }

    // Factory methods

    public static EMFSingleMessage empty() {
        return new EMFSingleMessage(null);
    }

    public static EMFSingleMessage of(@NotNull Component component) {
        return new EMFSingleMessage(component);
    }

    public static EMFSingleMessage ofList(@NotNull List<Component> components) {
        Component finalComponent = Component.join(JoinConfiguration.newlines(), components);
        return new EMFSingleMessage(finalComponent);
    }

    public static EMFSingleMessage fromString(@NotNull String string) {
        return of(formatString(string));
    }

    public static EMFSingleMessage fromStringList(@NotNull List<String> strings) {
        return ofList(strings.stream().map(EMFSingleMessage::formatString).toList());
    }

    // Class methods

    public void send(@NotNull Audience audience) {
        if (isEmpty() || silentCheck(this.message)) {
            return;
        }

        EMFSingleMessage copy = createCopy();

        if (perPlayer && audience instanceof Player player) {
            copy.setPlayer(player);
        }

        audience.sendMessage(copy.getComponentMessage());
    }

    public void sendActionBar(@NotNull Audience audience) {
        if (isEmpty() || silentCheck(this.message)) {
            return;
        }

        EMFSingleMessage copy = createCopy();

        if (perPlayer && audience instanceof Player player) {
            copy.setPlayer(player);
        }

        audience.sendActionBar(copy.getComponentMessage());
    }

    /**
     * @return The stored component in its original form, with no variables applied.
     */
    public @NotNull Component getRawMessage() {
        return this.message;
    }

    /**
     * @return The stored Component with all variables applied..
     */
    @Override
    public @NotNull Component getComponentMessage() {
        formatPlaceholderAPI();
        return removeDefaultItalics(this.message);
    }

    @Override
    public @NotNull List<Component> getComponentListMessage() {
        return List.of(getComponentMessage());
    }

    @Override
    public void formatPlaceholderAPI() {
        this.message = FishUtils.parsePlaceholderAPI(this.message, relevantPlayer);
    }

    public void setMessage(@NotNull String message) {
        this.message = formatString(message);
    }

    public void setMessage(@NotNull Component message) {
        this.message = message;
    }

    public void setMessage(@NotNull EMFSingleMessage message) {
        this.message = message.message;
    }

    @Override
    public boolean isEmpty() {
        return PLAINTEXT_SERIALIZER.serialize(this.message).isEmpty();
    }

    @Override
    public boolean containsString(@NotNull String string) {
        return FishUtils.componentContainsString(this.message, string);
    }

    @Override
    public void appendString(@NotNull String string) {
        if (isEmpty()) {
            return;
        }
        this.message = this.message.append(formatString(string));
    }

    @Override
    public void appendMessage(@NotNull EMFMessage message) {
        if (isEmpty()) {
            return;
        }
        this.message = this.message.append(message.getComponentMessage());
    }

    @Override
    public void appendComponent(@NotNull Component component) {
        if (isEmpty()) {
            return;
        }
        this.message = this.message.append(component);
    }

    @Override
    public void prependString(@NotNull String string) {
        if (isEmpty()) {
            return;
        }
        // Ensure the base component is always empty
        this.message = EMPTY.append(formatString(string)).append(this.message);
    }

    @Override
    public void prependMessage(@NotNull EMFMessage message) {
        if (isEmpty()) {
            return;
        }
        // An EMFMessage base component is always empty
        this.message = message.getComponentMessage().append(this.message);
    }

    @Override
    public void prependComponent(@NotNull Component component) {
        if (isEmpty()) {
            return;
        }
        // Ensure the base component is always empty
        this.message = EMPTY.append(component).append(this.message);
    }

    @Override
    public void decorateIfAbsent(@NotNull TextDecoration decoration, TextDecoration.@NotNull State state) {
        this.message = FishUtils.decorateIfAbsent(this.message, decoration, state);
    }

    @Override
    public void colorIfAbsent(@NotNull TextColor color) {
        this.message = this.message.colorIfAbsent(color);
    }

    @Override
    protected void setEMFMessageVariable(@NotNull String variable, @NotNull EMFMessage replacement) {
        setComponentVariable(variable, replacement.getComponentMessage());
    }

    @Override
    protected void setComponentVariable(@NotNull String variable, @NotNull Component replacement) {
        TextReplacementConfig trc = TextReplacementConfig.builder()
            .matchLiteral(variable)
            .replacement(replacement)
            .build();
        this.message = this.message.replaceText(trc);
    }

}