package org.evenmorefish.fish.addons;

import com.oheers.fish.api.addons.AddonLoader;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.utils.system.JavaSpecVersion;
import com.oheers.fish.api.utils.system.SystemUtils;

import java.io.File;


public class AddonTemplateLoader extends AddonLoader {

    public AddonTemplateLoader(EMFPlugin plugin, File addonFile) {
        super(plugin, addonFile);
    }

    @Override
    public boolean canLoad() {
        return SystemUtils.isJavaVersionAtLeast(JavaSpecVersion.JAVA_21);
    }

    @Override
    public void loadAddons() {}

}
