package org.evenmorefish.fish.addons;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.addons.AddonLoader;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.utils.system.JavaSpecVersion;
import com.oheers.fish.api.utils.system.SystemUtils;
import com.oheers.fish.economy.ExcellentEconomyType;

import java.io.File;


public class ExcellentEconomyAddonLoader extends AddonLoader {

    public ExcellentEconomyAddonLoader(EMFPlugin plugin, File addonFile) {
        super(plugin, addonFile);
    }

    @Override
    public boolean canLoad() {
        return SystemUtils.isJavaVersionAtLeast(JavaSpecVersion.JAVA_25);
    }

    @Override
    public void loadAddons() {
        ExcellentEconomyType type = new ExcellentEconomyType();
        type.load();
        if (type.register()) {
            Logging.info("EvenMoreFish has successfully hooked into ExcellentEconomy.");
        }
    }

}
