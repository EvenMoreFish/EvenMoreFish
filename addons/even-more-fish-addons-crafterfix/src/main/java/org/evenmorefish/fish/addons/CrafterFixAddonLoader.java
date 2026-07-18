package org.evenmorefish.fish.addons;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.addons.AddonLoader;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.utils.system.JavaSpecVersion;
import com.oheers.fish.api.utils.system.SystemUtils;
import com.oheers.fish.utils.MinecraftVersionHelper;
import org.bukkit.Bukkit;

import java.io.File;


public class CrafterFixAddonLoader extends AddonLoader {

    public CrafterFixAddonLoader(EMFPlugin plugin, File addonFile) {
        super(plugin, addonFile);
    }

    @Override
    public boolean canLoad() {
        return SystemUtils.isJavaVersionAtLeast(JavaSpecVersion.JAVA_21) && MinecraftVersionHelper.isAtLeastVersion("1.21.1");
    }

    @Override
    public void loadAddons() {
        Logging.debug("Server is 1.21+. Registering Crafter Listener.");
        Bukkit.getPluginManager().registerEvents(new CrafterListener(), EvenMoreFish.getInstance());
    }


}
