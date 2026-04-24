package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.baits.IBait;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.baits.configs.BaitFileUpdates;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.baits.model.ApplicationResult;
import com.oheers.fish.baits.model.BaitData;
import com.oheers.fish.baits.model.FishChance;
import com.oheers.fish.baits.model.RarityChance;
import com.oheers.fish.baits.model.WeightModifier;
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
import net.kyori.adventure.text.Component;
import com.oheers.fish.utils.sort.Sortable;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaitHandler extends ConfigBase implements IBait, Sortable {
    private final @NotNull String id;
    private BaitData baitData;
    private ItemFactory itemFactory;
    private boolean warnedLegacyFormat;

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

        this.fishManager = fishManager;
        this.mainConfig = mainConfig;
        this.id = validateAndGetId();
        this.baitData = loadBaitData();

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
        Map<Rarity, WeightModifier> rarityModifiers = resolveRarityModifiers();
        Map<Fish, WeightModifier> fishModifiers = resolveFishModifiers();
        List<Rarity> rarities = List.copyOf(rarityModifiers.keySet());
        List<Fish> fish = List.copyOf(fishModifiers.keySet());
        return new BaitData(
                id,
                getConfig().getString("item.displayname", this.id),
                rarities,
                fish,
                rarityModifiers,
                fishModifiers,
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

    private @NotNull Map<Rarity, WeightModifier> resolveRarityModifiers() {
        final Section rarityModifiers = getConfig().getSection("rarity-modifiers");
        if (rarityModifiers != null) {
            return parseRarityModifiers(rarityModifiers);
        }

        final List<String> legacyRarities = getConfig().getStringList("rarities");
        if (legacyRarities.isEmpty()) {
            return Map.of();
        }

        warnLegacyFormat();
        final WeightModifier legacyModifier = WeightModifier.multiply(mainConfig.getBaitBoostRate());
        final Map<Rarity, WeightModifier> resolved = new LinkedHashMap<>();
        for (String rarityName : legacyRarities) {
            final Rarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
                logger.warning("Invalid rarity '" + rarityName + "' found in bait " + getId() + ".");
                continue;
            }
            resolved.put(rarity, legacyModifier);
        }
        return Map.copyOf(resolved);
    }

    private @NotNull Map<Rarity, WeightModifier> parseRarityModifiers(@NotNull Section section) {
        final Map<Rarity, WeightModifier> resolved = new LinkedHashMap<>();
        for (String rarityName : section.getRoutesAsStrings(false)) {
            final Rarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
                logger.warning("Invalid rarity '" + rarityName + "' found in bait " + getId() + ".");
                continue;
            }

            try {
                resolved.put(rarity, WeightModifier.parse(section.get(rarityName)));
            } catch (IllegalArgumentException exception) {
                logger.warning(exception.getMessage());
            }
        }
        return Map.copyOf(resolved);
    }

    private @NotNull Map<Fish, WeightModifier> resolveFishModifiers() {
        final Section fishModifiers = getConfig().getSection("fish-modifiers");
        if (fishModifiers != null) {
            return parseFishModifiers(fishModifiers);
        }

        final Section fishSection = getConfig().getSection("fish");
        if (fishSection == null) {
            EvenMoreFish.getInstance().debug("Fish section was null in bait. Returning empty list..");
            return Map.of();
        }

        warnLegacyFormat();
        final Map<Fish, WeightModifier> resolved = new LinkedHashMap<>();
        final WeightModifier legacyModifier = WeightModifier.multiply(mainConfig.getBaitBoostRate());
        for (String rarityName : fishSection.getRoutesAsStrings(false)) {
            final Rarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
                logger.warning("Invalid rarity '" + rarityName + "' found in legacy fish config for bait " + getId() + ".");
                continue;
            }
            for (String fishName : getConfig().getStringList("fish." + rarityName)) {
                final Fish fish = FishManager.getInstance().getFish(rarity.getId(), fishName);
                if (fish == null) {
                    logger.warning("Invalid fish '" + fishName + "' found under rarity '" + rarityName + "' in bait " + getId() + ".");
                    continue;
                }
                resolved.put(fish, legacyModifier);
            }
        }
        return Map.copyOf(resolved);
    }

    private @NotNull Map<Fish, WeightModifier> parseFishModifiers(@NotNull Section section) {
        final Map<Fish, WeightModifier> resolved = new LinkedHashMap<>();
        for (String rarityName : section.getRoutesAsStrings(false)) {
            final Section raritySection = section.getSection(rarityName);
            if (raritySection == null) {
                logger.warning("Invalid fish-modifiers section '" + rarityName + "' in bait " + getId() + ".");
                continue;
            }

            final Rarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
                logger.warning("Invalid rarity '" + rarityName + "' found in fish-modifiers for bait " + getId() + ".");
                continue;
            }

            for (String fishName : raritySection.getRoutesAsStrings(false)) {
                final Fish fish = FishManager.getInstance().getFish(rarity.getId(), fishName);
                if (fish == null) {
                    logger.warning("Invalid fish '" + fishName + "' found under rarity '" + rarityName + "' in bait " + getId() + ".");
                    continue;
                }

                try {
                    resolved.put(fish, WeightModifier.parse(raritySection.get(fishName)));
                } catch (IllegalArgumentException exception) {
                    logger.warning(exception.getMessage());
                }
            }
        }
        return Map.copyOf(resolved);
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
        Rarity selectedRarity = selectRarityWithModifiers(player);
        Fish selectedFish = selectFishFromRarity(selectedRarity, player, location);

        processBaitUsage(player, selectedRarity, selectedFish);

        return selectedFish;
    }

    private @NotNull Map<Rarity, WeightModifier> getRarityModifiers() {
        return baitData.rarityModifiers();
    }

    private @NotNull Map<Fish, WeightModifier> getFishModifiers() {
        return baitData.fishModifiers();
    }

    private @Nullable Rarity selectRarityWithModifiers(@NotNull Player player) {
        return fishManager.getWeightedRarity(
            player,
            Set.copyOf(fishManager.getRarityMap().values()),
            this::getEffectiveRarityWeight,
            null
        );
    }

    private @Nullable Fish selectFishFromRarity(@Nullable Rarity rarity, @NotNull Player player, @NotNull Location location) {
        if (rarity == null) {
            return null;
        }
        return fishManager.getWeightedFish(
            rarity,
            location,
            player,
            this::getEffectiveFishWeight,
            true,
            null,
            null
        );
    }

    private double getEffectiveRarityWeight(@NotNull Rarity rarity) {
        return getRarityModifiers().getOrDefault(rarity, WeightModifier.IDENTITY).apply(rarity.getWeight());
    }

    private double getEffectiveFishWeight(@NotNull Fish fish) {
        return getFishModifiers().getOrDefault(fish, WeightModifier.IDENTITY).apply(FishManager.getBaseFishWeight(fish));
    }

    private void processBaitUsage(@NotNull Player player, @Nullable Rarity rarity, @Nullable Fish fish) {
        if (fish == null) {
            return;
        }

        fish.setWasBaited(true);
        fish.setFisherman(player);

        if (shouldAlertUsage(rarity, fish)) {
            alertUsage(player);
        }
    }

    private boolean shouldAlertUsage(@Nullable Rarity rarity, @NotNull Fish fish) {
        return (rarity != null && hasRarityModifier(rarity)) || hasFishModifier(fish);
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
        fish.setFisherman(player);

        // Only consume bait if this bait actually affected the catch
        if (!shouldConsumeBait(fish)) {
            return;
        }

        try {
            ApplicationResult result = BaitNBTManager.applyBaitedRodNBT(fishingRod, this, -1); //updates the state of the rod, if the correct fish was baited

            fishingRod.setItemMeta(result.fishingRod().getItemMeta());
            EvenMoreFish.getInstance().getMetricsManager().incrementBaitsUsed(1);
        } catch (MaxBaitReachedException | MaxBaitsReachedException e) {
            logger.log(Level.WARNING, e.getMessage());
            player.sendMessage(e.getConfigMessage().getMessage().getComponentMessage(player));
        } catch (NullPointerException exception) {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
        }
    }

    private boolean shouldConsumeBait(@NotNull Fish fish) {
        return hasRarityModifier(fish.getRarity()) || hasFishModifier(fish);
    }

    private boolean hasRarityModifier(@NotNull Rarity rarity) {
        return getRarityModifiers().containsKey(rarity);
    }

    private boolean hasFishModifier(@NotNull Fish fish) {
        return getFishModifiers().containsKey(fish);
    }

    private boolean hasModifiersInRarity(@NotNull Rarity rarity) {
        return hasRarityModifier(rarity) || getFishModifiers().keySet().stream().anyMatch(fish -> fish.getRarity().equals(rarity));
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
        message.setBait(this);
        message.send(player);
    }

    @Override
    public double getWeight() {
        // TODO allow baits to have weight.
        return 0;
    }

    /**
     * @return The name identifier of the bait.
     */
    @Override
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
     * @return The displayname setting for the bait.
     */
    @Override
    public @NonNull String getDisplayName() {
        return baitData.displayName();
    }


    @Override
    public void reload(@NotNull File configFile) {
        super.reload(configFile);
        if (fishManager == null || mainConfig == null || id == null) {
            return;
        }
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
        if (fishManager == null || mainConfig == null || id == null) {
            return;
        }
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

    private void warnLegacyFormat() {
        if (warnedLegacyFormat) {
            return;
        }
        warnedLegacyFormat = true;
        logger.warning("Bait file '" + getFileName() + "' is using the old bait format. Please migrate it to 'rarity-modifiers' and/or 'fish-modifiers'.");
    }

    public @NotNull List<Component> createDebugMessages(@NotNull Player player) {
        return createDebugMessages(player, player.getLocation());
    }

    public @NotNull List<Component> createDebugMessages(@NotNull Player player, @NotNull Location location) {
        final List<Component> messages = new ArrayList<>();
        final List<RarityChance> rarityChances = calculateRarityChances(player, location);

        messages.add(Component.text("Bait debug for " + getId() + " at " + formatLocation(location) + " on " + player.getName()));

        if (rarityChances.isEmpty()) {
            messages.add(Component.text("No eligible rarities matched this player and location."));
            return messages;
        }

        messages.add(Component.text("Rarity chances:"));
        for (RarityChance rarityChance : rarityChances) {
            messages.add(Component.text(" - %s: %s [base=%s, effective=%s, modifier=%s]".formatted(
                rarityChance.rarity().getId(),
                formatPercent(rarityChance.chance()),
                formatNumber(rarityChance.baseWeight()),
                formatNumber(rarityChance.effectiveWeight()),
                rarityChance.modifier().describe()
            )));
        }

        final List<RarityChance> modifiedRarities = rarityChances.stream()
            .filter(rarityChance -> hasModifiersInRarity(rarityChance.rarity()))
            .toList();

        if (modifiedRarities.isEmpty()) {
            messages.add(Component.text("This bait does not modify any currently eligible rarity or fish."));
            return messages;
        }

        messages.add(Component.text("Fish chances in affected rarities:"));
        for (RarityChance rarityChance : modifiedRarities) {
            messages.add(Component.text(" * " + rarityChance.rarity().getId()));
            for (FishChance fishChance : rarityChance.fishChances()) {
                messages.add(Component.text("   - %s: overall=%s, in-rarity=%s [base=%s, effective=%s, modifier=%s]".formatted(
                    fishChance.fish().getName(),
                    formatPercent(fishChance.overallChance()),
                    formatPercent(fishChance.conditionalChance()),
                    formatNumber(fishChance.baseWeight()),
                    formatNumber(fishChance.effectiveWeight()),
                    fishChance.modifier().describe()
                )));
            }
        }

        return messages;
    }

    private @NotNull List<RarityChance> calculateRarityChances(@NotNull Player player, @NotNull Location location) {
        final List<Rarity> availableRarities = fishManager.getAvailableRarities(
            player,
            Set.copyOf(fishManager.getRarityMap().values()),
            null
        );
        if (availableRarities.isEmpty()) {
            return List.of();
        }

        final Map<Rarity, Long> rarityCounts = availableRarities.stream()
            .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        final double totalRarityWeight = rarityCounts.entrySet().stream()
            .mapToDouble(entry -> getEffectiveRarityWeight(entry.getKey()) * entry.getValue())
            .sum();

        return rarityCounts.entrySet().stream()
            .map(entry -> buildRarityChance(entry.getKey(), player, location, totalRarityWeight, availableRarities.size(), entry.getValue()))
            .sorted(Comparator.comparingDouble(RarityChance::chance).reversed())
            .toList();
    }

    private @NotNull RarityChance buildRarityChance(@NotNull Rarity rarity,
                                                    @NotNull Player player,
                                                    @NotNull Location location,
                                                    double totalRarityWeight,
                                                    int totalCandidateCount,
                                                    long multiplicity) {
        final double baseWeight = rarity.getWeight() * multiplicity;
        final WeightModifier rarityModifier = getRarityModifiers().getOrDefault(rarity, WeightModifier.IDENTITY);
        final double effectiveWeight = rarityModifier.apply(rarity.getWeight()) * multiplicity;
        final double rarityChance = totalRarityWeight > 0.0D
            ? effectiveWeight / totalRarityWeight
            : (double) multiplicity / totalCandidateCount;
        final List<FishChance> fishChances = calculateFishChances(rarity, player, location, rarityChance);

        return new RarityChance(rarity, baseWeight, effectiveWeight, rarityChance, rarityModifier, fishChances);
    }

    private @NotNull List<FishChance> calculateFishChances(@NotNull Rarity rarity, @NotNull Player player, @NotNull Location location, double rarityChance) {
        final List<Fish> availableFish = fishManager.getAvailableFish(rarity, location, player, true, null, null);
        if (availableFish.isEmpty()) {
            return List.of();
        }

        final double totalFishWeight = availableFish.stream()
            .mapToDouble(this::getEffectiveFishWeight)
            .sum();

        return availableFish.stream()
            .map(fish -> {
                final double baseWeight = FishManager.getBaseFishWeight(fish);
                final WeightModifier modifier = getFishModifiers().getOrDefault(fish, WeightModifier.IDENTITY);
                final double effectiveWeight = modifier.apply(baseWeight);
                final double conditionalChance = totalFishWeight > 0.0D
                    ? effectiveWeight / totalFishWeight
                    : 1.0D / availableFish.size();
                return new FishChance(fish, baseWeight, effectiveWeight, conditionalChance, rarityChance * conditionalChance, modifier);
            })
            .sorted(Comparator.comparingDouble(FishChance::overallChance).reversed())
            .toList();
    }

    private @NotNull String formatLocation(@NotNull Location location) {
        final String world = location.getWorld() != null ? location.getWorld().getName() : "unknown";
        return "%s %.1f %.1f %.1f".formatted(world, location.getX(), location.getY(), location.getZ());
    }

    private @NotNull String formatPercent(double value) {
        return String.format(Locale.ROOT, "%.2f%%", value * 100.0D);
    }

    private @NotNull String formatNumber(double value) {
        return String.format(Locale.ROOT, "%.3f", value)
            .replaceAll("0+$", "")
            .replaceAll("\\.$", "");
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
    public @Nullable Economy getEconomy() {
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
        message.setBait(this);
        message.send(player);

        return true;
    }

}
