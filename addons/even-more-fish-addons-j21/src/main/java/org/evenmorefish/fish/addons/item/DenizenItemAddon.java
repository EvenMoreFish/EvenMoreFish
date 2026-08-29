package org.evenmorefish.fish.addons.item;


import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.oheers.fish.api.addons.ItemAddon;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DenizenItemAddon extends ItemAddon {

    @Override
    public String getIdentifier() {
        return "denizen";
    }

    @Override
    public String getPluginName() {
        return "Denizen";
    }

    @Override
    public String getAuthor() {
        return "FireML";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public ItemStack getItemStack(String id) {
        final ItemTag itemTag = ItemTag.valueOf(id, false);
        if (itemTag == null) {
            getLogger().info(() -> String.format("Could not obtain denizen item %s", id));
            return null;
        }

        return itemTag.getItemStack();
    }

    @Override
    public @Nullable String convertToString(@NonNull ItemStack item) {
        ItemScriptContainer container = ItemScriptHelper.getItemScriptContainer(item);
        if (container == null) {
            return null;
        }
        return "denizen:" + container.getName();
    }

}
