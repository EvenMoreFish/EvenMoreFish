package com.oheers.fish.messages;

import com.oheers.fish.FishUtils;
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

    @Override
    public ComponentListMessage processPlaceholders(@Nullable OfflinePlayer player) {
        OfflinePlayer relevant = Optional.ofNullable(player).orElse(relevantPlayer);
        String name = Optional.ofNullable(FishUtils.getPlayerName(relevant)).orElse("N/A");
        return underlying.parsePlaceholderAPI(relevant).replace("{player}", name);
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
        return processPlaceholders(player).get();
    }

    @Override
    public @NotNull String getLegacyMessage(@Nullable OfflinePlayer player) {
        return String.join("\n", getLegacyListMessage(player));
    }

    @Override
    public @NotNull List<String> getLegacyListMessage(@Nullable OfflinePlayer player) {
        return processPlaceholders(player).getAsLegacy();
    }

    @Override
    public @NotNull String getPlainTextMessage(@Nullable OfflinePlayer player) {
        return String.join("\n", getPlainTextListMessage(player));
    }

    @Override
    public @NotNull List<String> getPlainTextListMessage(@Nullable OfflinePlayer player) {
        return processPlaceholders(player).getAsPlainText();
    }

    @Override
    public void formatPlaceholderAPI() {
        this.underlying = this.underlying.parsePlaceholderAPI(relevantPlayer);
    }

    @Override
    public boolean containsString(@NotNull String string) {
        return underlying.getAsPlainText().stream().anyMatch(line -> line.contains(string));
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
        setUnderlying(
            getUnderlying().appendEachLine(object)
        );
    }

    public void prependEachLine(@NotNull Object object) {
        setUnderlying(
            getUnderlying().prependEachLine(object)
        );
    }

}
