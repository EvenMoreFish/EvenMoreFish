package com.oheers.fish.messages;

import com.oheers.fish.FishUtils;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.firedev.daisylib.messages.message.ComponentListMessage;
import uk.firedev.daisylib.messages.message.ComponentMessage;
import uk.firedev.daisylib.messages.message.ComponentSingleMessage;

import java.util.List;
import java.util.Optional;

public class EMFSingleMessage extends EMFMessage {

    private ComponentSingleMessage underlying;

    protected EMFSingleMessage(@NonNull ComponentSingleMessage message) {
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
    public @NonNull ComponentSingleMessage getUnderlying() {
        return underlying;
    }

    @Override
    public void setUnderlying(@NonNull ComponentMessage<?, ?> message) {
        if (message instanceof ComponentListMessage listMessage) {
            this.underlying = listMessage.toSingleMessage();
        } else if (message instanceof ComponentSingleMessage singleMessage) {
            this.underlying = singleMessage;
        } else {
            // Should never happen.
            return;
        }
    }

    @Override
    public ComponentSingleMessage processPlaceholders(@Nullable OfflinePlayer player) {
        OfflinePlayer relevant = Optional.ofNullable(player).orElse(relevantPlayer);
        String name = Optional.ofNullable(FishUtils.getPlayerName(relevant)).orElse("N/A");
        return underlying.parsePlaceholderAPI(relevant).replace("{player}", name);
    }

    // Factory methods

    public static EMFSingleMessage empty() {
        return new EMFSingleMessage(
            ComponentMessage.componentMessage(Component.empty())
        );
    }

    public static EMFSingleMessage ofUnderlying(@NonNull ComponentSingleMessage underlying) {
        return new EMFSingleMessage(underlying);
    }

    public static EMFSingleMessage of(@NonNull Component component) {
        return new EMFSingleMessage(
            ComponentMessage.componentMessage(component)
        );
    }

    public static EMFSingleMessage ofList(@NonNull List<Component> components) {
        return new EMFSingleMessage(
            ComponentMessage.componentMessage(components).toSingleMessage()
        );
    }

    public static EMFSingleMessage fromString(@NonNull String string) {
        return new EMFSingleMessage(
            ComponentMessage.componentMessage(string)
        );
    }

    public static EMFSingleMessage fromStringList(@NonNull List<String> strings) {
        return new EMFSingleMessage(
            ComponentMessage.componentMessage(strings).toSingleMessage()
        );
    }

    // Class methods

    /**
     * @return The stored component.
     */
    public @NonNull Component getRawMessage() {
        return underlying.get();
    }

    @Override
    public @NonNull Component getComponentMessage(@Nullable OfflinePlayer player) {
        return processPlaceholders(player).get();
    }

    @Override
    public @NonNull List<Component> getComponentListMessage(@Nullable OfflinePlayer player) {
        return List.of(getComponentMessage(player));
    }

    @Override
    public @NonNull String getLegacyMessage(@Nullable OfflinePlayer player) {
        return processPlaceholders(player).getLegacy();
    }

    @Override
    public @NonNull List<String> getLegacyListMessage(@Nullable OfflinePlayer player) {
        return List.of(getLegacyMessage(player));
    }

    @Override
    public @NonNull String getPlainTextMessage(@Nullable OfflinePlayer player) {
        return processPlaceholders(player).getPlainText();
    }

    @Override
    public @NonNull List<String> getPlainTextListMessage(@Nullable OfflinePlayer player) {
        return List.of(getPlainTextMessage(player));
    }

    @Override
    public void formatPlaceholderAPI() {
        this.underlying = this.underlying.parsePlaceholderAPI(relevantPlayer);
    }

    public void setMessage(@NonNull String message) {
        this.underlying = ComponentMessage.componentMessage(message).messageType(underlying.messageType());
    }

    public void setMessage(@NonNull Component message) {
        this.underlying = ComponentMessage.componentMessage(message).messageType(underlying.messageType());
    }

    public void setMessage(@NonNull EMFSingleMessage message) {
        this.underlying = message.underlying;
    }

    public void trim() {
        Component newComponent = MiniMessage.miniMessage().deserialize(
            MiniMessage.miniMessage().serialize(underlying.get()).stripTrailing()
        );
        this.underlying = ComponentMessage.componentMessage(newComponent, underlying.messageType());
    }

    @Override
    public boolean containsString(@NonNull String string) {
        return underlying.contains(string);
    }

}