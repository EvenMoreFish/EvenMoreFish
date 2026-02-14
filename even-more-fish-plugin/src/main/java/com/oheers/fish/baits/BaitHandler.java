package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.baits.IBait;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.baits.configs.BaitFileUpdates;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.baits.model.ApplicationResult;
import com.oheers.fish.baits.model.BaitData;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaitHandler extends ConfigBase implements IBait {
    private final @NotNull String id;
    private BaitData baitData;
    private ItemFactory itemFactory;

    private static final double DEFAULT_BOOST_RATE = 1.0;
    private final Logger logger = EvenMoreFish.getInstance().getLogger();
    private final FishManager fishManager;
    private final MainConfig mainConfig;
    private final Economy economy;

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
    public BaitHandler(@NotNull File file, FishManager fishManager, MainConfig mainConfig) throws InvalidConfigurationException {
        super(file, EvenMoreFish.getInstance(), false);
        BaitFileUpdates.update(this);

        this.id = validateAndGetId();
        this.baitData = loadBaitData();

        this.fishManager = fishManager;
        this.mainConfig = mainConfig;

        this.economy = fetchEconomyInstance();

        this.itemFactory = new BaitItemFactory(
                baitData.id(),
                baitData.rarities(),
                baitData.fish(),
                getConfig()
        ).createFactory();
    }

    // Current required config: id
    private String validateAndGetId() throws InvalidConfigurationException {
        final String baitId = getConfig().getString("id");
        if (baitId == null) {
            logger.warning("Rarity invalid: 'id' missing in " + getFileName());
            throw new InvalidConfigurationException("An ID has not been found in " + getFileName() + ". Please correct this.");
        }

        return baitId;
    }

    /**
     * Fetches the economy instance that will belong to this bait.
     * If the bait is not purchasable, null is returned.
     * If there are no provided economy types, the global economy instance is returned.
     */
    private Economy fetchEconomyInstance() {
        // Bait cannot be purchased at all.
        if (getPurchasePrice() <= -1.0D || getPurchaseQuantity() <= 0) {
            return null;
        }

        List<String> typeStrings = getConfig().getStringList("purchase.economy-types");
        // No economy types specified, use global economy.
        if (typeStrings.isEmpty()) {
            return Economy.getInstance();
        }
        List<EconomyType> types = typeStrings.stream()
            .map(EMFRegistry.ECONOMY_TYPE::get)
            .filter(Objects::nonNull)
            .toList();
        // No valid economy types configured, warn console and return null.
        if (types.isEmpty()) {
            Logging.warn("No valid economy types found for bait: " + getId() + ". This bait will not be purchasable.");
            return null;
        }
        // Return new economy instance for specified types.
        return Economy.economy(types);
    }

    /**
     * This creates an item based on random settings in the yml files, adding things such as custom model data and glowing
     * effects.
     *
     * @return An item stack representing the bait object, with nbt.
     */
    @Override
    public @NotNull ItemStack create(@NotNull OfflinePlayer player) {
        return itemFactory.createItem(player.getUniqueId());
    }

    private BaitData loadBaitData() {
        List<Rarity> rarities = resolveRarity();
        List<Fish> fish = resolveFish();
        return new BaitData(
                id,
                getConfig().getString("item.displayname", this.id),
                rarities,
                fish,
                getConfig().getBoolean("disabled", false),
                getConfig().getBoolean("infinite", false),
                getConfig().getInt("max-applications", -1),
                getConfig().getInt("drop-quantity", 1),
                getConfig().getDouble("application-weight", 100.0),
                getConfig().getDouble("catch-weight", 100.0),
                getConfig().getBoolean("can-be-caught", true),
                getConfig().getBoolean("disable-use-alert", false)
        );
    }

    /**
     * @return All configured rarities from this bait's configuration.
     */
    @Override
    public @NotNull List<Rarity> getRarities() {
        return baitData.rarities();
    }

    private List<Rarity> resolveRarity() {
        List<String> rarityStrings = getConfig().getStringList("rarities");
        return rarityStrings.stream()
                .map(FishManager.getInstance()::getRarity)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Fish> resolveFish() {
        final Section fishSection = getConfig().getSection("fish");
        if (fishSection == null) {
            EvenMoreFish.getInstance().debug("Fish section was null in bait. Returning empty list..");
            return Collections.emptyList();
        }

        return getConfig().getSection("fish").getRoutesAsStrings(false).stream()
                .map(FishManager.getInstance()::getRarity)
                .filter(Objects::nonNull)
                .flatMap(rarity -> rarity.getFishList().stream())
                .filter(fish -> getConfig().getStringList("fish." + fish.getRarity().getId())
                        .contains(fish.getName()))
                .toList();
    }

    private @NotNull List<Fish> getFish() {
        return baitData.fish();
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
    @Override
    public @NotNull Fish chooseFish(@NotNull Player player, @NotNull Location location) {
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
                mainConfig.getBaitBoostRate(),
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

    @Override
    public void handleFish(@NotNull Player player, @NotNull IFish iFish, @NotNull ItemStack fishingRod) {
        if (!(iFish instanceof Fish fish)) {
            Logging.debug("Fish: " + iFish.getName() + " is not a Fish object, ignoring..");
            return;
        }
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
            if (result.getFishingRod() == null) {
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
        if (baitData.disableUseAlert()) {
            return;
        }

        EMFMessage message = ConfigMessage.BAIT_USED.getMessage();
        message.setBait(format(getDisplayName()));
        message.send(player);
    }

    /**
     * @return The name identifier of the bait.
     */
    @Override
    public @NotNull String getId() {
        return baitData.id();
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
     * @return The displayname setting for the bait.
     */
    @Override
    public @NonNull String getDisplayName() {
        return baitData.displayName();
    }


    @Override
    public void reload(@NotNull File configFile) {
        super.reload(configFile);
        this.baitData = loadBaitData();
        this.itemFactory = new BaitItemFactory(
                baitData.id(),
                baitData.rarities(),
                baitData.fish(),
                getConfig()
        ).createFactory();
    }

    @Override
    public void reload() {
        super.reload();
        this.baitData = loadBaitData();
        this.itemFactory = new BaitItemFactory(
            baitData.id(),
            baitData.rarities(),
            baitData.fish(),
            getConfig()
        ).createFactory();
    }

    public BaitData getBaitData() {
        return baitData;
    }

    // Bait Shop

    /**
     * Fetches the purchase price of the bait from the config.
     * Defaults to -1 to allow baits to be given for free.
     */
    @Override
    public double getPurchasePrice() {
        return getConfig().getDouble("purchase.price", -1.0D);
    }

    /**
     * Fetches the purchase quantity of the bait from the config.
     * Defaults to 0.
     */
    @Override
    public int getPurchaseQuantity() {
        return getConfig().getInt("purchase.quantity", 0);
    }

    /**
     * Fetches the economy that this bait is purchased with.
     */
    @Override
    public @NotNull Economy getEconomy() {
        return this.economy;
    }

    /**
     * Attempts to purchase the bait for the player.
     * @param player The player purchasing the bait.
     * @return True if the purchase was successful, false otherwise.
     */
    @Override
    public boolean attemptPurchase(@NotNull Player player) {
        if (economy == null || economy.isEmpty()) {
            ConfigMessage.BAIT_NOT_FOR_SALE.getMessage().send(player);
            return false;
        }
        double price = getPurchasePrice();
        if (price <= -1.0D) {
            ConfigMessage.BAIT_NOT_FOR_SALE.getMessage().send(player);
            return false;
        }
        int quantity = getPurchaseQuantity();
        if (quantity <= 0) {
            ConfigMessage.BAIT_NOT_FOR_SALE.getMessage().send(player);
            return false;
        }
        if (!economy.has(player, price)) {
            EMFMessage message = ConfigMessage.BAIT_CANNOT_AFFORD.getMessage();
            message.setVariable("{price}", economy.getWorthFormat(price, false));
            message.send(player);
            return false;
        }
        economy.withdraw(player, price, false);

        ItemStack baitItem = create(player);
        // Limit to the item's max stack size.
        int finalQuantity = Math.min(baitItem.getMaxStackSize(), quantity);
        baitItem.setAmount(finalQuantity);
        FishUtils.giveItem(baitItem, player);

        EMFMessage message = ConfigMessage.BAIT_PURCHASED.getMessage();
        message.setAmount(finalQuantity);
        message.setVariable("{price}", economy.getWorthFormat(price, false));
        message.setBait(getDisplayName());
        message.send(player);

        return true;
    }

}
