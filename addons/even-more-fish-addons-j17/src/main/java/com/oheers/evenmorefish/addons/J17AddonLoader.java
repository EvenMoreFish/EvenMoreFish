package com.oheers.evenmorefish.addons;

import com.oheers.fish.api.addons.AddonLoader;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.utils.system.JavaSpecVersion;
import com.oheers.fish.api.utils.system.SystemUtils;

import java.io.File;


public class J17AddonLoader extends AddonLoader {

    public J17AddonLoader(EMFPlugin plugin, File addonFile) {
        super(plugin, addonFile);
    }


    @Override
    public boolean canLoad() {
        return SystemUtils.isJavaVersionAtLeast(JavaSpecVersion.JAVA_17);
    }

    @Override
    public void loadAddons() {
        new DenizenItemAddon().register();
        new HeadDatabaseItemAddon().register();
        new ItemsAdderItemAddon().register();
    }

}
