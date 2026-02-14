package com.oheers.fish.baits.manager;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.baits.AbstractBaitManager;
import com.oheers.fish.api.baits.IBait;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.configs.BaitConversions;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.items.FishManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class BaitManager extends AbstractBaitManager<BaitHandler> {

    private static BaitManager instance;

    private BaitManager() {
        super();
    }

    public static @NotNull BaitManager getInstance() {
        if (instance == null) {
            instance = new BaitManager();
        }
        return instance;
    }

    @Override
    protected void performPreLoadConversions() {
        new BaitConversions().performCheck();
    }

    @Override
    protected void loadItems() {
        loadItemsFromFiles(
                "baits",
                file -> new BaitHandler(file, FishManager.getInstance(), MainConfig.getInstance()),
                BaitHandler::getId,
                bait -> {
                    if (bait.getBaitData().disabled()) {
                        EvenMoreFish.getInstance().debug("Skipping disabled bait: " + bait.getId());
                        return true;
                    }
                    return false;
                }
        );
    }

    @Override
    protected void logLoadedItems() {
        EvenMoreFish.getInstance().getLogger().info(
                "Loaded " + getItemMap().size() + " baits successfully."
        );
    }

    public @Nullable BaitHandler getBait(@Nullable String baitName) {
        return baitName != null ? getItem(baitName) : null;
    }

    public @Nullable BaitHandler getBait(@Nullable ItemStack itemStack) {
        return itemStack != null ? getBait(BaitNBTManager.getBaitName(itemStack)) : null;
    }

    @Override
    public @Nullable IBait getBait(@Nullable Entity itemEntity) {
        if (!(itemEntity instanceof Item item)) {
            return null;
        }
        return getBait(item.getItemStack());
    }

    @Override
    public boolean isBait(@Nullable ItemStack itemStack) {
        return itemStack != null && FishUtils.isBaitObject(itemStack);
    }

    @Override
    public boolean isBait(@Nullable Entity itemEntity) {
        if (!(itemEntity instanceof Item item)) {
            return false;
        }
        return isBait(item.getItemStack());
    }

}