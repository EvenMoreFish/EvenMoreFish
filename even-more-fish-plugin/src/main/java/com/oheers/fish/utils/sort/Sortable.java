package com.oheers.fish.utils.sort;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@ApiStatus.Internal
public interface Sortable {

    int getIndex();

    double getWeight();

    @NotNull String getId();

}
