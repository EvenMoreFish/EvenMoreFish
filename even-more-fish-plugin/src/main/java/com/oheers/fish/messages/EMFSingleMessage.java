package com.oheers.fish.messages;

import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.message.ComponentListMessage;
import uk.firedev.messagelib.message.ComponentMessage;
import uk.firedev.messagelib.message.ComponentSingleMessage;

import java.util.List;
import java.util.Optional;

public class EMFSingleMessage extends EMFMessage {

    private ComponentSingleMessage underlying;

    protected EMFSingleMessage(@NotNull ComponentSingleMessage message) {
        super();
        this.underlying = message;
    }

    @Override
    public EMFSingleMessage createCopy() {
        EMFSingleMessage newMessage = new EMFSingleMessage(underlying.createCopy());
        newMessage.perPlayer = this.perPlayer;
        return newMessage;
    }

    @Override
    public @NotNull ComponentSingleMessage getUnderlying() {
        return underlying;
    }

    @Override
    public void setUnderlying(@NotNull ComponentMessage message) {
        if (message instanceof ComponentListMessage listMessage) {
            this.underlying = listMessage.toSingleMessage();
        } else if (message instanceof ComponentSingleMessage singleMessage) {
            this.underlying = singleMessage;
        } else {
            // Should never happen.
            return;
        }
    }

    // Factory methods

    public static EMFSingleMessage empty() {
        return new EMFSingleMessage(
            ComponentMessage.componentMessage(Component.empty())
        );
    }

    public static EMFSingleMessage ofUnderlying(@NotNull ComponentSingleMessage underlying) {
        return new EMFSingleMessage(underlying);
    }

    public static EMFSingleMessage of(@NotNull Component component) {
        return new EMFSingleMessage(
            ComponentMessage.componentMessage(component)
        );
    }

    public static EMFSingleMessage ofList(@NotNull List<Component> components) {
        return new EMFSingleMessage(
            ComponentMessage.componentMessage(components).toSingleMessage()
        );
    }

    public static EMFSingleMessage fromString(@NotNull String string) {
        return new EMFSingleMessage(
            ComponentMessage.componentMessage(string)
        );
    }

    public static EMFSingleMessage fromStringList(@NotNull List<String> strings) {
        return new EMFSingleMessage(
            ComponentMessage.componentMessage(strings).toSingleMessage()
        );
    }

    // Class methods

    /**
     * @return The stored component.
     */
    public @NotNull Component getRawMessage() {
        return underlying.get();
    }

    @Override
    public @NotNull Component getComponentMessage(@Nullable OfflinePlayer player) {
        OfflinePlayer relevant = relevantPlayer == null ? player : relevantPlayer;
        return underlying.parsePlaceholderAPI(relevant)
            .replace("{player}", Optional.ofNullable(relevant).map(OfflinePlayer::getName).orElse("null"))
            .get();
    }

    @Override
    public @NotNull List<Component> getComponentListMessage(@Nullable OfflinePlayer player) {
        return List.of(getComponentMessage(player));
    }

    @Override
    public @NotNull String getLegacyMessage() {
        return underlying.getAsLegacy();
    }

    @Override
    public @NotNull List<String> getLegacyListMessage() {
        return List.of(underlying.getAsLegacy());
    }

    @Override
    public @NotNull String getPlainTextMessage() {
        return underlying.getAsPlainText();
    }

    @Override
    public @NotNull List<String> getPlainTextListMessage() {
        return List.of(underlying.getAsPlainText());
    }

    @Override
    public void formatPlaceholderAPI() {
        this.underlying = this.underlying.parsePlaceholderAPI(relevantPlayer);
    }

    public void setMessage(@NotNull String message) {
        this.underlying = ComponentMessage.componentMessage(message).messageType(underlying.messageType());
    }

    public void setMessage(@NotNull Component message) {
        this.underlying = ComponentMessage.componentMessage(message).messageType(underlying.messageType());
    }

    public void setMessage(@NotNull EMFSingleMessage message) {
        this.underlying = message.underlying;
    }

    public void trim() {
        Component newComponent = MiniMessage.miniMessage().deserialize(
            MiniMessage.miniMessage().serialize(underlying.get()).stripTrailing()
        );
        this.underlying = ComponentMessage.componentMessage(newComponent, underlying.messageType());
    }

    @Override
    public boolean containsString(@NotNull String string) {
        return underlying.contains(string);
    }

}