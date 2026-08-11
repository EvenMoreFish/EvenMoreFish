package com.oheers.fish.api.sort;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

@ApiStatus.Internal
public interface Sortable {

    int getIndex();

    double getWeight();

    @NonNull String getId();

}
