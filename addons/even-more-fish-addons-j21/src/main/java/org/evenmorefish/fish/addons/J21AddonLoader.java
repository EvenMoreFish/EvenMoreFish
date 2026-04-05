package org.evenmorefish.fish.addons;

import org.evenmorefish.fish.addons.item.CraftEngineItemAddon;
import org.evenmorefish.fish.addons.item.DenizenItemAddon;
import org.evenmorefish.fish.addons.item.EcoItemsItemAddon;
import org.evenmorefish.fish.addons.item.HeadDatabaseItemAddon;
import org.evenmorefish.fish.addons.item.ItemsAdderItemAddon;
import org.evenmorefish.fish.addons.item.MMOItemsItemAddon;
import org.evenmorefish.fish.addons.item.NexoItemAddon;
import org.evenmorefish.fish.addons.item.OraxenItemAddon;
import com.oheers.fish.api.addons.AddonLoader;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.utils.system.JavaSpecVersion;
import com.oheers.fish.api.utils.system.SystemUtils;

import java.io.File;


public class J21AddonLoader extends AddonLoader {

    public J21AddonLoader(EMFPlugin plugin, File addonFile) {
        super(plugin, addonFile);
    }

    @Override
    public boolean canLoad() {
        return SystemUtils.isJavaVersionAtLeast(JavaSpecVersion.JAVA_21);
    }

    @Override
    public void loadAddons() {
        // ItemAddon
        new CraftEngineItemAddon().register();
        new DenizenItemAddon().register();
        new EcoItemsItemAddon().register();
        new HeadDatabaseItemAddon().register();
        new ItemsAdderItemAddon().register();
        new MMOItemsItemAddon().register();
        new NexoItemAddon().register();
        new OraxenItemAddon().register();
    }


}
