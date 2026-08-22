package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.baits.IBait;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.api.fishing.FishingType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.sort.Sortable;
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
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.recipe.EMFRecipe;
import com.oheers.fish.recipe.RecipeUtil;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BaitHandler extends ConfigBase implements IBait, Sortable {

    private final @NonNull String id;
    private BaitData baitData;
    private ItemFactory itemFactory;
    private boolean warnedLegacyFormat;

    private final Logger logger = EvenMoreFish.getInstance().getLogger();
    private final FishManager fishManager;
    private final MainConfig mainConfig;
    private final Economy economy;

    private final EMFRecipe<?> recipe;

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
    public BaitHandler(@NonNull File file, FishManager fishManager, MainConfig mainConfig) throws InvalidConfigurationException {
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

        this.recipe = loadRecipe();
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
    public @NonNull ItemStack create(@NonNull OfflinePlayer player) {
        return itemFactory.createItem(player.getUniqueId());
    }

    /**
     * This creates an item based on random settings in the yml files, adding things such as custom model data and glowing
     * effects.
     *
     * @return An item stack representing the bait object, with nbt.
     */
    @Override
    public @NonNull ItemStack create() {
        return itemFactory.createItem();
    }

    private BaitData loadBaitData() {
        Map<IRarity, WeightModifier> rarityModifiers = resolveRarityModifiers();
        Map<IFish, WeightModifier> fishModifiers = resolveFishModifiers();
        List<IRarity> rarities = List.copyOf(rarityModifiers.keySet());
        List<IFish> fish = List.copyOf(fishModifiers.keySet());
        return new BaitData(
                id,
                getConfig().getString("item.displayname", this.id),
                rarities,
                fish,
                rarityModifiers,
                fishModifiers,
                getConfig().getBoolean("disabled", false),
                getConfig().getBoolean("infinite", false),
                resolveMaxBaits(getConfig()),
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
    public @NonNull List<IRarity> getRarities() {
        return baitData.rarities();
    }

    private @NonNull Map<IRarity, WeightModifier> resolveRarityModifiers() {
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
        final Map<IRarity, WeightModifier> resolved = new LinkedHashMap<>();
        for (String rarityName : legacyRarities) {
            final IRarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
                logger.warning("Invalid rarity '" + rarityName + "' found in bait " + getId() + ".");
                continue;
            }
            resolved.put(rarity, legacyModifier);
        }
        return Map.copyOf(resolved);
    }

    private @NonNull Map<IRarity, WeightModifier> parseRarityModifiers(@NonNull Section section) {
        final Map<IRarity, WeightModifier> resolved = new LinkedHashMap<>();
        for (String rarityName : section.getRoutesAsStrings(false)) {
            final IRarity rarity = FishManager.getInstance().getRarity(rarityName);
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

    private @NonNull Map<IFish, WeightModifier> resolveFishModifiers() {
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
        final Map<IFish, WeightModifier> resolved = new LinkedHashMap<>();
        final WeightModifier legacyModifier = WeightModifier.multiply(mainConfig.getBaitBoostRate());
        for (String rarityName : fishSection.getRoutesAsStrings(false)) {
            final IRarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
                logger.warning("Invalid rarity '" + rarityName + "' found in legacy fish config for bait " + getId() + ".");
                continue;
            }
            for (String fishName : getConfig().getStringList("fish." + rarityName)) {
                final IFish fish = rarity.getFish(fishName);
                if (fish == null) {
                    logger.warning("Invalid fish '" + fishName + "' found under rarity '" + rarityName + "' in bait " + getId() + ".");
                    continue;
                }
                resolved.put(fish, legacyModifier);
            }
        }
        return Map.copyOf(resolved);
    }

    private @NonNull Map<IFish, WeightModifier> parseFishModifiers(@NonNull Section section) {
        final Map<IFish, WeightModifier> resolved = new LinkedHashMap<>();
        for (String rarityName : section.getRoutesAsStrings(false)) {
            final Section raritySection = section.getSection(rarityName);
            if (raritySection == null) {
                logger.warning("Invalid fish-modifiers section '" + rarityName + "' in bait " + getId() + ".");
                continue;
            }

            final IRarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
                logger.warning("Invalid rarity '" + rarityName + "' found in fish-modifiers for bait " + getId() + ".");
                continue;
            }

            for (String fishName : raritySection.getRoutesAsStrings(false)) {
                final IFish fish = rarity.getFish(fishName);
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

    private @NonNull List<IFish> getFish() {
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
    public @NonNull IFish chooseFish(@NonNull Player player, @NonNull Location location) {
        RequirementContext context = new RequirementContext(
            player.getWorld(),
            player.getLocation(),
            player,
            null,
            null,
            FishingType.VANILLA
        );
        return chooseFish(player, location, context, null);
    }

    public @NonNull IFish chooseFish(@NonNull Player player, @NonNull Location location, @NonNull RequirementContext requirementContext, @Nullable CustomRod customRod) {
        IRarity selectedRarity = selectRarityWithModifiers(player, requirementContext, customRod);
        IFish selectedFish = selectFishFromRarity(selectedRarity, player, location, customRod);

        processBaitUsage(player, selectedRarity, selectedFish);

        return selectedFish;
    }

    private @NonNull Map<IRarity, WeightModifier> getRarityModifiers() {
        return baitData.rarityModifiers();
    }

    private @NonNull Map<IFish, WeightModifier> getFishModifiers() {
        return baitData.fishModifiers();
    }

    private @Nullable IRarity selectRarityWithModifiers(@NonNull Player player, @NonNull RequirementContext requirementContext, @Nullable CustomRod customRod) {
        return fishManager.getWeightedRarity(
            player,
            Set.copyOf(fishManager.getRarityMap().values()),
            this::getEffectiveRarityWeight,
            customRod,
            requirementContext
        );
    }

    private @Nullable IFish selectFishFromRarity(@Nullable IRarity rarity, @NonNull Player player, @NonNull Location location, @Nullable CustomRod customRod) {
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
            customRod
        );
    }

    private double getEffectiveRarityWeight(@NonNull IRarity rarity) {
        return getRarityModifiers().getOrDefault(rarity, WeightModifier.IDENTITY).apply(rarity.getWeight());
    }

    private double getEffectiveFishWeight(@NonNull IFish fish) {
        return getFishModifiers().getOrDefault(fish, WeightModifier.IDENTITY).apply(FishManager.getBaseFishWeight(fish));
    }

    private void processBaitUsage(@NonNull Player player, @Nullable IRarity rarity, @Nullable IFish fish) {
        if (fish == null) {
            return;
        }

        fish.setWasBaited(true);
        fish.setFisherman(player);

        if (shouldAlertUsage(rarity, fish)) {
            alertUsage(player);
        }
    }

    private boolean shouldAlertUsage(@Nullable IRarity rarity, @NonNull IFish fish) {
        return (rarity != null && hasRarityModifier(rarity)) || hasFishModifier(fish);
    }

    @Override
    public void handleFish(@NonNull Player player, @NonNull IFish fish, @NonNull ItemStack fishingRod) {
        if (!fish.isWasBaited()) {
            EvenMoreFish.getInstance().debug("Fish: %s was not baited, ignoring..".formatted(FishRarityKey.of(fish)));
            return;
        }

        EvenMoreFish.getInstance().debug("Fish: %s was baited".formatted(FishRarityKey.of(fish)));
        fish.setFisherman(player);

        // Only consume bait if this bait actually affected the catch
        if (!shouldConsumeBait(fish)) {
            Logging.debug("Bait %s did not modify caught fish %s; leaving bait unchanged.".formatted(getId(), FishRarityKey.of(fish)));
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

    private boolean shouldConsumeBait(@NonNull IFish fish) {
        return shouldConsumeBait(getRarityModifiers(), getFishModifiers(), fish);
    }

    static boolean shouldConsumeBait(
        @NonNull Map<IRarity, WeightModifier> rarityModifiers,
        @NonNull Map<IFish, WeightModifier> fishModifiers,
        @NonNull IFish fish
    ) {
        return rarityModifiers.containsKey(fish.getRarity()) || fishModifiers.containsKey(fish);
    }

    private boolean hasRarityModifier(@NonNull IRarity rarity) {
        return getRarityModifiers().containsKey(rarity);
    }

    private boolean hasFishModifier(@NonNull IFish fish) {
        return getFishModifiers().containsKey(fish);
    }

    private boolean hasModifiersInRarity(@NonNull IRarity rarity) {
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
    public int getIndex() {
        return getConfig().getInt("sort-index");
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
    public @NonNull String getId() {
        return id;
    }

    public @NonNull EMFSingleMessage getFormat() {
        String format = getConfig().getString("format", "<yellow>{name}");
        return EMFSingleMessage.fromString(format);
    }

    public @NonNull EMFSingleMessage format(@NonNull String name) {
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
    public boolean isSilent() {
        return getConfig().getBoolean("silent", false);
    }

    @Override
    public boolean hasCatchRewards() {
        return !getConfig().getStringList("catch-event").isEmpty();
    }

    @Override
    public @NonNull List<Reward> getCatchRewards() {
        return getConfig().getStringList("catch-event").stream()
            .map(this::parseEventPlaceholders)
            .map(Reward::new)
            .toList();
    }

    @Override
    public void reload(@NonNull File configFile) {
        super.reload(configFile);
        if (fishManager == null || mainConfig == null) {
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
        if (fishManager == null || mainConfig == null) {
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
        logger.warning("Bait file '" + getFileName() + "' is using the pre-2.3.0 bait format. Please migrate it to 'rarity-modifiers' and/or 'fish-modifiers'.");
    }

    public @NonNull List<Component> createDebugMessages(@NonNull Player player, @NonNull RequirementContext requirementContext) {
        return createDebugMessages(player, player.getLocation(), requirementContext);
    }

    public @NonNull List<Component> createDebugMessages(@NonNull Player player, @NonNull Location location, @NonNull RequirementContext requirementContext) {
        final List<Component> messages = new ArrayList<>();
        final List<RarityChance> rarityChances = calculateRarityChances(player, location, requirementContext);

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
                    fishChance.fish().getId(),
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

    private @NonNull List<RarityChance> calculateRarityChances(@NonNull Player player, @NonNull Location location, @NonNull RequirementContext requirementContext) {
        final List<IRarity> availableRarities = fishManager.getAvailableRarities(
            player,
            Set.copyOf(fishManager.getRarityMap().values()),
            null,
            requirementContext
        );
        if (availableRarities.isEmpty()) {
            return List.of();
        }

        final Map<IRarity, Long> rarityCounts = availableRarities.stream()
            .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        final double totalRarityWeight = rarityCounts.entrySet().stream()
            .mapToDouble(entry -> getEffectiveRarityWeight(entry.getKey()) * entry.getValue())
            .sum();

        return rarityCounts.entrySet().stream()
            .map(entry -> buildRarityChance(entry.getKey(), player, location, totalRarityWeight, availableRarities.size(), entry.getValue()))
            .sorted(Comparator.comparingDouble(RarityChance::chance).reversed())
            .toList();
    }

    private @NonNull RarityChance buildRarityChance(@NonNull IRarity rarity,
                                                    @NonNull Player player,
                                                    @NonNull Location location,
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

    private @NonNull List<FishChance> calculateFishChances(@NonNull IRarity rarity, @NonNull Player player, @NonNull Location location, double rarityChance) {
        final List<? extends IFish> availableFish = fishManager.getAvailableFish(rarity, location, player, true, null, null);
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

    private @NonNull String formatLocation(@NonNull Location location) {
        final String world = location.getWorld() != null ? location.getWorld().getName() : "unknown";
        return "%s %.1f %.1f %.1f".formatted(world, location.getX(), location.getY(), location.getZ());
    }

    private @NonNull String formatPercent(double value) {
        return String.format(Locale.ROOT, "%.2f%%", value * 100.0D);
    }

    private @NonNull String formatNumber(double value) {
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
    public boolean attemptPurchase(@NonNull Player player) {
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

    private @NonNull NamespacedKey getRecipeKey() {
        return new NamespacedKey(EvenMoreFish.getInstance(), "bait-" + getId());
    }

    static int resolveMaxBaits(@NonNull Section config) {
        return config.getInt("max-baits", -1);
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

    public @Nullable EMFRecipe<?> getRecipe() {
        return this.recipe;
    }

    private String parseEventPlaceholders(String rewardString) {
        // {displayname} Placeholder
        rewardString = rewardString.replace("{displayname}", getDisplayName());

        // {name} Placeholder
        rewardString = rewardString.replace("{name}", getId());

        return rewardString;
    }

}
