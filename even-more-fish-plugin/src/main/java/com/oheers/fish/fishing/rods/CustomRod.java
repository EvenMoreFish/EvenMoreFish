package com.oheers.fish.fishing.rods;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.recipe.EMFRecipe;
import com.oheers.fish.recipe.RecipeUtil;
import com.oheers.fish.api.Logging;
import com.oheers.fish.utils.nbt.NbtKeys;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CustomRod extends ConfigBase {

    private final List<Rarity> allowedRarities;
    private final List<Fish> allowedFish;
    private final EMFRecipe<?> recipe;
    private final ItemFactory factory;

    public CustomRod(@NotNull File file) throws InvalidConfigurationException {
        super(file, EvenMoreFish.getInstance(), false);
        performRequiredConfigChecks();
        this.allowedRarities = loadAllowedRarities();
        this.allowedFish = loadAllowedFish();
        this.factory = ItemFactory.itemFactory(getConfig());
        this.factory.setFinalChanges(item ->
            NBT.modify(item, nbt -> {
                ReadWriteNBT emfCompound = nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND);
                emfCompound.setString(NbtKeys.EMF_ROD_ID, getId());
            })
        );
        this.recipe = loadRecipe();
    }

    // Current required config: id
    private void performRequiredConfigChecks() throws InvalidConfigurationException {
        if (getConfig().getString("id") == null) {
            Logging.warn("Custom Rod invalid: 'id' missing in " + getFileName());
            throw new InvalidConfigurationException("An ID has not been found in " + getFileName() + ". Please correct this.");
        }
    }

    // Loading things

    /**
     * @return the contents of the "allowed-rarities" section, or an empty list if the section does not exist.
     */
    public List<Rarity> loadAllowedRarities() {
        if ("ALL".equalsIgnoreCase(getConfig().getString("allowed-rarities"))) {
            return FishManager.getInstance().getRarityMap().values().stream().toList();
        }
        return getConfig().getStringList("allowed-rarities").stream()
            .map(id -> FishManager.getInstance().getRarity(id))
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * @return the contents of the "allowed-fish" section, or an empty list if the section does not exist.
     */
    public List<Fish> loadAllowedFish() {
        Section section = getConfig().getSection("allowed-fish");
        if (section == null) {
            return List.of();
        }

        return section.getRoutesAsStrings(false).stream()
            .flatMap(rarityId -> {
                Rarity rarity = FishManager.getInstance().getRarity(rarityId);
                if (rarity == null) {
                    Logging.warn("Rarity '" + rarityId + "' not found for custom rod '" + getId() + "'.");
                    return Stream.empty();
                }
                if (!allowedRarities.contains(rarity)) {
                    Logging.warn("Rarity '" + rarityId + "' is not allowed for custom rod '" + getId() + "'.");
                    return Stream.empty();
                }
                return section.getStringList(rarityId)
                    .stream()
                    .map(rarity::getFish)
                    .filter(Objects::nonNull);
            })
            .toList();
    }

    private EMFRecipe<?> loadRecipe() {
        Section section = getConfig().getSection("recipe");
        if (section == null) {
            return null;
        }
        return RecipeUtil.getRecipe(
            section,
            getRecipeKey(),
            create()
        );
    }

    // Config getters

    public @NotNull String getId() {
        return Objects.requireNonNull(getConfig().getString("id"));
    }

    private @NotNull NamespacedKey getRecipeKey() {
        return new NamespacedKey(EvenMoreFish.getInstance(), "customrod-" + getId());
    }

    public boolean isDisabled() {
        return getConfig().getBoolean("disabled");
    }

    public ItemFactory getFactory() {
        return this.factory;
    }

    public ItemStack create() {
        return factory.createItem();
    }

    public List<Rarity> getAllowedRarities() {
        return this.allowedRarities;
    }

    public List<Fish> getAllowedFish() {
        return this.allowedFish;
    }

    public @Nullable EMFRecipe<?> getRecipe() {
        return this.recipe;
    }

}
