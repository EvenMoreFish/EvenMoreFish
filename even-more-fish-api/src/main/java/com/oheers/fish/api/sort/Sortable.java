package com.oheers.fish.api.sort;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

public interface Sortable {

    int getIndex();

    double getWeight();

    @NonNull String getId();

}
