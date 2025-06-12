package com.oheers.fish.utils.nbt;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class NbtKeys {

    private NbtKeys() {
        throw new UnsupportedOperationException();
    }

    public static final String EMF_COMPOUND = JavaPlugin.getProvidingPlugin(NbtUtils.class).getName().toLowerCase(Locale.ROOT);
    public static final String EMF_FISH_PLAYER = "emf-fish-player";
    public static final String EMF_FISH_RARITY = "emf-fish-rarity";
    public static final String EMF_FISH_LENGTH = "emf-fish-length";
    public static final String EMF_FISH_NAME = "emf-fish-name";
    public static final String EMF_FISH_RANDOM_INDEX = "emf-fish-random-index";
    public static final String EMF_BAIT = "emf-bait";
    public static final String EMF_APPLIED_BAIT = "emf-applied-bait";

    // Used to identify whether a baited rod has been updated with the line identifier
    public static final String EMF_BAIT_REFORMATTED = "emf-bait-reformatted";

    public static final String EMF_ROD_NBT = "emf-rod-nbt";
    public static final String EMF_ROD_ID = "emf-rod-id";

    public static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";

}
