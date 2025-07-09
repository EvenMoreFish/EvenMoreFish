package com.oheers.fish.entities;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class EntityLoader {

    public abstract Entity spawn(@NotNull Location location);

}
