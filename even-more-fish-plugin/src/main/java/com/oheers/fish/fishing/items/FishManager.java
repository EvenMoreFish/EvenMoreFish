package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.AbstractFileBasedManager;
import com.oheers.fish.api.fishing.items.AbstractFishManager;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.Processor;
import com.oheers.fish.fishing.items.config.FishConversions;
import com.oheers.fish.fishing.items.config.RarityConversions;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.utils.WeightedRandom;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
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
                                          @Nullable CustomRod customRod) {
        Rarity preDecided = getPreDecidedRarity(fisher);
        if (preDecided != null) return preDecided;

        List<Rarity> allowedRarities = filterByCustomRod(
                getAllowedRarities(fisher, boostRate, boostedRarities, totalRarities),
                customRod
        );

        if (allowedRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe(
                    "No rarities available for " + (fisher != null ? fisher.getName() : "N/A")
            );
            return null;
        }

        Rarity selected = selectRandomRarity(allowedRarities, boostRate, boostedRarities);
        return selected != null && isRarityAllowedInCompetition(selected) ? selected : null;
    }

    public Fish getFish(Rarity rarity, Location location, Player player,
                        double boostRate, List<Fish> boostedFish,
                        boolean doRequirementChecks,
                        @Nullable Processor<?> processor,
                        @Nullable CustomRod customRod) {
        if (rarity == null || rarity.getOriginalFishList().isEmpty()) {
            rarity = getRandomWeightedRarity(player, 1,
                    Collections.emptySet(),
                    Set.copyOf(getItemMap().values()),
                    customRod
            );
            if (rarity == null) return null;
        }

        final RequirementContext context = new RequirementContext(
                location != null ? location.getWorld() : null,
                location,
                player,
                null,
                null
        );

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

    private Rarity selectRandomRarity(List<Rarity> rarities, double boostRate, Set<Rarity> boosted) {
        return WeightedRandom.pick(
                rarities,
                Rarity::getWeight,
                boostRate,
                boosted,
                EvenMoreFish.getInstance().getRandom()
        );
    }

    private List<Rarity> filterByCustomRod(List<Rarity> rarities, CustomRod rod) {
        return rod != null ?
                rarities.stream().filter(r -> rod.getAllowedRarities().contains(r)).toList() :
                rarities;
    }

    private List<Rarity> getAllowedRarities(Player fisher, double boostRate,
                                            Set<Rarity> boostedRarities,
                                            Set<Rarity> totalRarities) {
        if (fisher == null) return new ArrayList<>(totalRarities);

        RequirementContext context = new RequirementContext(
                fisher.getWorld(),
                fisher.getLocation(),
                fisher,
                null,
                null
        );

        String region = FishUtils.getRegionName(fisher.getLocation());
        return getItemMap().values().stream()
                .filter(r -> !shouldSkipRarity(r, boostRate, boostedRarities, fisher))
                .filter(r -> r.getRequirement().meetsRequirements(context))
                .flatMap(r -> Collections.nCopies(
                        (int)Math.max(1, MainConfig.getInstance().getRegionBoost(region, r.getId())),
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

        ToDoubleFunction<Fish> weightFunction = fish -> fish.getWeight() == 0 ? 1 : fish.getWeight();
        Set<Fish> boostedSet = boostedFish != null ? new HashSet<>(boostedFish) : Collections.emptySet();

        return WeightedRandom.pick(
                fishList,
                weightFunction,
                boostRate,
                boostedSet,
                EvenMoreFish.getInstance().getRandom()
        );
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