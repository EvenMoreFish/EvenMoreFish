package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.config.ConfigUtils;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.selling.WorthNBT;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class Fish implements IFish {

    private final @NotNull Section section;
    private final String name;
    private final Rarity rarity;
    private final ItemFactory factory;
    private UUID fisherman;
    private float length;

    private @Nullable List<Reward> actionRewards = null;
    private @Nullable List<Reward> fishRewards = null;
    private @Nullable List<Reward> sellRewards = null;
    private String eventType;

    private @NotNull Requirement requirement;

    private boolean wasBaited;
    private boolean silent;

    private double weight;

    private double minSize;
    private double maxSize;

    private boolean isCompExemptFish;

    private final boolean disableFisherman;
    private final String displayName;

    private boolean showInJournal;

    private final double setWorth;

    private Fish(@NotNull Rarity rarity, @NotNull Section section) {
        this.section = section;
        this.rarity = rarity;
        // This should never be null, but we have this check just to be safe.
        this.name = Objects.requireNonNull(section.getNameAsString());

        this.weight = section.getDouble("weight");
        if (this.weight != 0) {
            rarity.setFishWeighted(true);
        }

        this.length = -1F;

        this.disableFisherman = section.getBoolean("disable-fisherman", rarity.isShouldDisableFisherman());

        this.setWorth = section.getDouble("set-worth");

        ItemFactory factory = ItemFactory.itemFactory(section);
        factory.setFinalChanges(fish -> {
            fish.editMeta(meta -> {
                meta.displayName(getDisplayName().getComponentMessage());
                if (!section.getBoolean("disable-lore", false)) {
                    meta.lore(getFishLore());
                }
                meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            });
            WorthNBT.setNBT(fish, this);
        });
        this.factory = factory;

        this.displayName = factory.getDisplayName().getConfiguredValue();

        this.showInJournal = section.getBoolean("journal", true);

        factory.getLore().setEnabled(!section.getBoolean("disable-lore", false));

        setSize();

        checkSilent();

        this.requirement = loadRequirements();
    }

    /**
     * Creates a Fish from its config section.
     * @param section The section for this fish.
     */
    public static Fish create(@NotNull Rarity rarity, @NotNull Section section) {
        return new Fish(rarity, section);
    }

    /**
     * Creates a Fish from its config section.
     * @param section The section for this fish.
     * @throws InvalidFishException When section is null.
     */
    public static Fish createOrThrow(@NotNull Rarity rarity, @Nullable Section section) throws InvalidFishException {
        if (section == null) {
            throw new InvalidFishException("Fish could not be fetched from the config.");
        }
        return new Fish(rarity, section);
    }

    private Requirement loadRequirements() {
        Section requirementSection = ConfigUtils.getSectionOfMany(section, "requirements", "requirement");
        return new Requirement(requirementSection);
    }

    @Override
    public @NotNull ItemStack give(int randomIndex) {
        int initialIndex = factory.getRandomIndex();

        factory.setRandomIndex(randomIndex);
        ItemStack item = give();
        factory.setRandomIndex(initialIndex);

        return item;
    }

    /**
     * Returns the item stack version of the fish to be given to the player.
     *
     * @return An ItemStack version of the fish.
     */
    @Override
    public @NotNull ItemStack give() {
        return factory.createItem(fisherman);
    }

    private OfflinePlayer getFishermanPlayer() {
        return fisherman == null ? null : Bukkit.getOfflinePlayer(fisherman);
    }

    private void setSize() {
        this.minSize = section.getDouble("size.minSize");
        this.maxSize = section.getDouble("size.maxSize");

        // are min & max size changed? If not, there's no fish-specific value. Check the rarity's value
        if (minSize == 0.0 && maxSize == 0.0) {
            this.minSize = rarity.getMinSize();
            this.maxSize = rarity.getMaxSize();
        }

        // If there's no rarity-specific value (or max is smaller than min), to avoid being in a pickle we just set min default to 0 and max default to 10
        if ((minSize == 0.0 && maxSize == 0.0) || minSize > maxSize) {
            this.minSize = 0.0;
            this.maxSize = 10.0;
        }
    }

    private void generateSize() {
        if (minSize < 0) {
            this.length = -1f;
        } else {
            // Calculate the range for the random number (scaled by 10 to preserve decimal precision)
            int range = (int) ((maxSize - minSize) * 10);

            // Generate a random integer within the range (0 to range-1)
            int randomValue = EvenMoreFish.getInstance().getRandom().nextInt(range + 1); // nextInt(bound) ensures a positive value

            // Calculate the length, scaling back down by dividing by 10
            this.length = (float) (randomValue + minSize * 10) / 10;
        }
    }

    @Override
    public double getWorthMultiplier() {
        return section.getDouble("worth-multiplier", rarity.getWorthMultiplier());
    }

    @Override
    public boolean hasEatRewards() {
        checkEatEvent();
        if (eventType != null) {
            return eventType.equals("eat");
        } else {
            return false;
        }
    }

    @Override
    public boolean hasFishRewards() {
        checkFishEvent();
        return fishRewards != null && !fishRewards.isEmpty();
    }

    @Override
    public boolean hasSellRewards() {
        checkSellEvent();
        return sellRewards != null && !sellRewards.isEmpty();
    }

    @Override
    public boolean hasIntRewards() {
        checkIntEvent();
        if (eventType != null) {
            return eventType.equals("int");
        } else {
            return false;
        }
    }

    // checks if the config contains a message to be displayed when the fish is fished
    private void checkMessage() {
        String msg = section.getString("message");

        if (msg == null) {
            return;
        }
        if (fisherman == null) {
            return;
        }
        Player player = Bukkit.getPlayer(fisherman);
        if (player != null) {
            EMFSingleMessage.fromString(msg).send(player);
        }
    }

    private void checkEffects() {
        String effectConfig = section.getString("effect");

        // if the config doesn't have an effect stated to be given
        if (effectConfig == null) {
            return;
        }

        // Check if fisherman is null
        if (this.fisherman == null) {
            return;
        }
        // Check if the requested player is null
        Player player = Bukkit.getPlayer(this.fisherman);
        if (player == null) {
            return;
        }

        PotionEffect effect = FishUtils.getPotionEffect(effectConfig);
        if (effect == null) {
            Logging.warn(effectConfig + " is not a valid potion effect for fish: " + getName());
            return;
        }

        EvenMoreFish.getScheduler().runTask(player, () -> player.addPotionEffect(effect));
    }

    // prepares it to be given to the player
    @Override
    public void init() {
        generateSize();
        checkMessage();
        checkEffects();
    }

    /**
     * From the new method of fetching the lore, where the admin specifies exactly how they want the lore to be set up,
     * letting them modify the order, add a twist to how they want extra details and so on.
     * <p>
     * It goes through each line of the Messages' getFishLoreFormat, if the line is just {fish_lore} then it gets replaced
     * with a fish's lore value, if not then nothing is done.
     *
     * @return A lore to be used by fetching data from the old messages.yml set-up.
     */
    private List<Component> getFishLore() {
        List<String> loreOverride = section.getStringList("lore-override");
        EMFListMessage newLoreLine;
        if (!loreOverride.isEmpty()) {
            newLoreLine = EMFListMessage.fromStringList(loreOverride);
        } else {
            newLoreLine = ConfigMessage.FISH_LORE.getMessage().toListMessage();
        }

        OfflinePlayer fishermanPlayer = getFishermanPlayer();

        List<String> fishLore = factory.getLore().getConfiguredValue();
        EMFListMessage fishLoreReplacement = (fishLore == null || fishLore.isEmpty()) ? EMFListMessage.empty() : EMFListMessage.fromStringList(fishLore);
        newLoreLine.setVariableWithListInsertion("{fish_lore}", fishLoreReplacement);

        if (!disableFisherman && fishermanPlayer != null) {
            EMFMessage message = ConfigMessage.FISHERMAN_LORE.getMessage();
            message.setRelevantPlayer(fishermanPlayer);
            newLoreLine.setVariableWithListInsertion("{fisherman_lore}", message.toListMessage());
        } else {
            newLoreLine.setVariableWithListInsertion("{fisherman_lore}", EMFListMessage.empty());
        }

        if (length > 0) {
            newLoreLine.setVariableWithListInsertion("{length_lore}", ConfigMessage.LENGTH_LORE.getMessage().toListMessage());
            newLoreLine.setLength(Float.toString(length));
        } else {
            newLoreLine.setVariableWithListInsertion("{length_lore}", EMFListMessage.empty());
        }

        newLoreLine.setRarity(this.rarity.getLorePrep());

        if (disableFisherman || fishermanPlayer == null) {
            return newLoreLine.getComponentListMessage();
        } else {
            return newLoreLine.getComponentListMessage(fishermanPlayer);
        }
    }

    private void checkEatEvent() {
        if (this.actionRewards != null) {
            return;
        }
        List<String> configRewards = section.getStringList("eat-event");
        // Checks if the player has actually set reward for an eat event
        if (!configRewards.isEmpty()) {
            this.eventType = "eat";
            this.actionRewards = new ArrayList<>();

            // Translates all the reward into Reward objects and adds them to the fish.
            configRewards.forEach(reward -> {
                reward = parseEventPlaceholders(reward);
                this.actionRewards.add(new Reward(reward));
            });
        }
    }

    private void checkFishEvent() {
        if (this.fishRewards != null) {
            return;
        }
        fishRewards = new ArrayList<>();
        List<String> configRewards = section.getStringList("catch-event");
        if (!configRewards.isEmpty()) {
            // Translates all the reward into Reward objects and adds them to the fish.
            configRewards.forEach(reward -> {
                reward = parseEventPlaceholders(reward);
                this.fishRewards.add(new Reward(reward));
            });
        }
    }

    private void checkSellEvent() {
        if (this.sellRewards != null) {
            return;
        }
        sellRewards = new ArrayList<>();
        List<String> configRewards = section.getStringList("sell-event");
        if (!configRewards.isEmpty())  {
            configRewards.forEach(reward -> {
                reward = parseEventPlaceholders(reward);
                this.sellRewards.add(new Reward(reward));
            });
        }
    }

    private void checkIntEvent() {
        if (this.actionRewards != null) {
            return;
        }
        List<String> configRewards = section.getStringList("interact-event");
        // Checks if the player has actually set reward for an interact event
        if (!configRewards.isEmpty()) {
            this.eventType = "int";
            actionRewards = new ArrayList<>();

            // Translates all the reward into Reward objects and adds them to the fish.
            configRewards.forEach(reward -> {
                reward = parseEventPlaceholders(reward);
                this.actionRewards.add(new Reward(reward));
            });
        }
    }

    /**
     * Checks if the fish has silent: true enabled, which stops the "You caught ... fish" from being broadcasted to anyone.
     */
    @Override
    public void checkSilent() {
        this.silent = section.getBoolean("silent", false);
    }

    @Override
    public @NotNull Fish createCopy() {
        return create(rarity, section);
    }

    @Override
    public boolean hasFishermanDisabled() {
        return disableFisherman;
    }

    @Override
    public @Nullable UUID getFisherman() {
        return fisherman;
    }

    @Override
    public void setFisherman(@Nullable UUID fisherman) {
        this.fisherman = fisherman;
    }

    @Override
    public boolean isCompExemptFish() {
        return isCompExemptFish;
    }

    @Override
    public void setCompExemptFish(boolean compExemptFish) {
        isCompExemptFish = compExemptFish;
    }

    @Override
    public double getSetWorth() {
        return setWorth;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull Rarity getRarity() {
        return rarity;
    }

    @Override
    public float getLength() {
        return length;
    }

    @Override
    public void setLength(Float length) {
        this.length = length == null ? -1 : length;
    }

    @Override
    public @NotNull List<Reward> getActionRewards() {
        checkIntEvent();
        checkEatEvent();
        return actionRewards == null ? new ArrayList<>() : actionRewards;
    }

    @Override
    public @NotNull List<Reward> getFishRewards() {
        checkFishEvent();
        return fishRewards == null ? new ArrayList<>() : fishRewards;
    }

    @Override
    public @NotNull List<Reward> getSellRewards() {
        checkSellEvent();
        return sellRewards == null ? new ArrayList<>() : sellRewards;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @NotNull
    public EMFSingleMessage getDisplayName() {
        if (displayName == null) {
            return rarity.format(name);
        }
        return rarity.format(displayName);
    }

    public ItemFactory getFactory() {
        return factory;
    }

    @Override
    public @NotNull Requirement getRequirement() {
        return this.requirement;
    }

    @Override
    public void setRequirement(@NotNull Requirement requirement) {
        this.requirement = requirement;
    }

    @Override
    public boolean isWasBaited() {
        return wasBaited;
    }

    @Override
    public void setWasBaited(boolean wasBaited) {
        this.wasBaited = wasBaited;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }

    @Override
    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    private String parseEventPlaceholders(String rewardString) {

        // {length} Placeholder
        rewardString = rewardString.replace("{length}", String.valueOf(length));

        // {rarity} Placeholder
        String rarityReplacement = "";
        if (rarity != null) {
            rarityReplacement = rarity.getId();
        }
        rewardString = rewardString.replace("{rarity}", rarityReplacement);

        // {displayname} Placeholder
        String displayNameReplacement = "";
        if (displayName != null) {
            displayNameReplacement = displayName;
        }
        rewardString = rewardString.replace("{displayname}", displayNameReplacement);

        // {name} Placeholder
        String nameReplacement = "";
        if (name != null) {
            nameReplacement = name;
        }
        rewardString = rewardString.replace("{name}", nameReplacement);

        return rewardString;
    }

    @Override
    public @NotNull CatchType getCatchType() {
        String typeStr = section.getString("catch-type");
        if (typeStr == null) {
            return rarity.getCatchType();
        }
        CatchType catchType = FishUtils.getEnumValue(CatchType.class, typeStr);
        if (catchType == null) {
            return rarity.getCatchType();
        }
        return catchType;
    }

    @Override
    public boolean getShowInJournal() {
        return showInJournal;
    }

    @Override
    public void setShowInJournal(boolean showInJournal) {
        this.showInJournal = showInJournal;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Fish fish)) {
            return false;
        }
        // Check if the rarity and name match.
        return this.getRarity().equals(fish.getRarity()) && this.getName().equals(fish.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRarity(), getName());
    }

}