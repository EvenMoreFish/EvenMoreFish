package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.api.config.ConfigUtils;
import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.config.RarityFileUpdates;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.EMFSingleMessage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Rarity extends ConfigBase implements IRarity {

    private static final Logger logger = EvenMoreFish.getInstance().getLogger();

    private final @NotNull String id;

    private boolean fishWeighted;
    private boolean showInJournal = true;
    private final Requirement requirement;
    private final List<Fish> fishList;

    /**
     * Constructs a Rarity from its config file.
     * @param file The file for this rarity.
     */
    public Rarity(@NotNull File file) throws InvalidConfigurationException {
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
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public boolean isDisabled() {
        return getConfig().getBoolean("disabled");
    }

    public @NotNull EMFSingleMessage getFormat() {
        String format = getConfig().getString("format", "<white>{name}");
        return EMFSingleMessage.fromString(format);
    }

    public @NotNull String getColour() {
        String color = getConfig().getString("color");
        if (color != null) {
            return color;
        }
        String format = getConfig().getString("format", "<white>{name}");
        int nameIndex = format.indexOf("{name}");
        if (nameIndex <= 0) {
            return "<white>";
        }
        return format.substring(0, nameIndex);
    }

    public @NotNull EMFSingleMessage format(@NotNull String name) {
        EMFSingleMessage message = getFormat();
        message.setVariable("{name}", name);
        return message;
    }

    @Override
    public double getWeight() {
        return getConfig().getDouble("weight");
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

    public @NotNull EMFSingleMessage getDisplayName() {
        String displayName = getConfig().getString("displayname", this.id);
        return format(displayName);
    }

    public @NotNull EMFSingleMessage getLorePrep() {
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

    @Override
    public @Nullable String getPermission() {
        return getConfig().getString("permission");
    }

    @Override
    public @NotNull Requirement getRequirement() {
        return requirement;
    }

    @Override
    public boolean isShouldDisableFisherman() {
        return getConfig().getBoolean("disable-fisherman", false);
    }

    @Override
    public double getMinSize() {
        return getConfig().getDouble("size.minSize");
    }

    @Override
    public double getMaxSize() {
        return getConfig().getDouble("size.maxSize");
    }

    // TODO this was set to always be false at some point, we need to re-add the removed code.
    public boolean hasCompExemptFish() {
        return false;
    }

    /**
     * @return This rarity's original list of loaded fish
     */
    @Override
    public @NotNull List<Fish> getOriginalFishList() {
        return fishList;
    }

    /**
     * @return This rarity's list of loaded fish, but each fish is a clone of the original
     */
    @Override
    public @NotNull List<Fish> getFishList() {
        return fishList.stream().map(Fish::createCopy).toList();
    }

    @Override
    public @Nullable Fish getEditableFish(@NotNull String name) {
        for (Fish fish : fishList) {
            if (fish.getName().equalsIgnoreCase(name)) {
                return fish;
            }
        }
        return null;
    }

    @Override
    public @Nullable Fish getFish(@NotNull String name) {
        Fish fish = getEditableFish(name);
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
    public @NotNull ItemStack getJournalItem() {
        // Old format for compatibility
        ItemStack oldItem = FishUtils.getItem(getConfig().getString("material"));
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

    private List<Fish> loadFish() {
        Section rootFishSection = getConfig().getSection("fish");
        if (rootFishSection == null) {
            return List.of();
        }
        List<Fish> fishList = new ArrayList<>();
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

    protected @NotNull CatchType getCatchType() {
        String typeStr = getConfig().getString("catch-type");
        CatchType type = FishUtils.getEnumValue(CatchType.class, typeStr);
        if (type == null) {
            return CatchType.BOTH;
        }
        return type;
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

}