package com.oheers.fish.addons.internal.item;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.utils.Base64;
import org.bukkit.inventory.ItemStack;

public class Head64ItemAddon extends ItemAddon {

    @Override
    public String getIdentifier() {
        return "head64";
    }

    @Override
    public String getPluginName() {
        return null;
    }

    @Override
    public String getAuthor() {
        return "EvenMoreFish";
    }

    @Override
    public ItemStack getItemStack(String id) {
        if (!Base64.isBase64(id)) {
            EvenMoreFish.getInstance().getLogger().warning(() -> String.format("%s is not a valid base64 string.", id));
            return null;
        }

        return FishUtils.getSkullFromBase64(id);
    }

}
