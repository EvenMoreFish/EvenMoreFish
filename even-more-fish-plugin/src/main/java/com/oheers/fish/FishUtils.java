package com.oheers.fish;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.utils.ItemUtils;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.tr7zw.changeme.nbtapi.NBT;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;


public class FishUtils {

    private FishUtils() {
        throw new UnsupportedOperationException();
    }

    // checks for the "emf-fish-name" nbt tag, to determine if this ItemStack is a fish or not.
    public static boolean isFish(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return false;
        }

        return NbtUtils.hasKey(item, NbtKeys.EMF_FISH_NAME);
    }

    public static boolean isFish(Skull skull) {
        if (skull == null) {
            return false;
        }

        return NbtUtils.hasKey(skull, NbtKeys.EMF_FISH_NAME);
    }

    public static @Nullable Fish getFish(ItemStack item) {
        // all appropriate null checks can be safely assumed to have passed to get to a point where we're running this method.

        String nameString = NbtUtils.getString(item, NbtKeys.EMF_FISH_NAME);
        String playerString = NbtUtils.getString(item, NbtKeys.EMF_FISH_PLAYER);
        String rarityString = NbtUtils.getString(item, NbtKeys.EMF_FISH_RARITY);
        Float lengthFloat = NbtUtils.getFloat(item, NbtKeys.EMF_FISH_LENGTH);
        Integer randomIndex = NbtUtils.getInteger(item, NbtKeys.EMF_FISH_RANDOM_INDEX);

        if (nameString == null || rarityString == null) {
            return null;
        }


        // Get the rarity
        Rarity rarity = FishManager.getInstance().getRarity(rarityString);

        if (rarity == null) {
            return null;
        }

        // setting the correct length so it's an exact replica.
        Fish fish = rarity.getFish(nameString);
        if (fish == null) {
            return null;
        }
        if (randomIndex != null) {
            fish.getFactory().setType(randomIndex);
        }
        fish.setLength(lengthFloat);
        if (playerString != null) {
            try {
                fish.setFisherman(UUID.fromString(playerString));
            } catch (IllegalArgumentException exception) {
                fish.setFisherman(null);
            }
        }
        return fish;
    }

    public static @Nullable Fish getFish(Skull skull, Player fisher) throws InvalidFishException {
        // all appropriate null checks can be safely assumed to have passed to get to a point where we're running this method.
        final String nameString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_NAME).toString()));
        final String playerString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_PLAYER).toString()));
        final String rarityString = NBT.getPersistentData(skull, nbt -> nbt.getString(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_RARITY).toString()));
        final Float lengthFloat = NBT.getPersistentData(skull, nbt -> nbt.getFloat(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_LENGTH).toString()));
        final Integer randomIndex = NBT.getPersistentData(skull, nbt -> nbt.getInteger(NbtUtils.getNamespacedKey(NbtKeys.EMF_FISH_RANDOM_INDEX).toString()));

        if (nameString == null || rarityString == null) {
            throw new InvalidFishException("NBT Error");
        }

        // Get the rarity
        Rarity rarity = FishManager.getInstance().getRarity(rarityString);

        if (rarity == null) {
            return null;
        }

        // setting the correct length and randomIndex, so it's an exact replica.
        Fish fish = rarity.getFish(nameString);
        if (fish == null) {
            return null;
        }
        fish.setLength(lengthFloat);
        if (randomIndex != null) {
            fish.getFactory().setType(randomIndex);
        }
        if (playerString != null) {
            try {
                fish.setFisherman(UUID.fromString(playerString));
            } catch (IllegalArgumentException exception) {
                fish.setFisherman(null);
            }
        } else {
            fish.setFisherman(fisher.getUniqueId());
        }

        return fish;
    }

    public static void giveItems(List<ItemStack> items, Player player) {
        if (items == null || items.isEmpty()) {
            return; // Early return if the list is null or empty
        }

        // Remove null items and avoid modifying the original list
        List<ItemStack> filteredItems = items.stream()
                .filter(Objects::nonNull)
                .toList();

        // Play item pickup sound
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);

        // Add items to the player's inventory
        Map<Integer, ItemStack> leftoverItems = player.getInventory().addItem(filteredItems.toArray(new ItemStack[0]));

        // Drop any leftover items in the world
        leftoverItems.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
    }


    public static void giveItems(ItemStack[] items, Player player) {
        giveItems(Arrays.asList(items), player);
    }

    public static void giveItem(ItemStack item, Player player) {
        giveItems(List.of(item), player);
    }

    public static boolean checkRegion(Location location, List<String> whitelistedRegions) {
        // If no whitelist is defined, allow all regions
        if (whitelistedRegions.isEmpty()) {
            return true;
        }

        // Check WorldGuard
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(location));

            for (ProtectedRegion region : regions) {
                if (whitelistedRegions.contains(region.getId())) {
                    return true; // Return true if a region matches the whitelist
                }
            }

            return false; // No match found in WorldGuard regions
        }

        // Check RedProtect
        if (Bukkit.getPluginManager().isPluginEnabled("RedProtect")) {
            Region region = RedProtect.get().getAPI().getRegion(location);
            if (region != null) {
                return whitelistedRegions.contains(region.getName()); // Check if the region is whitelisted
            }
            return false; // No region found in RedProtect
        }

        // If no supported region plugins are found
        EvenMoreFish.getInstance().getLogger().warning("Please install WorldGuard or RedProtect to use allowed-regions.");
        return true; // Allow by default if no region plugin is present
    }


    public static @Nullable String getRegionName(Location location) {
        if (!MainConfig.getInstance().isRegionBoostsEnabled()) {
            EvenMoreFish.getInstance().debug("Region boosts are disabled.");
            return null;
        }

        EvenMoreFish plugin = EvenMoreFish.getInstance();
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        Plugin worldGuard = pluginManager.getPlugin("WorldGuard");
        if (worldGuard != null && worldGuard.isEnabled()) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            ApplicableRegionSet set = container.createQuery().getApplicableRegions(BukkitAdapter.adapt(location));

            if (set.getRegions().isEmpty()) {
                EvenMoreFish.getInstance().debug("Could not find any regions with WorldGuard");
                return null;
            }

            return set.iterator().next().getId(); // Return the first region found
        }

        if (pluginManager.isPluginEnabled("RedProtect")) {
            Region region = RedProtect.get().getAPI().getRegion(location);
            if (region == null) {
                EvenMoreFish.getInstance().debug("Could not find any regions with RedProtect");
                return null;
            }

            return region.getName();
        }

        plugin.getLogger().warning("Please install WorldGuard or RedProtect to use region-boosts.");
        return null;
    }


    public static boolean checkWorld(Location l) {
        // if the user has defined a world whitelist
        if (!MainConfig.getInstance().worldWhitelist()) {
            return true;
        }

        // Gets a list of user defined regions
        List<String> whitelistedWorlds = MainConfig.getInstance().getAllowedWorlds();
        if (l.getWorld() == null) {
            return false;
        }

        return whitelistedWorlds.contains(l.getWorld().getName());
    }

    public static @NotNull EMFMessage timeFormat(long timeLeft) {
        long hours = timeLeft / 3600;
        long minutes = (timeLeft % 3600) / 60;
        long seconds = timeLeft % 60;

        EMFSingleMessage formatted = EMFSingleMessage.empty();

        if (hours > 0) {
            EMFMessage message = ConfigMessage.BAR_HOUR.getMessage();
            message.setVariable("{hour}", String.valueOf(hours));
            formatted.appendMessage(message);
            formatted.appendComponent(Component.space());
        }

        if (minutes > 0) {
            EMFMessage message = ConfigMessage.BAR_MINUTE.getMessage();
            message.setVariable("{minute}", String.valueOf(minutes));
            formatted.appendMessage(message);
            formatted.appendComponent(Component.space());
        }

        // Shows remaining seconds if seconds > 0 or hours and minutes are 0, e.g. "1 minutes and 0 seconds left" and "5 seconds left"
        if (seconds > 0 || (minutes == 0 && hours == 0)) {
            EMFMessage message = ConfigMessage.BAR_SECOND.getMessage();
            message.setVariable("{second}", String.valueOf(seconds));
            formatted.appendMessage(message);
            formatted.appendComponent(Component.space());
        }

        return formatted;
    }

    public static @NotNull String timeRaw(long timeLeft) {
        String returning = "";
        long hours = timeLeft / 3600;

        if (timeLeft >= 3600) {
            returning += hours + ":";
        }

        if (timeLeft >= 60) {
            returning += ((timeLeft % 3600) / 60) + ":";
        }

        // Remaining seconds to always show, e.g. "1 minutes and 0 seconds left" and "5 seconds left"
        returning += (timeLeft % 60);
        return returning;
    }

    public static void broadcastFishMessage(EMFMessage message, Player referencePlayer, boolean actionBar) {
        String plain = message.getPlainTextMessage();
        Competition activeComp = Competition.getCurrentlyActive();

        if (plain.isEmpty() || activeComp == null) {
            EvenMoreFish.getInstance().debug("Formatted (Empty Message) " + plain.isEmpty());
            EvenMoreFish.getInstance().debug("Active Comp is null? " + (activeComp == null));
            return;
        }

        List<? extends Player> validPlayers = getValidPlayers(referencePlayer, activeComp);
        List<String> playerNames = validPlayers.stream().map(Player::getName).toList();
        EvenMoreFish.getInstance().debug("Valid players: " + StringUtils.join(playerNames, ","));

        if (actionBar) {
            validPlayers.forEach(message::sendActionBar);
        } else {
            validPlayers.forEach(message::send);
        }
    }

    private static @NotNull List<? extends Player> getValidPlayers(@NotNull Player referencePlayer, @NotNull Competition activeComp) {
        CompetitionFile activeCompetitionFile = activeComp.getCompetitionFile();

        // Get the list of online players once and store in a variable.
        Stream<? extends Player> validPlayers = Bukkit.getOnlinePlayers().stream();

        // Combine checks for fishing rod and broadcast range, to avoid unnecessary filtering.
        if (activeCompetitionFile.shouldBroadcastOnlyRods() || activeCompetitionFile.getBroadcastRange() > -1) {
            validPlayers = validPlayers.filter(player -> {
                boolean isRodHolder = !activeCompetitionFile.shouldBroadcastOnlyRods() || isHoldingMaterial(player, Material.FISHING_ROD);
                boolean isInRange = activeCompetitionFile.getBroadcastRange() <= -1 || isWithinRange(referencePlayer, player, activeCompetitionFile.getBroadcastRange());
                return isRodHolder && isInRange;
            });
        }

        return validPlayers.toList();
    }


    public static boolean isHoldingMaterial(@NotNull Player player, @NotNull Material material) {
        return player.getInventory().getItemInMainHand().getType().equals(material)
                || player.getInventory().getItemInOffHand().getType().equals(material);
    }

    private static boolean isWithinRange(Player player1, Player player2, int rangeSquared) {
        return player1.getWorld() == player2.getWorld() && player1.getLocation().distanceSquared(player2.getLocation()) <= rangeSquared;
    }

    /**
     * Determines whether the bait has the emf nbt tag "bait:", this can be used to decide whether this is a bait that
     * can be applied to a rod or not.
     *
     * @param item The item being considered.
     * @return Whether this ItemStack is a bait.
     */
    public static boolean isBaitObject(ItemStack item) {
        if (item.getItemMeta() != null) {
            return NbtUtils.hasKey(item, NbtKeys.EMF_BAIT);
        }

        return false;
    }

    /**
     * Gets the first Character from a given String
     *
     * @param string      The String to use.
     * @param defaultChar The default character to use if an exception is thrown.
     * @return The first Character from the String
     */
    public static char getCharFromString(@NotNull String string, char defaultChar) {
        try {
            return string.toCharArray()[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return defaultChar;
        }
    }

    public static @Nullable Biome getBiome(@NotNull String keyString) {
        // Force lowercase
        keyString = keyString.toLowerCase();
        // If no namespace, assume minecraft
        if (!keyString.contains(":")) {
            keyString = "minecraft:" + keyString;
        }
        // Get the key and check if null
        NamespacedKey key = NamespacedKey.fromString(keyString);
        if (key == null) {
            EvenMoreFish.getInstance().getLogger().severe(keyString + " is not a valid biome.");
            return null;
        }
        // Get the biome and check if null
        Biome biome = Registry.BIOME.get(key);
        if (biome == null) {
            EvenMoreFish.getInstance().getLogger().severe(keyString + " is not a valid biome.");
            return null;
        }
        return biome;
    }

    /**
     * Calculates the total weight of a list of fish, applying a boost to specific fish if applicable.
     *
     * @param fishList    The list of fish to process.
     * @param boostRate   The boost multiplier for certain fish. If set to -1, only boosted fish are considered.
     * @param boostedFish The list of fish that should receive the boost. Can be null if no boost is applied.
     * @return The total calculated weight.
     */
    public static double getTotalWeight(List<Fish> fishList, double boostRate, List<Fish> boostedFish) {
        double totalWeight = 0;
        boolean applyBoost = boostRate != -1 && boostedFish != null;

        for (Fish fish : fishList) {
            // When boostRate is -1, we need to guarantee a fish, so fishList has already been filtered
            // to only contain boosted fish. Otherwise, check if the fish should receive a boost.
            boolean isBoosted = applyBoost && boostedFish.contains(fish);

            // If the fish has no weight, assign a default weight of 1.
            double weight = fish.getWeight();
            double baseWeight = (weight == 0.0d) ? 1 : weight;

            // Apply the boost if applicable.
            if (isBoosted) {
                totalWeight += baseWeight * boostRate;
            } else {
                totalWeight += baseWeight;
            }
        }

        return totalWeight;
    }


    public static @Nullable DayOfWeek getDay(@NotNull String day) {
        try {
            return DayOfWeek.valueOf(day.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static @Nullable Integer getInteger(@NotNull String intString) {
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static boolean classExists(@NotNull String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    public static @Nullable ItemStack getCustomItem(@NotNull String materialString) {
        if (!materialString.contains(":")) {
            return null;
        }
        try {
            final String[] split = materialString.split(":", 2);
            final String prefix = split[0];
            final String id = split[1];
            EvenMoreFish.getInstance().debug("GET ITEM for Addon(%s) Id(%s)".formatted(prefix, id));
            return ItemAddon.getItem(prefix, id);
        } catch (ArrayIndexOutOfBoundsException exception) {
            return null;
        }
    }

    /**
     * Gets an ItemStack from a string. If the string contains a colon, it is assumed to be an addon string.
     * @param materialString The string to parse.
     * @return The ItemStack, or null if the material is invalid.
     */
    public static @Nullable ItemStack getItem(@NotNull final String materialString) {
        // Colon assumes an addon item
        if (materialString.contains(":")) {
            return getCustomItem(materialString);
        }

        Material material = ItemUtils.getMaterial(materialString);
        if (material == null) {
            return null;
        }

        return new ItemStack(material);
    }

    public static @NotNull ItemStack getSkullFromBase64(String base64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "EMFSkull");
            profile.setProperty(new ProfileProperty("textures", base64));
            meta.setPlayerProfile(profile);
        });
        return skull;
    }

    public static @NotNull ItemStack getSkullFromUUID(UUID uuid) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(uuid, "EMFSkull");
            meta.setPlayerProfile(profile);
        });
        return skull;
    }

    /**
     * Sorts a double value by rounding it to the provided amount of decimal places.
     *
     * @param value The double value to be sorted.
     * @param places The amount of decimal places to round to.
     * @return The rounded double value with the provided amount of decimal places.
     */
    public static double roundDouble(final double value, final int places) {
        return new BigDecimal(value)
            .setScale(places, RoundingMode.HALF_UP)
            .doubleValue();
    }

    /**
     * Formats a double value by applying the configured decimal format.
     *
     * @param value The double value to be formatted.
     * @return The formatted double
     */
    public static Component formatDouble(@NotNull final String formatStr, final double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(MainConfig.getInstance().getDecimalLocale());
        DecimalFormat format = new DecimalFormat(formatStr, symbols);
        return Component.text(format.format(value));
    }

    /**
     * Sorts a float value by rounding it to the provided amount of decimal places.
     *
     * @param value The float value to be sorted.
     * @param places The amount of decimal places to round to.
     * @return The rounded float value with the provided amount of decimal places.
     */
    public static float roundFloat(final float value, int places) {
        return new BigDecimal(value)
            .setScale(places, RoundingMode.HALF_UP)
            .floatValue();
    }

    /**
     * Formats a float value by applying the configured decimal format.
     *
     * @param value The float value to be formatted.
     * @return The formatted float
     */
    public static String formatFloat(@NotNull final String formatStr, final float value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(MainConfig.getInstance().getDecimalLocale());
        DecimalFormat format = new DecimalFormat(formatStr, symbols);
        return format.format(value);
    }

    /**
     * Checks if a provided String is a legacy string by stripping MiniMessage tags and seeing if the String is the same.
     * @return Whether this String is using legacy color codes.
     */
    public static boolean isLegacyString(@NotNull String string) {
        String stripped = EMFMessage.MINIMESSAGE.stripTags(string);
        return string.equals(stripped);
    }

    public static @NotNull Component parsePlaceholderAPI(@NotNull Component component, @Nullable OfflinePlayer target) {
        if (!EvenMoreFish.getInstance().isUsingPAPI()) {
            return component;
        }
        TextReplacementConfig trc = TextReplacementConfig.builder()
            .match(PlaceholderAPI.getPlaceholderPattern())
            .replacement((matchResult, builder) -> {
                String matched = matchResult.group();
                Component parsed = EMFMessage.LEGACY_SERIALIZER.deserialize(
                    PlaceholderAPI.setPlaceholders(target, matched)
                );
                return builder.append(parsed);
            })
            .build();
        return component.replaceText(trc);
    }

    public static boolean componentContainsString(@NotNull Component component, @NotNull String string) {
        return EMFMessage.PLAINTEXT_SERIALIZER.serialize(component).contains(string);
    }

    public static @NotNull Component decorateIfAbsent(@NotNull Component component, @NotNull TextDecoration decoration, @NotNull TextDecoration.State state) {
        TextDecoration.State oldState = component.decoration(decoration);
        if (oldState == TextDecoration.State.NOT_SET) {
            return component.decoration(decoration, state);
        }
        return component;
    }

    /**
     * @param colour The original colour
     * @return A MiniMessage string turned into a format key for use in configs.
     */
    public static @NotNull String getFormat(@NotNull String colour) {
        int openingTagEnd = colour.indexOf(">");

        if (openingTagEnd == -1) {
            return colour + "{name}";  // No tags at all
        }

        // At least one opening tag exists
        if (colour.contains("</")) {
            // Case: <tag>content</tag> → <tag>{name}content</tag>
            return colour.substring(0, openingTagEnd + 1) + "{name}" + colour.substring(openingTagEnd + 1);
        }

        // Case: <tag> → <tag>{name}
        return colour.substring(0, openingTagEnd + 1) + "{name}";
    }

}
