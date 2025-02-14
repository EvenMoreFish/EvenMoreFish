package com.oheers.fish.api.addons;

import com.oheers.fish.api.addons.exceptions.JavaVersionException;
import com.oheers.fish.api.addons.exceptions.RequiredPluginException;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.bukkit.Bukkit;

import java.util.logging.Logger;


public interface Addon {

    /**
     * @return The prefix used by the addon: "playerhead:id" or "base64:id" or "prefix:id"
     */
    String getPrefix();

    /**
     * @return The dependant plugin name, if there isn't one, you can set this to null.
     * You should override {@link #canRegister() canRegister() method} if there is no dependant plugin.
     */
    String getPluginName();

    default String getVersion() {
        return "0.0.0";
    }


    /**
     * This should check if the addon can be registered. I.e., required java version + required plugin.
     */
    default boolean canRegister() throws JavaVersionException, RequiredPluginException {
        final boolean hasRequiredPlugin = Bukkit.getPluginManager().getPlugin(getPluginName()) != null;
        final boolean hasRequiredJavaVersion = SystemUtils.isJavaVersionAtLeast(getRequiredJavaVersion());
        if(!hasRequiredPlugin) {
            throw new RequiredPluginException(getPluginName());
        }
        if(!hasRequiredJavaVersion) {
            throw new JavaVersionException(getPluginName(), getRequiredJavaVersion());
        }
        return (Bukkit.getPluginManager().isPluginEnabled(getPluginName())) && SystemUtils.isJavaVersionAtLeast(getRequiredJavaVersion());
    }

    /**
     * @return The author's name
     */
    String getAuthor();

    default JavaVersion getRequiredJavaVersion() {
        return JavaVersion.JAVA_17;
    }

    default Logger getLogger() {
        return Logger.getLogger("EvenMoreFish:" + getPrefix());
    }

}
