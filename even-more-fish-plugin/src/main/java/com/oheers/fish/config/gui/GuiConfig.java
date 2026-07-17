package com.oheers.fish.config.gui;

import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.messages.EMFSingleMessage;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class GuiConfig extends ConfigBase {

    public GuiConfig(@NotNull String fileName, @NotNull String resourceName, @NotNull Plugin plugin, boolean configUpdater) {
        super(fileName, resourceName, plugin, configUpdater);
    }

    public @NotNull EMFSingleMessage getTitle() {
        return EMFSingleMessage.fromString(getConfig().getString("title", "EvenMoreFish GUI"));
    }

    public @NotNull String @NotNull [] getLayout() {
        return getConfig().getStringList("layout").stream()
            .filter(Objects::nonNull)
            .limit(6)
            .toArray(String[]::new);
    }

}
