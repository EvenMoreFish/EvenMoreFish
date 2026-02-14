package com.oheers.fish.fishing.rods;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.fishing.rods.AbstractRodManager;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RodManager extends AbstractRodManager<CustomRod> {

    private static final RodManager instance = new RodManager();

    private RodManager() {
        super();
    }

    public static @NotNull RodManager getInstance() {
        return instance;
    }

    @Override
    protected void performPreLoadConversions() {
        new RodConversion().performCheck();
    }

    @Override
    protected void loadItems() {
        performPreLoadConversions();
        loadItemsFromFiles(
                "rods",
                CustomRod::new,
                CustomRod::getId,
                rod -> {
                    if (rod.isDisabled()) {
                        return true;
                    }
                    if (rod.getRecipe() != null) {
                        rod.getRecipe().register();
                    }
                    return false;
                }
        );
    }

    @Override
    protected void logLoadedItems() {
        EvenMoreFish.getInstance().getLogger().info("Loaded RodManager with " + itemMap.size() + " Custom Rods.");
    }

    @Override
    protected void clearMap(boolean reload) {
        if (reload) {
            itemMap.forEach((id, rod) -> {
                if (rod.getRecipe() != null) {
                    rod.getRecipe().unregister();
                }
            });
        }
        super.clearMap(reload);
    }

    @Override
    public @Nullable CustomRod getRod(@NotNull ItemStack item) {
        String rodId = NbtUtils.getString(item, NbtKeys.EMF_ROD_ID);
        if (rodId != null) {
            return getItem(rodId);
        }
        // For the old nbt-rod tags
        if (NbtUtils.hasKey(item, NbtKeys.EMF_ROD_NBT)) {
            return getItem("default");
        }
        return null;
    }

    @Override
    public @Nullable CustomRod getRod(@NotNull String rodId) {
        return getItem(rodId);
    }

}
