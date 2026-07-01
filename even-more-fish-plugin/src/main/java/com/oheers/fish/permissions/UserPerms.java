package com.oheers.fish.permissions;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class UserPerms {

    private UserPerms() {
        throw new UnsupportedOperationException();
    }

    public static final String USE_ROD = "emf.use_rod";
    public static final String SHOP = "emf.shop";
    public static final String TOP = "emf.top";
    public static final String SELL_ALL = "emf.sellall";
    public static final String GUI = "emf.gui";
    public static final String NEXT = "emf.next";
    public static final String HELP = "emf.help";
    public static final String APPLYBAITS = "emf.applybaits";
    public static final String JOURNAL = "emf.journal";

    public static final String TOGGLE = "emf.toggle";
    public static final String TOGGLE_FISHING = "emf.toggle.fishing";
    public static final String TOGGLE_BOSSBAR = "emf.toggle.bossbar";
    public static final String TOGGLE_CATCH_MESSAGE = "emf.toggle.catchmessage";

    public static boolean checkTogglePerms(@NotNull CommandSender sender) {
        return sender.hasPermission(TOGGLE_FISHING)
            || sender.hasPermission(TOGGLE_BOSSBAR)
            || sender.hasPermission(TOGGLE_CATCH_MESSAGE);
    }

}
