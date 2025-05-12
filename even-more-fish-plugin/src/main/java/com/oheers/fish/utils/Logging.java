package com.oheers.fish.utils;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.messages.EMFSingleMessage;

public class Logging {

    public static void info(String message) {
        EvenMoreFish.getInstance().getLogger().info(message);
    }

    public static void infoComponent(String message) {
        EvenMoreFish.getInstance().getComponentLogger().info(
            EMFSingleMessage.fromString(message).getComponentMessage()
        );
    }

    public static void warn(String message) {
        EvenMoreFish.getInstance().getLogger().warning(message);
    }

    public static void warnComponent(String message) {
        EvenMoreFish.getInstance().getComponentLogger().warn(
            EMFSingleMessage.fromString(message).getComponentMessage()
        );
    }

    public static void error(String message) {
        EvenMoreFish.getInstance().getLogger().severe(message);
    }

    public static void errorComponent(String message) {
        EvenMoreFish.getInstance().getComponentLogger().error(
            EMFSingleMessage.fromString(message).getComponentMessage()
        );
    }

}
