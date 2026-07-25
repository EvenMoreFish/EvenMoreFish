package com.oheers.fish.api.config.serializer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface EMFSerializer<T> {

    @NonNull String serialize(@NonNull T element);

    @Nullable T deserialize(@Nullable String element);

}
