package com.oheers.fish.utils.sort;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@ApiStatus.Internal
public interface Sortable {

    int getIndex();

    double getWeight();

    @NonNull String getId();

}
