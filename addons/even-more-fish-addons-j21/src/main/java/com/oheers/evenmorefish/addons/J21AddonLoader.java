package com.oheers.evenmorefish.addons;

import com.oheers.evenmorefish.addons.item.CraftEngineItemAddon;
import com.oheers.evenmorefish.addons.item.DenizenItemAddon;
import com.oheers.evenmorefish.addons.item.EcoItemsItemAddon;
import com.oheers.evenmorefish.addons.item.HeadDatabaseItemAddon;
import com.oheers.evenmorefish.addons.item.ItemsAdderItemAddon;
import com.oheers.evenmorefish.addons.item.NexoItemAddon;
import com.oheers.evenmorefish.addons.item.OraxenItemAddon;
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
        new EcoItemsItemAddon().register();
        new NexoItemAddon().register();
        new OraxenItemAddon().register();
        new DenizenItemAddon().register();
        new HeadDatabaseItemAddon().register();
        new ItemsAdderItemAddon().register();
    }


}
