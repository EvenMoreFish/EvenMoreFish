package com.oheers.evenmorefish.addons;

import com.oheers.fish.api.addons.AddonLoader;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

public class J21AddonLoader extends AddonLoader {

    @Override
    public @NotNull String getName() {
        return "Java 21 Addons";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @NotNull String getAuthor() {
        return "EvenMoreFish";
    }

    @Override
    public boolean canLoad() {
        return SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_21);
    }

    @Override
    public void loadAddons() {
        // ItemAddon
        new CraftEngineItemAddon().register();
        new NexoItemAddon().register();
        new OraxenItemAddon().register();
    }

}
