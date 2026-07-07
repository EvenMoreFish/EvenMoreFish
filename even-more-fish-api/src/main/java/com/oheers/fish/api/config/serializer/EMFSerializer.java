package com.oheers.fish.api.config.serializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EMFSerializer<T> {

    @NotNull String serialize(@NotNull T element);

    @Nullable T deserialize(@Nullable String element);

}
