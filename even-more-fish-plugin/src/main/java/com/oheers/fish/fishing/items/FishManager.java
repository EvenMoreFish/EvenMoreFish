package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.boost.RarityBoostRegistry;
import com.oheers.fish.api.fishing.FishingType;
import com.oheers.fish.api.fishing.items.AbstractFishManager;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.RarityKey;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.Processor;
import com.oheers.fish.fishing.items.config.FishConversions;
import com.oheers.fish.fishing.items.config.RarityConversions;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.items.nbt.NBTHolder;
import com.oheers.fish.utils.WeightedRandom;
import com.oheers.fish.utils.nbt.NbtKeys;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.ToDoubleFunction;

public class FishManager extends AbstractFishManager<Rarity> {

    private static FishManager instance;

    private FishManager() {
        super();
    }

    public static @NotNull FishManager getInstance() {
        if (instance == null) {
            instance = new FishManager();
        }
        return instance;
    }

    @Override
    protected void performPreLoadConversions() {
        new RarityConversions().performCheck();
        new FishConversions().performCheck();
    }

    @Override
    protected void loadItems() {
        loadItemsFromFiles(
                "rarities",
                this::loadRaritySafely,
                Rarity::getId,
                rarity -> shouldSkipRarity(rarity, getItemMap())
        );
    }

    @Override
    protected void logLoadedItems() {
        int totalFish = getItemMap().values().stream()
                .mapToInt(rarity -> rarity.getOriginalFishList().size())
                .sum();

        EvenMoreFish.getInstance().getLogger().info(() ->
                "Loaded FishManager with %d Rarities and %d Fish."
                        .formatted(getItemMap().size(), totalFish)
        );
    }

    /* Original Fish Manager Functionality Below */

    @Override
    public @Nullable Rarity getRarity(@NotNull String rarityName) {
        return getItem(rarityName);
    }

    @Override
    public @Nullable Fish getFish(@NotNull String rarityName, @NotNull String fishName) {
        final Rarity rarity = getRarity(rarityName);
        return rarity != null ? rarity.getFish(fishName) : null;
    }

    @Override
    public @Nullable IFish getFish(@Nullable ItemStack item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        NBTHolder<ItemStack> holder = NBTHolder.itemStack(item);
        String nameString = holder.getString(NbtKeys.EMF_FISH_NAME.get());
        String playerString = holder.getString(NbtKeys.EMF_FISH_PLAYER.get());
        String rarityString = holder.getString(NbtKeys.EMF_FISH_RARITY.get());
        Float length = holder.getFloat(NbtKeys.EMF_FISH_LENGTH.get());
        Integer randomIndex = holder.getInteger(NbtKeys.EMF_FISH_RANDOM_INDEX.get());

        if (nameString == null || rarityString == null) {
            return null;
        }

        RarityKey key = RarityKey.of(rarityString, nameString);
        if (key == null) {
            return null;
        }

        IFish fish = key.getFish();
        if (randomIndex != null && fish instanceof Fish f) { // TODO Can remove that instanceof when ItemFactory is part of API.
            f.getFactory().setRandomIndex(randomIndex);
        }
        fish.setLength(length);
        if (playerString != null) {
            try {
                fish.setFisherman(UUID.fromString(playerString));
            } catch (IllegalArgumentException exception) {
                fish.setFisherman((OfflinePlayer) null);
            }
        }
        return fish;
    }

    @Override
    public @Nullable IFish getFish(@Nullable Skull skull, @Nullable Player fisher) {
        if (skull == null) {
            return null;
        }
        final String nameString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_NAME).toString()));
        final String playerString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_PLAYER).toString()));
        final String rarityString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_RARITY).toString()));
        final Float lengthFloat = NBT.getPersistentData(skull, nbt -> nbt.getFloat(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_LENGTH).toString()));
        final Integer randomIndex = NBT.getPersistentData(skull, nbt -> nbt.getInteger(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_RANDOM_INDEX).toString()));

        if (nameString == null || rarityString == null) {
            Logging.warn("NBT Error", new InvalidFishException("NBT Error"));
            return null;
        }

        Rarity rarity = FishManager.getInstance().getRarity(rarityString);
        if (rarity == null) {
            return null;
        }

        Fish fish = rarity.getFish(nameString);
        if (fish == null) {
            return null;
        }
        fish.setLength(lengthFloat);
        if (randomIndex != null) {
            fish.getFactory().setRandomIndex(randomIndex);
        }
        if (playerString != null) {
            try {
                fish.setFisherman(UUID.fromString(playerString));
            } catch (IllegalArgumentException exception) {
                fish.setFisherman((OfflinePlayer) null);
            }
        } else if (fisher != null) {
            fish.setFisherman(fisher);
        }

        return fish;
    }

    @Override
    public @Nullable IFish getFish(@Nullable Entity itemEntity) {
        if (!(itemEntity instanceof Item item)) {
            return null;
        }
        return getFish(item.getItemStack());
    }

    @Override
    public boolean isFish(@Nullable ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        return NbtUtils.hasKey(item, NbtKeys.EMF_FISH_NAME);
    }

    @Override
    public boolean isFish(@Nullable Skull skull) {
        if (skull == null) {
            return false;
        }
        return NbtUtils.hasKey(skull, NbtKeys.EMF_FISH_NAME);
    }

    @Override
    public boolean isFish(@Nullable Entity itemEntity) {
        if (!(itemEntity instanceof Item item)) {
            return false;
        }
        return isFish(item.getItemStack());
    }

    private Rarity loadRaritySafely(File file) throws InvalidConfigurationException {
        EvenMoreFish.getInstance().debug("Loading " + file.getName() + " rarity");
        return new Rarity(file);
    }

    private boolean shouldSkipRarity(Rarity rarity, Map<String, Rarity> rarityMap) {
        if (rarity.isDisabled()) {
            return true;
        }
        final String id = rarity.getId();
        if (rarityMap.containsKey(id)) {
            EvenMoreFish.getInstance().getLogger().warning(
                    "Duplicate rarity ID '" + id + "' found. Skipping."
            );
            return true;
        }
        return false;
    }

    @Override
    public @NotNull TreeMap<String, Rarity> getRarityMap() {
        return getItemMap();
    }

    /* Fishing Logic Methods */

    public Rarity getRandomWeightedRarity(Player fisher, double boostRate,
                                          @NotNull Set<Rarity> boostedRarities,
                                          Set<Rarity> totalRarities,
                                          @Nullable CustomRod customRod,
                                          @NotNull RequirementContext requirementContext) {
        Rarity preDecided = getPreDecidedRarity(fisher);
        if (preDecided != null) return preDecided;

        List<Rarity> allowedRarities = filterByCustomRod(
                getAllowedRarities(fisher, boostRate, boostedRarities, totalRarities, requirementContext),
                customRod
        );

        if (allowedRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe(
                    "No rarities available for " + (fisher != null ? fisher.getName() : "N/A")
            );
            return null;
        }

        Rarity selected = selectRandomRarity(allowedRarities, boostRate, boostedRarities,
                fisher, requirementContext.getLocation());
        return selected != null && isRarityAllowedInCompetition(selected) ? selected : null;
    }

    public @Nullable Rarity getWeightedRarity(@Nullable Player fisher,
                                              @NotNull Set<Rarity> totalRarities,
                                              @NotNull ToDoubleFunction<Rarity> weightFunction,
                                              @Nullable CustomRod customRod,
                                              @NotNull RequirementContext requirementContext) {
        Rarity preDecided = getPreDecidedRarity(fisher);
        if (preDecided != null) {
            return preDecided;
        }

        List<Rarity> allowedRarities = getAvailableRarities(fisher, totalRarities, customRod, requirementContext);
        if (allowedRarities.isEmpty()) {
            return null;
        }

        Rarity selected = WeightedRandom.pick(
            allowedRarities,
            weightFunction,
            EvenMoreFish.getInstance().getRandom()
        );
        return selected != null && isRarityAllowedInCompetition(selected) ? selected : null;
    }

    public Fish getFish(Rarity rarity, Location location, Player player,
                        double boostRate, List<Fish> boostedFish,
                        boolean doRequirementChecks,
                        @Nullable Processor<?> processor,
                        @Nullable CustomRod customRod,
                        @NotNull RequirementContext context) {
        if (rarity == null || rarity.getOriginalFishList().isEmpty()) {
            rarity = getRandomWeightedRarity(player, 1,
                Collections.emptySet(),
                Set.copyOf(getItemMap().values()),
                customRod,
                context
            );
            if (rarity == null) return null;
        }

        final List<Fish> available = rarity.getFishList().stream()
            .filter(fish -> isFishAllowed(fish, boostRate, boostedFish, processor, customRod, context, doRequirementChecks))
            .toList();

        if (available.isEmpty()) {
            logNoFishAvailable(rarity, location, customRod);
            return null;
        }

        Fish selected = getRandomWeightedFish(available, boostRate, boostedFish);
        return isFishAllowedOutsideCompetition(selected) ? selected : null;
    }

    public @Nullable Fish getWeightedFish(@Nullable Rarity rarity,
                                          @Nullable Location location,
                                          @Nullable Player player,
                                          @NotNull ToDoubleFunction<Fish> weightFunction,
                                          boolean doRequirementChecks,
                                          @Nullable Processor<?> processor,
                                          @Nullable CustomRod customRod) {
        if (rarity == null) {
            return null;
        }

        List<Fish> available = getAvailableFish(rarity, location, player, doRequirementChecks, processor, customRod);
        if (available.isEmpty()) {
            logNoFishAvailable(rarity, location, customRod);
            return null;
        }

        Fish selected = WeightedRandom.pick(
            available,
            weightFunction,
            EvenMoreFish.getInstance().getRandom()
        );
        return isFishAllowedOutsideCompetition(selected) ? selected : null;
    }

    /* Helper Methods */

    private Rarity getPreDecidedRarity(Player player) {
        return player != null ?
                EvenMoreFish.getInstance().getDecidedRarities().remove(player.getUniqueId()) :
                null;
    }

    private boolean isRarityAllowedInCompetition(Rarity rarity) {
        return Competition.isActive() ||
                !MainConfig.getInstance().isFishCatchOnlyInCompetition() ||
                (EvenMoreFish.getInstance().isRaritiesCompCheckExempt() && rarity.hasCompExemptFish());
    }

    private Rarity selectRandomRarity(List<Rarity> rarities, double boostRate, Set<Rarity> boosted,
                                      @Nullable Player fisher, @Nullable Location location) {
        return WeightedRandom.pick(
                rarities,
                externallyBoostedWeight(fisher, location),
                boostRate,
                boosted,
                EvenMoreFish.getInstance().getRandom()
        );
    }

    /**
     * The rarity weight function with any externally registered {@link RarityBoostRegistry}
     * boosts (e.g. area-of-effect fishing buffs from other plugins) multiplied in. Falls back
     * to the plain configured weight when nothing is registered or no fisher/location is known.
     */
    private ToDoubleFunction<Rarity> externallyBoostedWeight(@Nullable Player fisher, @Nullable Location location) {
        RarityBoostRegistry boosts = RarityBoostRegistry.getInstance();
        if (fisher == null || location == null || boosts.isEmpty()) {
            return Rarity::getWeight;
        }
        return rarity -> rarity.getWeight() * boosts.combinedMultiplier(fisher, location, rarity.getId());
    }

    public @NotNull List<Rarity> getAvailableRarities(@Nullable Player fisher,
                                                      @NotNull Set<Rarity> totalRarities,
                                                      @Nullable CustomRod customRod,
                                                      @NotNull RequirementContext requirementContext) {
        return filterByCustomRod(
            getAllowedRarities(fisher, 1.0D, Collections.emptySet(), totalRarities, requirementContext),
            customRod
        );
    }

    private List<Rarity> filterByCustomRod(List<Rarity> rarities, CustomRod rod) {
        return rod != null ?
                rarities.stream().filter(r -> rod.getAllowedRarities().contains(r)).toList() :
                rarities;
    }

    private List<Rarity> getAllowedRarities(Player fisher, double boostRate,
                                            Set<Rarity> boostedRarities,
                                            Set<Rarity> totalRarities,
                                            @NotNull RequirementContext requirementContext) {
        if (fisher == null) return new ArrayList<>(totalRarities);

        String region = FishUtils.getRegionName(fisher.getLocation());
        return getItemMap().values().stream()
            .filter(r -> !shouldSkipRarity(r, boostRate, boostedRarities, fisher))
            .filter(r -> r.getRequirement().meetsRequirements(requirementContext))
            .flatMap(r -> Collections.nCopies(
                (int) Math.max(1, MainConfig.getInstance().getRegionBoost(region, r.getId())),
                r
            ).stream())
            .toList();
    }

    private boolean shouldSkipRarity(Rarity rarity, double boostRate,
                                     Set<Rarity> boostedRarities, Player fisher) {
        return (boostedRarities != null && boostRate == -1 && !boostedRarities.contains(rarity)) ||
                (rarity.getPermission() != null && !fisher.hasPermission(rarity.getPermission()));
    }

    public @Nullable Fish getRandomWeightedFish(@NotNull List<Fish> fishList, double boostRate, @Nullable List<Fish> boostedFish) {
        if (fishList.isEmpty()) return null;

        ToDoubleFunction<Fish> weightFunction = FishManager::getBaseFishWeight;
        Set<Fish> boostedSet = boostedFish != null ? new HashSet<>(boostedFish) : Collections.emptySet();

        return WeightedRandom.pick(
                fishList,
                weightFunction,
                boostRate,
                boostedSet,
                EvenMoreFish.getInstance().getRandom()
        );
    }

    public @NotNull List<Fish> getAvailableFish(@NotNull Rarity rarity,
                                                @Nullable Location location,
                                                @Nullable Player player,
                                                boolean doRequirementChecks,
                                                @Nullable Processor<?> processor,
                                                @Nullable CustomRod customRod) {
        final RequirementContext context = new RequirementContext(
            location != null ? location.getWorld() : null,
            location,
            player,
            null,
            null,
            null
        );

        return rarity.getFishList().stream()
            .filter(fish -> isFishAllowedByCustomRod(fish, customRod))
            .filter(fish -> isFishAllowedByProcessor(fish, processor))
            .filter(fish -> meetsRequirements(fish, doRequirementChecks, context))
            .toList();
    }

    public static double getBaseFishWeight(@NotNull Fish fish) {
        return fish.getWeight() == 0 ? 1.0D : fish.getWeight();
    }

    private boolean isFishAllowed(Fish fish, double boostRate, List<Fish> boostedFish,
                                  Processor<?> processor, CustomRod customRod,
                                  RequirementContext context, boolean doRequirements) {
        return isFishAllowedByCustomRod(fish, customRod) &&
                isFishBoosted(fish, boostRate, boostedFish) &&
                isFishAllowedByProcessor(fish, processor) &&
                meetsRequirements(fish, doRequirements, context);
    }

    private boolean isFishAllowedByCustomRod(Fish fish, CustomRod rod) {
        return rod == null || rod.getAllowedFish().isEmpty() || rod.getAllowedFish().contains(fish);
    }

    private boolean isFishBoosted(Fish fish, double boostRate, List<Fish> boostedFish) {
        return boostRate == -1 || boostedFish == null || boostedFish.contains(fish);
    }

    private boolean isFishAllowedByProcessor(Fish fish, Processor<?> processor) {
        return processor == null || processor.canUseFish(fish);
    }

    private boolean meetsRequirements(Fish fish, boolean doChecks, RequirementContext context) {
        return !doChecks || fish.getRequirement().meetsRequirements(context);
    }

    private boolean isFishAllowedOutsideCompetition(Fish fish) {
        return fish != null && (
                Competition.isActive() ||
                        !MainConfig.getInstance().isFishCatchOnlyInCompetition() ||
                        (EvenMoreFish.getInstance().isRaritiesCompCheckExempt() && fish.isCompExemptFish())
        );
    }

    private void logNoFishAvailable(Rarity rarity, Location location, CustomRod rod) {
        String biome = location != null && location.getWorld() != null ?
                location.getWorld().getBiome(location).name() : "unknown biome";

        EvenMoreFish.getInstance().getLogger().warning(() ->
                "No fish available for rarity %s at %s in biome %s (Custom Rod: %b)"
                        .formatted(
                                rarity.getId(),
                                location != null ?
                                        "x=%.1f,y=%.1f,z=%.1f".formatted(location.getX(), location.getY(), location.getZ()) :
                                        "null location",
                                biome,
                                rod != null
                        )
        );
    }
}
