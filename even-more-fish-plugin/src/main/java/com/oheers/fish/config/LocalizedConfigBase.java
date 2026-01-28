package com.oheers.fish.config;

import com.oheers.fish.api.config.ConfigBase;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * For configuration files that are localized to a specific language.
 */
@ApiStatus.Internal
public class LocalizedConfigBase extends ConfigBase {

    public LocalizedConfigBase(@NotNull String fileName, @NotNull String resourceName, @NotNull Plugin plugin, boolean configUpdater) {
        super(
            fileName,
            "locales/" + MainConfig.getInstance().getLocale() + "/" + resourceName,
            plugin,
            configUpdater
        );
    }

}
