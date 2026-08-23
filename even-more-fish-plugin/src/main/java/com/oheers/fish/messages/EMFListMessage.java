package com.oheers.fish.messages;

import com.oheers.fish.FishUtils;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.firedev.daisylib.messages.message.ComponentListMessage;
import uk.firedev.daisylib.messages.message.ComponentMessage;
import uk.firedev.daisylib.messages.message.ComponentSingleMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EMFListMessage extends EMFMessage {

    private ComponentListMessage underlying;

    private EMFListMessage(@NonNull ComponentListMessage message) {
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
    public @NonNull ComponentListMessage getUnderlying() {
        return underlying;
    }

    @Override
    public void setUnderlying(@NonNull ComponentMessage<?, ?> message) {
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

    public static EMFListMessage ofUnderlying(@NonNull ComponentListMessage underlying) {
        return new EMFListMessage(underlying);
    }

    public static EMFListMessage of(@NonNull Component component) {
        return new EMFListMessage(
            ComponentMessage.componentMessage(List.of(component))
        );
    }

    public static EMFListMessage ofList(@NonNull List<Component> components) {
        return new EMFListMessage(
            ComponentMessage.componentMessage(components)
        );
    }

    public static EMFListMessage fromString(@NonNull String string) {
        return new EMFListMessage(
            ComponentMessage.componentMessage(List.of(string))
        );
    }

    public static EMFListMessage fromStringList(@NonNull List<String> strings) {
        return new EMFListMessage(
            ComponentMessage.componentMessage(strings)
        );
    }

    // Class methods

    /**
     * @return The stored components in their original form, with no variables applied.
     */
    public @NonNull List<Component> getRawMessage() {
        return this.underlying.get();
    }

    @Override
    public @NonNull Component getComponentMessage(@Nullable OfflinePlayer player) {
        return Component.join(JoinConfiguration.newlines(), getComponentListMessage(player));
    }

    @Override
    public @NonNull List<Component> getComponentListMessage(@Nullable OfflinePlayer player) {
        return processPlaceholders(player).get();
    }

    @Override
    public @NonNull String getLegacyMessage(@Nullable OfflinePlayer player) {
        return String.join("\n", getLegacyListMessage(player));
    }

    @Override
    public @NonNull List<String> getLegacyListMessage(@Nullable OfflinePlayer player) {
        return processPlaceholders(player).getLegacy();
    }

    @Override
    public @NonNull String getPlainTextMessage(@Nullable OfflinePlayer player) {
        return String.join("\n", getPlainTextListMessage(player));
    }

    @Override
    public @NonNull List<String> getPlainTextListMessage(@Nullable OfflinePlayer player) {
        return processPlaceholders(player).getPlainText();
    }

    @Override
    public void formatPlaceholderAPI() {
        this.underlying = this.underlying.parsePlaceholderAPI(relevantPlayer);
    }

    @Override
    public boolean containsString(@NonNull String string) {
        return underlying.getPlainText().stream().anyMatch(line -> line.contains(string));
    }

    public void setVariableWithListInsertion(@NonNull String variable, @NonNull Object replacement) {
        this.underlying = this.underlying.replaceWithListInsertion(variable, replacement);
    }

    public void setVariablesWithListInsertion(@Nullable Map<String, ?> variableMap) {
        if (variableMap == null || variableMap.isEmpty()) {
            return;
        }
        this.underlying = this.underlying.replaceWithListInsertion(variableMap);
    }

    public void appendEachLine(@NonNull Object object) {
        setUnderlying(
            getUnderlying().appendEachLine(object)
        );
    }

    public void prependEachLine(@NonNull Object object) {
        setUnderlying(
            getUnderlying().prependEachLine(object)
        );
    }

}
