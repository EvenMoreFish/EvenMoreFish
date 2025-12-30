package com.oheers.fish.commands;

import org.jetbrains.annotations.NotNull;

/**
 * @param <C> The command class.
 * @param <A> The argument class.
 */
public interface AdminCommandProvider<C, A> {

    @NotNull C get();

    @NotNull A getAsArgument();

    @NotNull A database();

    @NotNull A fish();

    @NotNull A list();

    @NotNull A competition();

    @NotNull A customRod();

    @NotNull A bait();

    @NotNull A clearBaits();

    @NotNull A reload();

    @NotNull A version();

    @NotNull A rawItem();

    @NotNull A help();

}
