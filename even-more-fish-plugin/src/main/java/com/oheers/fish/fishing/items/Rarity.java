package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.utils.ItemUtils;
import com.oheers.fish.fishing.CatchType;
import com.oheers.fish.fishing.items.config.RarityFileUpdates;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.utils.ItemUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Rarity extends ConfigBase {

    private static final Logger logger = EvenMoreFish.getInstance().getLogger();

    private boolean fishWeighted;
    private Requirement requirement = new Requirement();
    private final List<Fish> fishList;

    /**
     * Constructs a Rarity from its config file.
     * @param file The file for this rarity.
     */
    public Rarity(@NotNull File file) throws InvalidConfigurationException {
        super(file, EvenMoreFish.getInstance(), false);
        RarityFileUpdates.update(this);
        performRequiredConfigChecks();
        updateRequirementFormats();
        fishList = loadFish();
    }

    // Current required config: id
    private void performRequiredConfigChecks() throws InvalidConfigurationException {
        if (getConfig().getString("id") == null) {
            logger.warning("Rarity invalid: 'id' missing in " + getFileName());
            throw new InvalidConfigurationException("An ID has not been found in " + getFileName() + ". Please correct this.");
        }
    }

    // Config getters

    public @NotNull String getId() {
        return Objects.requireNonNull(getConfig().getString("id"));
    }

    public boolean isDisabled() {
        return getConfig().getBoolean("disabled");
    }

    public @NotNull EMFSingleMessage getFormat() {
        String format = getConfig().getString("format", "<white>{name}");
        return EMFSingleMessage.fromString(format);
    }

    public @NotNull EMFSingleMessage format(@NotNull String name) {
        EMFSingleMessage message = getFormat();
        message.setVariable("{name}", name);
        return message;
    }

    public double getWeight() {
        return getConfig().getDouble("weight");
    }

    public boolean getAnnounce() {
        return getConfig().getBoolean("broadcast", true);
    }

    public boolean getUseConfigCasing() {
        return getConfig().getBoolean("use-this-casing");
    }

    public @NotNull EMFSingleMessage getDisplayName() {
        String displayName = getConfig().getString("displayname");
        return format(Objects.requireNonNullElseGet(displayName, this::getId));
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

    public @Nullable String getPermission() {
        return getConfig().getString("permission");
    }

    public Requirement getRequirement() {
        if (requirement == null) {
            requirement = loadRequirements();
        }
        return requirement;
    }

    public boolean isShouldDisableFisherman() {
        return getConfig().getBoolean("disable-fisherman", false);
    }

    public double getMinSize() {
        return getConfig().getDouble("size.minSize");
    }

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
    public @NotNull List<Fish> getOriginalFishList() {
        return fishList;
    }

    /**
     * @return This rarity's list of loaded fish, but each fish is a clone of the original
     */
    public @NotNull List<Fish> getFishList() {
        return fishList.stream().map(Fish::createCopy).toList();
    }

    public @Nullable Fish getEditableFish(@NotNull String name) {
        for (Fish fish : fishList) {
            if (fish.getName().equalsIgnoreCase(name)) {
                return fish;
            }
        }
        return null;
    }

    public @Nullable Fish getFish(@NotNull String name) {
        Fish fish = getEditableFish(name);
        if (fish == null) {
            return null;
        }
        return fish.createCopy();
    }

    public double getWorthMultiplier() {
        return getConfig().getDouble("worth-multiplier", 0.0D);
    }

    public Material getMaterial() {
        return ItemUtils.getMaterial(
            getConfig().getString("material"),
            Material.COD
        );
    }

    // External variables

    public boolean isFishWeighted() {
        return fishWeighted;
    }

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
        Section requirementSection = getConfig().getSection("requirements");
        Requirement requirement = new Requirement();
        if (requirementSection == null) {
            return requirement;
        }
        requirementSection.getRoutesAsStrings(false).forEach(requirementString -> {
            List<String> values = new ArrayList<>();
            if (requirementSection.isList(requirementString)) {
                values.addAll(requirementSection.getStringList(requirementString));
            } else {
                values.add(requirementSection.getString(requirementString));
            }
            requirement.add(requirementString, values);
        });
        return requirement;
    }

    private void updateRequirementFormats() {
        updateRequirementFormats(getConfig());
        Section fishSect = getConfig().getSection("fish");
        if (fishSect != null) {
            fishSect.getRoutesAsStrings(false).forEach(fishName -> {
                Section section = fishSect.getSection(fishName);
                if (section == null) {
                    return;
                }
                updateRequirementFormats(section);
            });
        }
        save();
    }

    private void updateRequirementFormats(@NotNull Section section) {
        Section ingameSection = section.getSection("requirements.ingame-time");
        if (ingameSection != null) {
            int min = ingameSection.getInt("minTime");
            int max = ingameSection.getInt("maxTime");
            ingameSection.remove("minTime");
            ingameSection.remove("maxTime");
            section.set("requirements.ingame-time", min + "-" + max);
        }
        Section irlSection = section.getSection("requirements.irl-time");
        if (irlSection != null) {
            String min = irlSection.getString("minTime");
            String max = irlSection.getString("maxTime");
            irlSection.remove("minTime");
            irlSection.remove("maxTime");
            section.set("requirements.irl-time", min + "-" + max);
        }
    }

    protected @NotNull CatchType getCatchType() {
        String typeStr = getConfig().getString("catch-type", "BOTH");
        CatchType type;
        try {
            type = CatchType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance().getLogger().warning("Rarity " + getId() + " has an incorrect catch-type. Defaulting to BOTH.");
            type = CatchType.BOTH;
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