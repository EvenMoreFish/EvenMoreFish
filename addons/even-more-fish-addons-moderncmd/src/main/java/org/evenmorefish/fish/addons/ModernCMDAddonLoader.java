package org.evenmorefish.fish.addons;

import com.oheers.fish.api.addons.AddonLoader;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.utils.system.JavaSpecVersion;
import com.oheers.fish.api.utils.system.SystemUtils;
import com.oheers.fish.items.ItemConfigResolver;
import com.oheers.fish.utils.MinecraftVersionHelper;
import org.evenmorefish.fish.addons.itemconfig.ModernCustomModelDataItemConfig;

import java.io.File;

public class ModernCMDAddonLoader extends AddonLoader {

    public ModernCMDAddonLoader(EMFPlugin plugin, File addonFile) {
        super(plugin, addonFile);
    }

    @Override
    public boolean canLoad() {
        return SystemUtils.isJavaVersionAtLeast(JavaSpecVersion.JAVA_21) && MinecraftVersionHelper.isAtLeastVersion("MC1_21_R3");
    }

    @Override
    public void loadAddons() {
        // ItemConfig
        ItemConfigResolver.getInstance().setCustomModelDataResolver(ModernCustomModelDataItemConfig::new);
    }

}
