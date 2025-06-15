package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.baits.configs.BaitFileUpdates;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO still uses deprecated methods
public class Bait extends ConfigBase {

    private static final Logger logger = EvenMoreFish.getInstance().getLogger();

    private final @NotNull String id;
    private final ItemFactory itemFactory;

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
    public Bait(@NotNull File file) throws InvalidConfigurationException {
        super(file, EvenMoreFish.getInstance(), false);
        BaitFileUpdates.update(this);
        performRequiredConfigChecks();
        this.id = Objects.requireNonNull(getConfig().getString("id"));

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
    public ItemStack create(OfflinePlayer player) {
        return itemFactory.createItem(player.getUniqueId());
    }

    /**
     * @return All configured rarities from this bait's configuration.
     */
    public @NotNull List<Rarity> getRarities() {
        List<String> rarityStrings = getConfig().getStringList("rarities");
        return rarityStrings.stream()
                .map(FishManager.getInstance()::getRarity)
                .filter(Objects::nonNull)
                .toList();
    }

    private @NotNull List<Fish> getFish() {
        List<Fish> fishList = new ArrayList<>();
        Section fishSection = getConfig().getSection("fish");
        if (fishSection == null) {
            return fishList;
        }
        for (String rarityName : fishSection.getRoutesAsStrings(false)) {
            Rarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
                continue;
            }
            List<String> fishNames = fishSection.getStringList(rarityName);
            for (String fishName : fishNames) {
                Fish fish = rarity.getFish(fishName);
                if (fish == null) {
                    continue;
                }
                fishList.add(fish);
            }
        }
        return fishList;
    }

    /**
     * This fetches the boost's lore from the config and inserts the boost-rates into the {boosts} variable. This needs
     * to be called after the bait theme is set and the boosts have been initialized, since it uses those variables.
     */
    private List<Component> createBoostLore() {

        EMFListMessage lore = ConfigMessage.BAIT_BAIT_LORE.getMessage().toListMessage();

        Supplier<EMFMessage> boostsVariable = () -> {
            EMFMessage message = EMFSingleMessage.empty();
            List<Rarity> rarityList = getRarities();
            if (!rarityList.isEmpty()) {
                if (rarityList.size() > 1) {
                    message.appendMessage(ConfigMessage.BAIT_BOOSTS_RARITIES.getMessage());
                } else {
                    message.appendMessage(ConfigMessage.BAIT_BOOSTS_RARITY.getMessage());
                }
                message.setAmount(Integer.toString(rarityList.size()));
            }

            List<Fish> fishList = getFish();
            if (!fishList.isEmpty()) {
                message.appendMessage(ConfigMessage.BAIT_BOOSTS_FISH.getMessage());
                message.setAmount(Integer.toString(fishList.size()));
            }
            return message;
        };
        lore.setVariable("{boosts}", boostsVariable.get());

        Supplier<EMFListMessage> loreVariable = () -> EMFListMessage.fromStringList(itemFactory.getLore().getConfiguredValue());
        lore.setVariable("{lore}", loreVariable.get());

        lore.setVariable("{bait_theme}", "");

        return lore.getComponentListMessage();
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
     *
     * @return A chosen fish.
     */
    public Fish chooseFish(@NotNull Player player, @NotNull Location location) {
        Set<Rarity> boostedRarities = new HashSet<>(getRarities());
        List<Rarity> fishListRarities = getFish().stream().map(Fish::getRarity).toList();

        boostedRarities.addAll(fishListRarities);

        Rarity fishRarity = FishManager.getInstance().getRandomWeightedRarity(player, getBoostRate(), boostedRarities, Set.copyOf(FishManager.getInstance().getRarityMap().values()), null);
        Fish fish;

        if (!getFish().isEmpty()) {
            // The bait has both rarities: and fish: set but the plugin chose a rarity with no boosted fish. This ensures
            // the method isn't given an empty list.
            if (!fishListRarities.contains(fishRarity)) {
                fish = FishManager.getInstance().getFish(fishRarity, location, player, MainConfig.getInstance().getBaitBoostRate(), fishRarity.getFishList(), true, null, null);
            } else {
                fish = FishManager.getInstance().getFish(fishRarity, location, player, MainConfig.getInstance().getBaitBoostRate(), getFish(), true, null, null);
            }

            if (!getRarities().contains(fishRarity) && (fish == null || !getFish().contains(fish))) {
                // boost effect chose a fish but the randomizer didn't pick out the right fish - they've been incorrectly boosted.
                fish = FishManager.getInstance().getFish(fishRarity, location, player, 1, null, true, null, null);
            } else {
                alertUsage(player);
            }
        } else {
            fish = FishManager.getInstance().getFish(fishRarity, location, player, 1, null, true, null, null);
            if (getRarities().contains(fishRarity)) {
                alertUsage(player);
            }
        }
        if (fish != null) {
            fish.setWasBaited(true);
            fish.setFisherman(player.getUniqueId());
        }
        return fish;
    }

    public void handleFish(@NotNull Player player, @NotNull Fish fish, @NotNull ItemStack fishingRod) {
        if (!fish.isWasBaited()) {
            return;
        }
        fish.setFisherman(player.getUniqueId());
        try {
            ApplicationResult result = BaitNBTManager.applyBaitedRodNBT(fishingRod, this, -1);
            if (result == null) {
                return;
            }
            ItemStack newFishingRod = result.getFishingRod();
            if (newFishingRod != null) {
                fishingRod.setItemMeta(newFishingRod.getItemMeta());
                EvenMoreFish.getInstance().incrementMetricBaitsUsed(1);
            }
        } catch (MaxBaitsReachedException | MaxBaitReachedException | NullPointerException exception) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);
        }
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
        message.setBait(format(id));
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
        return MainConfig.getInstance().getBaitBoostRate();
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

}
