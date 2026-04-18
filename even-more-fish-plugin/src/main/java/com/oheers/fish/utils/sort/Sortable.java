package com.oheers.fish.utils.sort;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public interface Sortable {

    double getWeight();

    @NotNull String getId();

}
