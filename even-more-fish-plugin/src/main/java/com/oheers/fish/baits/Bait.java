package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.baits.configs.BaitFileUpdates;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.items.configs.DisplayNameItemConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bait extends ConfigBase {
    private static final double DEFAULT_BOOST_RATE = 1.0;
    private final Logger logger = EvenMoreFish.getInstance().getLogger();
    private final FishManager fishManager;
    private final MainConfig mainConfig;
    private final ItemFactory itemFactory;

    private final @NotNull String id;

    private List<Rarity> cachedRarities;
    private List<Fish> cachedFish;

    /**
     * This represents a bait, which can be used to boost the likelihood that a certain fish or fish rarity appears from
     * the rod. All data is fetched from the config when the Bait object is created and then can be given out using
     * the create() method.
     * <p>
     * The plugin recognises the bait item from the create() method using NBT data, which can be applied using the
     * BaitNBTManager class, which handles all the NBT thingies.
     *
     * @param file The bait's config file
     */
    public Bait(@NotNull File file, FishManager fishManager, MainConfig mainConfig) throws InvalidConfigurationException {
        super(file, EvenMoreFish.getInstance(), false);
        BaitFileUpdates.update(this);
        performRequiredConfigChecks();

        final String configId = getConfig().getString("id");
        if (configId == null) {
            throw new InvalidConfigurationException("Missing 'id' in " + getFileName());
        }

        this.id = configId;
        this.fishManager = fishManager;
        this.mainConfig = mainConfig;
        ItemFactory factory = ItemFactory.itemFactory(getConfig());

        DisplayNameItemConfig displayNameConfig = factory.getDisplayName();
        displayNameConfig.setEnabled(true);
        displayNameConfig.setDefault("<yellow>" + this.id);

        factory.setFinalChanges(item -> {
            item.setAmount(getDropQuantity());
            item.editMeta(meta -> meta.lore(createBoostLore()));
            BaitNBTManager.applyBaitNBT(item, this.id);
        });
        this.itemFactory = factory;
    }

    // Current required config: id
    private void performRequiredConfigChecks() throws InvalidConfigurationException {
        if (getConfig().getString("id") == null) {
            logger.warning("Rarity invalid: 'id' missing in " + getFileName());
            throw new InvalidConfigurationException("An ID has not been found in " + getFileName() + ". Please correct this.");
        }
    }

    /**
     * This creates an item based on random settings in the yml files, adding things such as custom model data and glowing
     * effects.
     *
     * @return An item stack representing the bait object, with nbt.
     */
    public ItemStack create(@NotNull OfflinePlayer player) {
        return itemFactory.createItem(player.getUniqueId());
    }


    /**
     * @return All configured rarities from this bait's configuration.
     */
    public @NotNull List<Rarity> getRarities() {
        if (cachedRarities == null) {
            List<String> rarityStrings = getConfig().getStringList("rarities");
            this.cachedRarities = rarityStrings.stream()
                    .map(FishManager.getInstance()::getRarity)
                    .filter(Objects::nonNull)
                    .toList();
        }

        return cachedRarities;
    }

    private @NotNull List<Fish> getFish() {
        if (cachedFish == null) {
            final Section fishSection = getConfig().getSection("fish");
            if (fishSection == null) {
                EvenMoreFish.getInstance().debug("Fish section was null in bait. Returning empty list..");
                return Collections.emptyList();
            }

            this.cachedFish = getConfig().getSection("fish").getRoutesAsStrings(false).stream()
                    .map(FishManager.getInstance()::getRarity)
                    .filter(Objects::nonNull)
                    .flatMap(rarity -> rarity.getFishList().stream())
                    .filter(fish -> getConfig().getStringList("fish." + fish.getRarity().getId())
                            .contains(fish.getName()))
                    .toList();
        }

        return cachedFish;
    }

    /**
     * This fetches the boost's lore from the config and inserts the boost-rates into the {boosts} variable. This needs
     * to be called after the bait theme is set and the boosts have been initialized, since it uses those variables.
     *
     * @return A list of formatted Adventure components for the bait's lore
     */
    private @NotNull List<Component> createBoostLore() {
        final EMFListMessage lore = getBaseLoreTemplate();

        // Set dynamic variables
        lore.setVariable("{boosts}", createBoostsVariable());
        lore.setVariable("{lore}", createItemLoreVariable().get());
        lore.setVariable("{bait_theme}", Component.empty());

        return lore.getComponentListMessage();
    }

    private EMFListMessage getBaseLoreTemplate() {
        return ConfigMessage.BAIT_BAIT_LORE.getMessage().toListMessage();
    }

    private @NotNull EMFMessage createBoostsVariable() {
        EMFMessage boostsMessage = EMFSingleMessage.empty();
        appendRarityBoosts(boostsMessage);
        appendFishBoosts(boostsMessage);
        return boostsMessage;
    }

    private void appendRarityBoosts(EMFMessage message) {
        List<Rarity> rarities = getRarities();
        if (rarities.isEmpty()) return;

        ConfigMessage boostMessage = rarities.size() > 1
                ? ConfigMessage.BAIT_BOOSTS_RARITIES
                : ConfigMessage.BAIT_BOOSTS_RARITY;

        message.appendMessage(boostMessage.getMessage());
        message.setAmount(Integer.toString(rarities.size()));
    }

    private void appendFishBoosts(EMFMessage message) {
        List<Fish> fish = getFish();
        if (fish.isEmpty()) return;

        message.appendMessage(ConfigMessage.BAIT_BOOSTS_FISH.getMessage());
        message.setAmount(Integer.toString(fish.size()));
    }

    @Contract(pure = true)
    private @NotNull Supplier<EMFListMessage> createItemLoreVariable() {
        return () -> EMFListMessage.fromStringList(
                itemFactory.getLore().getConfiguredValue()
        );
    }

    public boolean isDisabled() {
        return getConfig().getBoolean("disabled", false);
    }

    public boolean isInfinite() {
        return getConfig().getBoolean("infinite", false);
    }

    public boolean shouldDisableUseAlert() {
        return getConfig().getBoolean("disable-use-alert", false);
    }


    /**
     * This chooses a random fish based on the set boosts of the bait's config.
     * <p>
     * If there's rarities in the rarityList, choose a rarity first, applying multiplication of weight.
     * If there's no rarities in the server list: *
     * Check if there's any fish in the bait for this rarity, boost them. REMOVE BAIT
     * If the rarity chosen was not boosted, check if any fish are in this rarity and boost them. REMOVE BAIT
     * <p>
     * * Pick a rarity, boosting all rarities referenced in the fishList, from that rarity choose a random fish, if that
     * fish is within the fishList then give it to the player as the fish roll. REMOVE BAIT
     * <p>
     * TLDR: Choose a fish based on the bait's configured boosts, applying probability modifications.
     *
     * @return The selected fish, or null if no valid fish was found
     */
    public Fish chooseFish(@NotNull Player player, @NotNull Location location) {
        // Step 1: Determine which rarities are boosted by this bait
        Set<Rarity> boostedRarities = determineBoostedRarities();

        // Step 2: Select a rarity considering the boosts
        Rarity selectedRarity = selectRarityWithBoosts(player, boostedRarities);

        // Step 3: Select a fish from the chosen rarity
        Fish selectedFish = selectFishFromRarity(selectedRarity, player, location);

        // Step 4: Handle bait consumption and metadata
        processBaitUsage(player, selectedRarity, selectedFish);

        return selectedFish;
    }

    private @NotNull Set<Rarity> determineBoostedRarities() {
        Set<Rarity> boosted = new HashSet<>(getRarities());
        getFish().stream()
                .map(Fish::getRarity)
                .forEach(boosted::add);
        return boosted;
    }

    private Rarity selectRarityWithBoosts(Player player, Set<Rarity> boostedRarities) {
        return fishManager.getRandomWeightedRarity(
                player,
                getBoostRate(),
                boostedRarities,
                Set.copyOf(fishManager.getRarityMap().values()),
                null
        );
    }

    private Fish selectFishFromRarity(Rarity rarity, Player player, Location location) {
        List<Fish> eligibleFish = getEligibleFishForRarity(rarity);
        double boostRate = shouldApplyBoost(rarity) ? mainConfig.getBaitBoostRate() : DEFAULT_BOOST_RATE;

        return fishManager.getFish(
                rarity,
                location,
                player,
                boostRate,
                eligibleFish,
                true,
                null,
                null
        );
    }

    private @Nullable List<Fish> getEligibleFishForRarity(Rarity rarity) {
        if (getFish().isEmpty()) {
            return null; // Let fishManager use all fish in rarity
        }

        // If this rarity has specifically boosted fish, use them
        if (getFish().stream().anyMatch(f -> f.getRarity().equals(rarity))) {
            return getFish();
        }

        // Otherwise use all fish from this rarity
        return rarity.getFishList();
    }

    private boolean shouldApplyBoost(Rarity rarity) {
        return getRarities().contains(rarity) ||
                getFish().stream().anyMatch(f -> f.getRarity().equals(rarity));
    }

    private void processBaitUsage(Player player, Rarity rarity, Fish fish) {
        if (fish == null) {
            return;
        }

        fish.setWasBaited(true);
        fish.setFisherman(player.getUniqueId());

        if (shouldAlertUsage(rarity, fish)) {
            alertUsage(player);
        }
    }

    private boolean shouldAlertUsage(Rarity rarity, Fish fish) {
        // Alert if either:
        // 1. The rarity was directly boosted, or
        // 2. The specific fish was boosted
        return getRarities().contains(rarity) ||
                (!getFish().isEmpty() && getFish().contains(fish));
    }

    public void handleFish(@NotNull Player player, @NotNull Fish fish, @NotNull ItemStack fishingRod) {
        if (!fish.isWasBaited()) {
            EvenMoreFish.getInstance().debug("Fish: %s was not baited, ignoring..".formatted(FishRarityKey.of(fish)));
            return;
        }

        EvenMoreFish.getInstance().debug("Fish: %s was baited".formatted(FishRarityKey.of(fish)));
        fish.setFisherman(player.getUniqueId());

        // Only consume bait if this bait actually affected the catch
        if (!shouldConsumeBait(fish)) {
            return;
        }

        try {
            ApplicationResult result = BaitNBTManager.applyBaitedRodNBT(fishingRod, this, -1); //updates the state of the rod, if the correct fish was baited
            if (result == null || result.getFishingRod() == null) {
                return;
            }

            fishingRod.setItemMeta(result.getFishingRod().getItemMeta());
            EvenMoreFish.getInstance().getMetricsManager().incrementBaitsUsed(1);
        } catch (MaxBaitReachedException | MaxBaitsReachedException e) {
            logger.log(Level.WARNING, e.getMessage());
            player.sendMessage(e.getConfigMessage().getMessage().getComponentMessage(player));
        } catch (NullPointerException exception) {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
        }
    }

    private boolean shouldConsumeBait(@NotNull Fish fish) {
        // Consume bait if:
        // 1. The fish's rarity is directly boosted by this bait, or
        // 2. The specific fish is in this bait's fish list
        return getRarities().contains(fish.getRarity()) || getFish().contains(fish);
    }

    /**
     * Lets the player know that they've used one of their baits. Uses the value in messages.yml under "bait-use".
     *
     * @param player The player that's used the bait.
     */

    private void alertUsage(Player player) {
        if (shouldDisableUseAlert()) {
            return;
        }

        EMFMessage message = ConfigMessage.BAIT_USED.getMessage();
        message.setBait(format(getDisplayName()));
        message.send(player);
    }

    /**
     * @return How likely the bait is to apply out of all others applied baits.
     */
    public double getApplicationWeight() {
        return getConfig().getDouble("application-weight");
    }

    /**
     * @return How likely the bait is to appear out of all other baits when caught.
     */
    public double getCatchWeight() {
        return getConfig().getDouble("catch-weight");
    }

    /**
     * @return The x multiplier of a chance to get one of the fish in the bait's fish to appear.
     */
    public double getBoostRate() {
        return mainConfig.getBaitBoostRate();
    }

    /**
     * @return The name identifier of the bait.
     */
    public @NotNull String getId() {
        return id;
    }

    public @NotNull EMFSingleMessage getFormat() {
        String format = getConfig().getString("format", "<yellow>{name}");
        return EMFSingleMessage.fromString(format);
    }

    public @NotNull EMFSingleMessage format(@NotNull String name) {
        EMFSingleMessage message = getFormat();
        message.setVariable("{name}", name);
        return message;
    }

    /**
     * @return How many of this bait can be applied to a fishing rod.
     */
    public int getMaxApplications() {
        return getConfig().getInt("max-baits");
    }

    public int getDropQuantity() {
        return getConfig().getInt("drop-quantity", 1);
    }

    /**
     * @return The displayname setting for the bait.
     */
    public String getDisplayName() {
        return getConfig().getString("item.displayname", this.id);
    }

    public boolean getCanBeCaught() {
        return getConfig().getBoolean("can-be-caught", true);
    }

    @Override
    public void reload(@NotNull File configFile) {
        super.reload(configFile);
        this.cachedFish = null;
        this.cachedRarities = null;
    }

    @Override
    public void reload() {
        super.reload();
        this.cachedFish = null;
        this.cachedRarities = null;
    }
}
