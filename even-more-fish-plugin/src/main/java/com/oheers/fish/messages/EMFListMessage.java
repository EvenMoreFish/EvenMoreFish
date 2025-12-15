package com.oheers.fish.messages;

import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.message.ComponentListMessage;
import uk.firedev.messagelib.message.ComponentMessage;
import uk.firedev.messagelib.message.ComponentSingleMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EMFListMessage extends EMFMessage {

    private ComponentListMessage underlying;

    private EMFListMessage(@NotNull ComponentListMessage message) {
        super();
        this.underlying = message;
    }

    @Override
    public EMFListMessage createCopy() {
        EMFListMessage newMessage = new EMFListMessage(underlying.createCopy());
        newMessage.perPlayer = this.perPlayer;
        return newMessage;
    }

    @Override
    public @NotNull ComponentListMessage getUnderlying() {
        return underlying;
    }

    @Override
    public void setUnderlying(@NotNull ComponentMessage message) {
        if (message instanceof ComponentSingleMessage singleMessage) {
            this.underlying = singleMessage.toListMessage();
        } else if (message instanceof ComponentListMessage listMessage) {
            this.underlying = listMessage;
        } else {
            // Should never happen.
            return;
        }
    }

    // Factory methods

    public static EMFListMessage empty() {
        return new EMFListMessage(
            ComponentMessage.componentMessage(List.of())
        );
    }

    public static EMFListMessage ofUnderlying(@NotNull ComponentListMessage underlying) {
        return new EMFListMessage(underlying);
    }

    public static EMFListMessage of(@NotNull Component component) {
        return new EMFListMessage(
            ComponentMessage.componentMessage(List.of(component))
        );
    }

    public static EMFListMessage ofList(@NotNull List<Component> components) {
        return new EMFListMessage(
            ComponentMessage.componentMessage(components)
        );
    }

    public static EMFListMessage fromString(@NotNull String string) {
        return new EMFListMessage(
            ComponentMessage.componentMessage(List.of(string))
        );
    }

    public static EMFListMessage fromStringList(@NotNull List<String> strings) {
        return new EMFListMessage(
            ComponentMessage.componentMessage(strings)
        );
    }

    // Class methods

    /**
     * @return The stored components in their original form, with no variables applied.
     */
    public @NotNull List<Component> getRawMessage() {
        return this.underlying.get();
    }

    @Override
    public @NotNull Component getComponentMessage(@Nullable OfflinePlayer player) {
        return Component.join(JoinConfiguration.newlines(), getComponentListMessage(player));
    }

    @Override
    public @NotNull List<Component> getComponentListMessage(@Nullable OfflinePlayer player) {
        OfflinePlayer relevant = relevantPlayer == null ? player : relevantPlayer;
        return underlying.parsePlaceholderAPI(relevant)
            .replace("{player}", Optional.ofNullable(relevant).map(OfflinePlayer::getName).orElse("null"))
            .get();
    }

    @Override
    public @NotNull String getLegacyMessage() {
        return String.join("\n", underlying.getAsLegacy());
    }

    @Override
    public @NotNull List<String> getLegacyListMessage() {
        return underlying.getAsLegacy();
    }

    @Override
    public @NotNull String getPlainTextMessage() {
        return String.join("\n", underlying.getAsPlainText());
    }

    @Override
    public @NotNull List<String> getPlainTextListMessage() {
        return underlying.getAsPlainText();
    }

    @Override
    public void formatPlaceholderAPI() {
        this.underlying = this.underlying.parsePlaceholderAPI(relevantPlayer);
    }

    @Override
    public boolean containsString(@NotNull String string) {
        return underlying.toSingleMessages().stream().anyMatch(singleMessage -> singleMessage.containsString(string));
    }

    public void setVariableWithListInsertion(@NotNull String variable, @NotNull Object replacement) {
        this.underlying = this.underlying.replaceWithListInsertion(variable, replacement);
    }

    public void setVariablesWithListInsertion(@Nullable Map<String, ?> variableMap) {
        if (variableMap == null || variableMap.isEmpty()) {
            return;
        }
        this.underlying = this.underlying.replaceWithListInsertion(variableMap);
    }

    public void appendEachLine(@NotNull Object object) {
        if (isEmpty()) {
            return;
        }
        setUnderlying(
            getUnderlying().appendEachLine(object)
        );
    }

    public void prependEachLine(@NotNull Object object) {
        if (isEmpty()) {
            return;
        }
        setUnderlying(
            getUnderlying().prependEachLine(object)
        );
    }

}
