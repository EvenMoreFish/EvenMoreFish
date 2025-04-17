package com.oheers.fish.api.addons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Addon {

    default String getVersion() {
        return "0.0.0";
    }

    /**
     * @return The author's name
     */
    String getAuthor();

    String getIdentifier();

    boolean canLoad();

    default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

}
