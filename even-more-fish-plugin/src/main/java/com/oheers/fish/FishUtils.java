package com.oheers.fish;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.utils.DurationFormatter;
import com.oheers.fish.utils.ItemUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.tr7zw.changeme.nbtapi.NBT;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Skull;
import org.bukkit.boss.BarStyle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class FishUtils {

    private static final DurationFormatter durationFormatter = new DurationFormatter(TimeUnit.SECONDS);
    private static final UUID B64_SKULL_UUID = UUID.fromString("07cd5534-e542-4fbf-861c-67a144ecf776");

    private FishUtils() {
        throw new UnsupportedOperationException();
    }

    // checks for the "emf-fish-name" nbt tag, to determine if this ItemStack is a fish or not.
    public static boolean isFish(@Nullable ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        return NbtUtils.hasKey(item, NbtKeys.EMF_FISH_NAME);
    }

    public static boolean isFish(@Nullable Skull skull) {
        if (skull == null) {
            return false;
        }
        return NbtUtils.hasKey(skull, NbtKeys.EMF_FISH_NAME);
    }

    public static @Nullable Fish getFish(@Nullable ItemStack item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
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
            fish.getFactory().setRandomIndex(randomIndex);
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

    public static @Nullable Fish getFish(@Nullable Skull skull, @Nullable Player fisher) throws InvalidFishException {
        if (skull == null) {
            return null;
        }
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
            fish.getFactory().setRandomIndex(randomIndex);
        }
        if (playerString != null) {
            try {
                fish.setFisherman(UUID.fromString(playerString));
            } catch (IllegalArgumentException exception) {
                fish.setFisherman(null);
            }
        } else if (fisher != null) {
            fish.setFisherman(fisher.getUniqueId());
        }

        return fish;
    }

    public static void giveItems(@NotNull List<ItemStack> items, @NotNull Player player) {
        if (items.isEmpty()) {
            return; // Early return if the list is null or empty
        }

        // Remove null items and avoid modifying the original list
        List<ItemStack> filteredItems = items.stream()
                .filter(Objects::nonNull)
                .toList();

        // Do not proceed if there are no valid items to give
        if (filteredItems.isEmpty()) {
            return;
        }

        // Play item pickup sound
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);

        // Add items to the player's inventory
        Map<Integer, ItemStack> leftoverItems = player.getInventory().addItem(filteredItems.toArray(new ItemStack[0]));

        // Drop any leftover items in the world
        leftoverItems.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
    }


    public static void giveItems(@NotNull ItemStack[] items, @NotNull Player player) {
        giveItems(Arrays.asList(items), player);
    }

    public static void giveItem(@NotNull ItemStack item, @NotNull Player player) {
        giveItems(List.of(item), player);
    }

    public static boolean checkRegion(@NotNull Location location, @NotNull List<String> whitelistedRegions) {
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


    public static @Nullable String getRegionName(@NotNull Location location) {
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


    public static boolean checkWorld(@NotNull Location l) {
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
        return EMFSingleMessage.of(durationFormatter.format(timeLeft));
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

    /**
     * Determines whether the bait has the emf nbt tag "bait:", this can be used to decide whether this is a bait that
     * can be applied to a rod or not.
     *
     * @param item The item being considered.
     * @return Whether this ItemStack is a bait.
     */
    public static boolean isBaitObject(@NotNull ItemStack item) {
        if (!item.isEmpty()) {
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
        Biome biome = getFromBukkitRegistry(keyString, Registry.BIOME);
        if (biome == null) {
            EvenMoreFish.getInstance().getLogger().severe(keyString + " is not a valid biome.");
        }
        return biome;
    }

    public static @Nullable DayOfWeek getDay(@NotNull String day) {
        return getEnumValue(DayOfWeek.class, day);
    }

    public static @Nullable Integer getInteger(@NotNull String intString) {
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static @Nullable String getPlayerName(@Nullable OfflinePlayer player) {
        return player == null ? null : player.getName();
    }

    public static @Nullable String getPlayerName(@Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return getPlayerName(Bukkit.getOfflinePlayer(uuid));
    }

    public static @Nullable String getPlayerName(@Nullable String uuidString) {
        if (uuidString == null) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(uuidString);
            return getPlayerName(uuid);
        } catch (IllegalArgumentException exception) {
            return null;
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
            return EMFRegistry.ITEM_ADDON.getItem(prefix, id);
        } catch (ArrayIndexOutOfBoundsException exception) {
            return null;
        }
    }

    /**
     * Gets an ItemStack from a string. If the string contains a colon, it is assumed to be an addon string.
     * @param materialString The string to parse.
     * @return The ItemStack, or null if the material is invalid.
     */
    public static @Nullable ItemStack getItem(@Nullable final String materialString) {
        if (materialString == null) {
            return null;
        }
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

    public static @NotNull ItemStack getSkullFromBase64(@NotNull String base64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(B64_SKULL_UUID, "EMFSkull");
            profile.setProperty(new ProfileProperty("textures", base64));
            meta.setPlayerProfile(profile);
        });
        return skull;
    }

    public static @NotNull ItemStack getSkullFromUUID(@NotNull UUID uuid) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(uuid, "EMFSkull");
            meta.setPlayerProfile(profile);
        });
        return skull;
    }

    public static @NotNull ItemStack getSkullFromUUIDString(@NotNull String uuidString) {
        try {
            return getSkullFromUUID(UUID.fromString(uuidString));
        } catch (IllegalArgumentException exception) {
            return new ItemStack(Material.PLAYER_HEAD);
        }
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
     * Sorts a float value by rounding it to the provided amount of decimal places.
     *
     * @param value The float value to be sorted.
     * @param places The amount of decimal places to round to.
     * @return The rounded float value with the provided amount of decimal places.
     */
    public static float roundFloat(final float value, int places) {
        return BigDecimal.valueOf(value)
            .setScale(places, RoundingMode.HALF_UP)
            .floatValue();
    }

    /**
     * @param colour The original colour
     * @return A string turned into a format key for use in configs.
     */
    public static @NotNull String getFormat(@NotNull String colour) {
        if (Utils.isLegacy(colour)) {
            // Legacy's formatting makes this insanely simple
            return colour + "{name}";
        } else {
            return getMiniMessageFormat(colour);
        }
    }

    private static @NotNull String getMiniMessageFormat(@NotNull String colour) {
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

    /**
     * Fetches a PotionEffect from a String.
     * @param effectString The String to fetch the PotionEffect from.
     * @param separator The string that separates the individual parts of the effect.
     * @return A PotionEffect built from the provided String, or null if invalid.
     */
    public static @Nullable PotionEffect getPotionEffect(@NotNull String effectString, @NotNull String separator) {

        String[] split = effectString.split(separator);
        if (split.length != 3) {
            Logging.error("Potion effect string is formatted incorrectly. Use \"potion,amplifier,duration\".");
            return null;
        }
        PotionEffectType type = PotionEffectType.getByName(split[0].toUpperCase());
        if (type == null) {
            Logging.error("Potion effect type " + split[0] + " is not valid.");
            return null;
        }
        Integer amplifier = FishUtils.getInteger(split[1]);
        if (amplifier == null || amplifier < 1) {
            Logging.error("Potion effect amplifier " + split[1] + " is not valid.");
            return null;
        }
        Integer duration = FishUtils.getInteger(split[2]);
        if (duration == null || duration < 1) {
            Logging.error("Potion effect duration " + split[2] + " is not valid.");
            return null;
        }
        return new PotionEffect(
            type,
            duration * 20,
            amplifier - 1,
            false
        );
    }

    public static @Nullable PotionEffect getPotionEffect(@NotNull String effectString) {
        PotionEffect effect = getPotionEffect(effectString, ",");
        if (effect != null) {
            return effect;
        }
        // Compatibility with configs using the wrong format we accidentally had in default files.
        return getPotionEffect(effectString, ":");
    }

    public static @Nullable Enchantment getEnchantment(@NotNull String namespace) {
        return getFromBukkitRegistry(namespace, Registry.ENCHANTMENT);
    }

    public static BossBar.Overlay fetchBarStyle(@Nullable String styleStr) {
        if (styleStr == null) {
            return BossBar.Overlay.NOTCHED_10;
        }
        BossBar.Overlay modern = getEnumValue(BossBar.Overlay.class, styleStr);
        if (modern != null) {
            return modern;
        }
        // Manually convert legacy to modern to stay compatible with old configs.
        BarStyle legacy = getEnumValue(BarStyle.class, styleStr);
        if (legacy == null) {
            return BossBar.Overlay.NOTCHED_10;
        }
        return switch (legacy) {
            case SOLID -> BossBar.Overlay.PROGRESS;
            case SEGMENTED_6 -> BossBar.Overlay.NOTCHED_6;
            case SEGMENTED_10 -> BossBar.Overlay.NOTCHED_10;
            case SEGMENTED_12 -> BossBar.Overlay.NOTCHED_12;
            case SEGMENTED_20 -> BossBar.Overlay.NOTCHED_20;
        };
    }

    public static @NotNull <E extends Enum<E>> E getEnumValue(@NotNull Class<E> enumClass, @Nullable String value, @NotNull E def) {
        E enumValue = getEnumValue(enumClass, value);
        if (enumValue == null) {
            return def;
        }
        return enumValue;
    }

    public static @Nullable <E extends Enum<E>> E getEnumValue(@NotNull Class<E> enumClass, @Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static <T extends Keyed> @Nullable T getFromBukkitRegistry(@NotNull String namespace, @NotNull Registry<T> registry) {
        namespace = namespace.toLowerCase();
        NamespacedKey key = NamespacedKey.fromString(namespace);
        if (key == null) {
            return null;
        }
        return registry.get(key);
    }

    public static boolean inventoryHasSpace(@Nullable Inventory inventory) {
        return inventory != null && inventory.firstEmpty() != -1;
    }

}
