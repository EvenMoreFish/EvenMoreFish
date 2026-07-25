package com.oheers.fish.config.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.messages.EMFSingleMessage;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public abstract class GuiConfig extends ConfigBase {

    public GuiConfig(@NonNull String name) {
        super(
            "gui/" + name,
            "gui/" + name,
            EvenMoreFish.getInstance(),
            true
        );
    }

    public @NonNull EMFSingleMessage getTitle() {
        return EMFSingleMessage.fromString(getConfig().getString("title", "EvenMoreFish GUI"));
    }

    public @NonNull String @NonNull [] getLayout() {
        return getConfig().getStringList("layout").stream()
            .filter(Objects::nonNull)
            .limit(6)
            .toArray(String[]::new);
    }

    public abstract boolean isPaginated();

}
