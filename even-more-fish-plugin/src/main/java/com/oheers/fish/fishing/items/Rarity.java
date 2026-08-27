package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.api.config.ConfigUtils;
import com.oheers.fish.api.config.serializer.ItemSerializer;
import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.config.RarityFileUpdates;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.EMFSingleMessage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

public class Rarity extends ConfigBase implements IRarity {

    private final @NonNull String id;

    private boolean fishWeighted;
    private boolean showInJournal = true;
    private final Requirement requirement;
    private final List<IFish> fishList;

    /**
     * Constructs a Rarity from its config file.
     * @param file The file for this rarity.
     */
    public Rarity(@NonNull File file) throws InvalidConfigurationException {
        super(file, EvenMoreFish.getInstance(), false);
        new RarityFileUpdates(this).update();
        this.id = validateId();
        this.requirement = loadRequirements();
        this.fishList = loadFish();
        this.showInJournal = getConfig().getBoolean("journal", true);
    }

    private String validateId() throws InvalidConfigurationException {
        String id = getConfig().getString("id");
        if (id == null) {
            throw new InvalidConfigurationException("Rarity " + getFileName() + " has no configured id.");
        }
        return id;
    }

    // Config getters

    @Override
    public @NonNull String getId() {
        return this.id;
    }

    @Override
    public boolean isDisabled() {
        return getConfig().getBoolean("disabled");
    }

    private @NonNull String getRawFormat() {
        return getConfig().getString("format", "<white>{name}");
    }

    public @NonNull EMFSingleMessage getFormat() {
        return EMFSingleMessage.fromString(getRawFormat());
    }

    public @NonNull EMFSingleMessage format(@NonNull String name) {
        String format = getRawFormat().replace("{name}", name);
        return EMFSingleMessage.fromString(format);
    }

    @Override
    public double getWeight() {
        return getConfig().getDouble("weight");
    }

    public int getCatchLimit() {
        return getConfig().getInt("catch-limit", -1);
    }

    @Override
    public boolean getBroadcastEnabled() {
        return getConfig().getBoolean("broadcast.enabled", true);
    }

    @Override
    public boolean getBroadcastOnlyRods() {
        return getConfig().getBoolean("broadcast.only-rods", false);
    }

    @Override
    public int getBroadcastRange() {
        return getConfig().getInt("broadcast.range", -1);
    }

    @Override
    public boolean getUseConfigCasing() {
        return getConfig().getBoolean("use-this-casing");
    }

    public @NonNull Component getDisplayName() {
        return getDisplayNameMessage().getComponentMessage();
    }

    public @NonNull EMFSingleMessage getDisplayNameMessage() {
        String displayName = getConfig().getString("displayname", this.id);
        return format(displayName);
    }

    public @NonNull EMFSingleMessage getLorePrep() {
        String loreOverride = getConfig().getString("override-lore");
        if (loreOverride != null) {
            return EMFSingleMessage.fromString(loreOverride);
        }
        String displayName = getConfig().getString("displayname");
        if (displayName != null) {
            return EMFSingleMessage.fromString(displayName);
        }
        String finalName = getId();
        if (!getUseConfigCasing()) {
            finalName = finalName.toUpperCase();
        }
        return format(finalName);
    }

    protected List<String> getLoreOverride() {
        return getConfig().getStringList("lore-override");
    }

    @Override
    public @Nullable String getPermission() {
        return getConfig().getString("permission");
    }

    @Override
    public @NonNull Requirement getRequirement() {
        return requirement;
    }

    @Override
    public boolean isShouldDisableFisherman() {
        return getConfig().getBoolean("disable-fisherman", false);
    }

    @Override
    public double getSetWorth() {
        Double worth = FishUtils.parseDoubleOrRange(getConfig().getString("set-worth"));
        return worth == null ? -1 : worth;
    }

    @Override
    public @NonNull Optional<Double> getSetSize() {
        return getSetSize(null);
    }

    public @NonNull Optional<Double> getSetSize(@Nullable OfflinePlayer player) {
        return Optional.ofNullable(FishUtils.fetchSize(getConfig(), "size", player));
    }

    @Override
    public double getMinSize() {
        return getMinSize(null);
    }

    public double getMinSize(@Nullable OfflinePlayer player) {
        Double minSize = FishUtils.fetchSize(getConfig(), "size.minSize", player);
        return minSize == null ? 1D : minSize;
    }

    @Override
    public double getMaxSize() {
        return getMaxSize(null);
    }

    public double getMaxSize(@Nullable OfflinePlayer player) {
        Double maxSize = FishUtils.fetchSize(getConfig(), "size.maxSize", player);
        return maxSize == null ? 10D : maxSize;
    }

    /**
     * @return This rarity's original list of loaded fish
     */
    @Override
    public @NonNull List<IFish> getOriginalFishList() {
        return fishList;
    }

    /**
     * @return This rarity's list of loaded fish, but each fish is a clone of the original
     */
    @Override
    public @NonNull List<IFish> getFishList() {
        return fishList.stream().map(IFish::createCopy).toList();
    }

    @Override
    public @Nullable IFish getEditableFish(@NonNull String name) {
        for (IFish fish : fishList) {
            if (fish.getId().equalsIgnoreCase(name)) {
                return fish;
            }
        }
        return null;
    }

    @Override
    public @Nullable IFish getFish(@NonNull String name) {
        IFish fish = getEditableFish(name);
        if (fish == null) {
            return null;
        }
        return fish.createCopy();
    }

    @Override
    public double getWorthMultiplier() {
        return getConfig().getDouble("worth-multiplier", 0.0D);
    }

    @Override
    public @NonNull ItemStack getJournalItem() {
        // Old format for compatibility
        ItemStack oldItem = ItemSerializer.get().deserialize(getConfig().getString("material"), true);
        if (oldItem != null) {
            return oldItem;
        }
        // New format that accepts ItemFactory configs
        ItemFactory factory = ItemFactory.itemFactory(getConfig());
        return factory.createItem();
    }

    @Override
    public boolean getShowInJournal() {
        return showInJournal;
    }

    @Override
    public void setShowInJournal(boolean showInJournal) {
        this.showInJournal = showInJournal;
    }

    // External variables

    @Override
    public boolean isFishWeighted() {
        return fishWeighted;
    }

    @Override
    public void setFishWeighted(boolean fishWeighted) {
        this.fishWeighted = fishWeighted;
    }

    // Loading stuff

    private List<IFish> loadFish() {
        Section rootFishSection = getConfig().getSection("fish");
        if (rootFishSection == null) {
            return List.of();
        }
        List<IFish> fishList = new ArrayList<>();
        rootFishSection.getRoutesAsStrings(false).forEach(fishStr -> {
            Section fishSection = rootFishSection.getSection(fishStr);
            if (fishSection == null) {
                fishSection = rootFishSection.createSection(fishStr);
            }
            try {
                fishList.add(Fish.createOrThrow(this, fishSection));
            } catch (InvalidFishException exception) {
                EvenMoreFish.getInstance().getLogger().log(Level.WARNING, exception.getMessage(), exception);
            }
        });
        // Creates an immutable list.
        return List.copyOf(fishList);
    }

    private Requirement loadRequirements() {
        Section requirementSection = ConfigUtils.getSectionOfMany(getConfig(), "requirements", "requirement");
        return new Requirement(requirementSection);
    }

    protected @NonNull CatchType getCatchType() {
        String typeStr = getConfig().getString("catch-type");
        CatchType type = FishUtils.getEnumValue(CatchType.class, typeStr);
        if (type == null) {
            return CatchType.BOTH;
        }
        return type;
    }

    public @NonNull List<String> getInteractRewards() {
        return getConfig().getStringList("interact-event");
    }

    public @NonNull List<String> getEatRewards() {
        return getConfig().getStringList("eat-event");
    }

    public @NonNull List<String> getCatchRewards() {
        return getConfig().getStringList("catch-event");
    }

    public @NonNull List<String> getSellRewards() {
        return getConfig().getStringList("sell-event");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Rarity rarity)) {
            return false;
        }
        // Check if the id matches.
        return this.getId().equals(rarity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    // Sortable

    @Override
    public int getIndex() {
        return getConfig().getInt("sort-index");
    }

}
