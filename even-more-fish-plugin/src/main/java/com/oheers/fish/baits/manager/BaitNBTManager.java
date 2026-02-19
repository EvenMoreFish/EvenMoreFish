package com.oheers.fish.baits.manager;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.model.ApplicationResult;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.utils.WeightedRandom;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class BaitNBTManager {
    public static final int UNLIMITED_BAIT = -1;
    public static final String BAIT_SEPARATOR = ":";
    public static final String BAIT_ENTRY_DELIMITER = ",";

    // Our line identifier. This is U+200C ZERO WIDTH NON-JOINER and is invisible
    public static final String LINE_IDENTIFIER = "\u200C";

    private BaitNBTManager() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks whether the item has nbt to suggest it is a bait object.
     *
     * @param itemStack The item stack that could potentially be a bait.
     * @return If the item stack is a bait or not (or if itemStack is null)
     */
    public static boolean isBaitObject(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (itemStack.hasItemMeta()) {
            return NbtUtils.hasKey(itemStack, NbtKeys.EMF_BAIT);
        }

        return false;
    }

    /**
     * @param itemStack The item stack that is a bait.
     * @return The name of the bait.
     */
    public static @Nullable String getBaitName(@NotNull ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            return NbtUtils.getString(itemStack, NbtKeys.EMF_BAIT);
        }
        return null;
    }

    /**
     * Gives an ItemStack the nbt required for the plugin to see it as a valid bait that can be applied to fishing rods.
     * It is inadvisable to use a block as a bait, as these will lose their nbt tags if they're placed - and the plugin
     * will forget that it was ever a bait.
     *
     * @param item The item stack being turned into a bait.
     * @param bait The name of the bait to be applied.
     */
    public static ItemStack applyBaitNBT(ItemStack item, String bait) {
        if (item == null) {
            return null;
        }
        NBT.modify(item, nbt -> {
            nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND).setString(NbtKeys.EMF_BAIT, bait);
        });
        return item;
    }

    /**
     * This checks against the item's NBTs to work out whether the fishing rod passed through has applied baits.
     *
     * @param itemStack The fishing rod that could maybe have bait NBTs applied.
     * @return Whether the fishing rod has bait NBT.
     */
    public static boolean isBaitedRod(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        if (itemStack.getType() != Material.FISHING_ROD) {
            return false;
        }

        if (itemStack.hasItemMeta()) {
            return NbtUtils.hasKey(itemStack, NbtKeys.EMF_APPLIED_BAIT);
        }

        return false;
    }

    /**
     * This applies a bait NBT reference to a fishing rod, and also checks whether the bait is already applied,
     * making an effort to increase it rather than apply it.
     *
     * @param item     The fishing rod having its bait applied.
     * @param bait     The name of the bait being applied.
     * @param quantity The number of baits being applied. These must be of the same bait.
     * @return An ApplicationResult containing the updated "item" itemstack and the remaining baits for the cursor.
     * @throws MaxBaitsReachedException When too many baits are tried to be applied to a fishing rod.
     * @throws MaxBaitReachedException  When one of the baits has hit maximum set by max-baits in baits.yml
     */
    public static ApplicationResult applyBaitedRodNBT(ItemStack item, BaitHandler bait, int quantity) throws MaxBaitsReachedException, MaxBaitReachedException {
        boolean doingLoreStuff = MainConfig.getInstance().getBaitAddToLore();
        AtomicBoolean maxBait = new AtomicBoolean(false);
        AtomicInteger cursorModifier = new AtomicInteger();

        StringBuilder combined = new StringBuilder();
        if (isBaitedRod(item)) {
            removeOldLoreIfNecessary(item, bait);

            boolean foundBait = applyToExistingBaits(item, bait, quantity, combined,cursorModifier,maxBait);

            // We can manage the last character not being a colon if we have to add it in ourselves.
            if (!foundBait) {
                addNewBait(item, bait, quantity, combined, cursorModifier, maxBait, doingLoreStuff);
            } else {
                if (!combined.isEmpty()) {
                    combined.deleteCharAt(combined.length() - 1);
                }
            }
            updateNBT(item, combined);
        } else {
            applyInitialBait(item, bait, quantity, combined, cursorModifier, maxBait);
        }

        if (doingLoreStuff && !combined.isEmpty()) {
            item.editMeta(meta -> meta.lore(newApplyLore(item)));
        }

        if (maxBait.get()) {
            throw new MaxBaitReachedException(bait, new ApplicationResult(item, cursorModifier.get()));
        }

        return new ApplicationResult(item, cursorModifier.get());
    }

    private static void addNewBait(
            ItemStack item, BaitHandler bait, int quantity,
            StringBuilder combined, AtomicInteger cursorModifier, AtomicBoolean maxBait,
            boolean doingLoreStuff) throws MaxBaitsReachedException {

        if (getNumBaitsApplied(item) >= MainConfig.getInstance().getBaitsPerRod()) {
            if (doingLoreStuff) {
                item.editMeta(meta -> meta.lore(newApplyLore(item)));
            }
            throw new MaxBaitsReachedException("Max baits reached.", new ApplicationResult(item, cursorModifier.get()));
        }

        int maxApplications = bait.getBaitData().maxApplications();
        if (quantity > maxApplications && maxApplications != UNLIMITED_BAIT) {
            cursorModifier.set(-maxApplications);
            combined.append(bait.getId()).append(BAIT_SEPARATOR).append(maxApplications);
            maxBait.set(true);
        } else {
            combined.append(bait.getId()).append(BAIT_SEPARATOR).append(quantity);
            cursorModifier.set(-quantity);
        }
    }


    private static void removeOldLoreIfNecessary(@NotNull ItemStack item, BaitHandler bait) {
        try {
            item.editMeta(meta -> meta.lore(deleteOldLore(item)));
        } catch (IndexOutOfBoundsException ex) {
            EvenMoreFish.getInstance().getLogger().severe(
                    "Failed to apply bait: " + bait.getId() + " to a user's fishing rod. Check baits.yml format.");
            throw new RuntimeException("Lore removal failed.", ex); // Optionally make this a custom exception
        }
    }

    private static boolean applyToExistingBaits(
            ItemStack item, BaitHandler bait, int quantity,
            StringBuilder combined, AtomicInteger cursorModifier, AtomicBoolean maxBait) {

        boolean foundBait = false;
        for (String baitName : NbtUtils.getBaitArray(item)) {
            String[] split = baitName.split(BAIT_SEPARATOR);
            String baitId = split[0];

            int baitQuantity = "∞".equals(split[1]) ? UNLIMITED_BAIT : Integer.parseInt(split[1]);

            if (baitId.equals(bait.getId())) {
                if (bait.getBaitData().infinite() || baitQuantity == UNLIMITED_BAIT) {
                    combined.append(baitId).append(":∞,");
                } else {
                    int newQuantity = baitQuantity + quantity;
                    int maxApplications = bait.getBaitData().maxApplications();

                    if (newQuantity > maxApplications && maxApplications != UNLIMITED_BAIT) {
                        combined.append(baitId).append(BAIT_SEPARATOR).append(maxApplications).append(BAIT_ENTRY_DELIMITER);
                        cursorModifier.set(-maxApplications + (newQuantity - quantity));
                        maxBait.set(true);
                    } else if (newQuantity != 0) {
                        combined.append(baitId).append(BAIT_SEPARATOR).append(newQuantity).append(BAIT_ENTRY_DELIMITER);
                        cursorModifier.set(-quantity);
                    }
                }
                foundBait = true;
            } else {
                combined.append(baitName).append(BAIT_ENTRY_DELIMITER);
            }
        }
        return foundBait;
    }

    private static void updateNBT(ItemStack item, StringBuilder combined) {
        NBT.modify(item, nbt -> {
            ReadWriteNBT compound = nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND);
            if (combined.isEmpty()) {
                compound.removeKey(NbtKeys.EMF_APPLIED_BAIT);
            } else {
                compound.setString(NbtKeys.EMF_APPLIED_BAIT, combined.toString());
            }
        });
    }

    private static void applyInitialBait(
            ItemStack item, BaitHandler bait, int quantity,
            StringBuilder combined, AtomicInteger cursorModifier, AtomicBoolean maxBait) {

        NBT.modify(item, nbt -> {
            ReadWriteNBT compound = nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND);
            int maxApplications = bait.getBaitData().maxApplications();

            if (quantity > maxApplications && maxApplications != UNLIMITED_BAIT) {
                combined.append(bait.getId()).append(BAIT_SEPARATOR).append(maxApplications);
                cursorModifier.set(-maxApplications);
                maxBait.set(true);
            } else {
                combined.append(bait.getId()).append(BAIT_SEPARATOR).append(quantity);
                cursorModifier.set(-quantity);
            }

            compound.setString(NbtKeys.EMF_APPLIED_BAIT, combined.toString());
        });
    }


    /**
     * This fetches a random bait applied to the rod, based on the application-weight of the baits (if they exist). The
     * weight defaults to "1" if there is no value applied for them.
     *
     * @param fishingRod The fishing rod.
     * @return A random bait applied to the fishing rod.
     */
    public static @Nullable BaitHandler randomBaitApplication(ItemStack fishingRod) {
        if (fishingRod == null || fishingRod.getItemMeta() == null) {
            return null;
        }

        String[] baitNameList = NbtUtils.getBaitArray(fishingRod);
        List<BaitHandler> baitList = new ArrayList<>();

        for (String baitName : baitNameList) {

            BaitHandler bait = BaitManager.getInstance().getBait(baitName.split(BAIT_SEPARATOR)[0]);
            if (bait != null) {
                baitList.add(bait);
            }

        }

        // Fix IndexOutOfBoundsException caused by the list being empty.
        if (baitList.isEmpty()) {
            return null;
        }

        return WeightedRandom.pick(
                baitList,
                bait -> bait.getBaitData().applicationWeight(),
                EvenMoreFish.getInstance().getRandom()
        );
    }

    /**
     * Calculates a random bait to throw out based on their catch-weight. It uses the same weight algorithm as
     * randomBaitApplication, using the baits from the main class in the baits list.
     *
     * @return A random bait weighted by its catch-weight.
     */
    public static Optional<BaitHandler> randomBaitCatch() {

        Map<String, BaitHandler> baitMap = BaitManager.getInstance().getItemMap();
        List<BaitHandler> baitList = baitMap.values().stream()
            .filter(bait -> bait.getBaitData().canBeCaught())
            .toList();
        
        // Fix IndexOutOfBoundsException caused by the list being empty.
        if (baitList.isEmpty()) {

            return Optional.empty();
        }

        return Optional.of(WeightedRandom.pick(baitList, bait -> bait.getBaitData().catchWeight(), EvenMoreFish.getInstance().getRandom()));
    }

    /**
     * Runs through the metadata of the rod to try and figure out whether a certain bait is applied or not.
     *
     * @param itemStack The fishing rod in item stack form.
     * @param bait      The name of the bait that could have been applied, must be the same as the time it was applied to the rod.
     * @return If the fishing rod contains the bait or not.
     */
    public static boolean hasBaitApplied(ItemStack itemStack, String bait) {
        if (itemStack == null) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return false;
        }

        String[] baitList = NbtUtils.getBaitArray(itemStack);

        for (String appliedBait : baitList) {
            if (appliedBait.split(BAIT_SEPARATOR)[0].equals(bait)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes all the baits stored in the nbt of the fishing rod. It then returns the total number of baits deleted just
     * incase you fancy doing something special with this number. It first checks whether there's any baits actually on
     * the rod in the first place. It loops through each bait stored to find out how many will be deleted then simply removes
     * the namespacedkey from the fishing rod.
     *
     * @param itemStack The fishing rod with baits on it/
     * @return The number of baits that were deleted in total.
     */
    public static int deleteAllBaits(ItemStack itemStack) {
        if (!NbtUtils.hasKey(itemStack, NbtKeys.EMF_APPLIED_BAIT)) {
            return 0;
        }

        String[] baitList = NbtUtils.getBaitArray(itemStack);
        int totalDeleted = Arrays.stream(baitList)
                .filter(Objects::nonNull)
                .mapToInt(bait -> {
                            String[] parts = bait.split(BAIT_SEPARATOR);
                            return getDeletedFromQuantityString(parts[1]);
                        }
                )
                .sum();

        NBT.modify(itemStack, nbt -> {
            nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND).removeKey(NbtKeys.EMF_APPLIED_BAIT);
        });

        return totalDeleted;
    }

    private static int getDeletedFromQuantityString(final String quantityStr) {
        if (!quantityStr.equals("∞")) {
            return Integer.parseInt(quantityStr);
        }

        return  1; // Count infinite baits as 1
    }

    public static List<Component> newApplyLore(ItemStack itemStack) {
        // Mark this item as having reformatted lore
        NBT.modify(itemStack, nbt -> {
            ReadWriteNBT compound = nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND);
            compound.setBoolean(NbtKeys.EMF_BAIT_REFORMATTED, true);
        });

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return Collections.emptyList();
        }

        List<Component> lore = meta.lore();

        if (lore == null) {
            lore = new ArrayList<>();
        }

        EMFListMessage format = ConfigMessage.BAIT_ROD_LORE.getMessage().toListMessage();

        Supplier<EMFListMessage> baitVariable = () -> {
            List<Component> list = new ArrayList<>();

            String rodNBT = NbtUtils.getString(itemStack, NbtKeys.EMF_APPLIED_BAIT);

            if (rodNBT == null || rodNBT.isEmpty()) {
                return EMFListMessage.empty();
            }

            final String[] baitRodNbt = rodNBT.split(BAIT_ENTRY_DELIMITER);
            for (String bait : baitRodNbt) {
                EMFSingleMessage baitFormat = ConfigMessage.BAIT_BAITS.getMessage().toSingleMessage();
                final String[] parts = bait.split(BAIT_SEPARATOR);
                if (parts.length == 2) {
                    baitFormat.setAmount(parts[1]);
                } else {
                    baitFormat.setAmount("N/A");
                }

                baitFormat.setBait(getBaitFormatted(parts[0]));
                list.add(baitFormat.getComponentMessage());
            }

            if (MainConfig.getInstance().getBaitShowUnusedSlots()) {
                for (int i = baitRodNbt.length; i < MainConfig.getInstance().getBaitsPerRod(); i++) {
                    list.add(ConfigMessage.BAIT_UNUSED_SLOT.getMessage().getComponentMessage());
                }
            }

            return EMFListMessage.ofList(list);
        };
        format.setVariableWithListInsertion("{baits}", baitVariable.get());

        format.setCurrentBaits(Integer.toString(getNumBaitsApplied(itemStack)));
        format.setMaxBaits(Integer.toString(MainConfig.getInstance().getBaitsPerRod()));

        // Add the lore with the line identifier added to the start of each line
        lore.addAll(
            format.getComponentListMessage().stream()
                .map(component -> Component.text(LINE_IDENTIFIER).append(component))
                .toList()
        );

        return lore;
    }

    /**
     * This deletes all the old lore inserted by the plugin to the baited fishing rod. If the config value for the lore
     * format had lines added/removed this will break the old rods.
     *
     * @param itemStack The lore of the itemstack having the bait section of its lore removed.
     */
    public static List<Component> deleteOldLore(ItemStack itemStack) {
        // Removes old lore from the rod
        if (removeOldLoreFormat(itemStack)) {
            return itemStack.lore();
        }

        List<Component> lore = itemStack.lore();
        if (lore == null || lore.isEmpty()) {
            return Collections.emptyList();
        }

        // Return the lore with all bait lines removed from the rod
        return lore.stream().filter(component ->
            !PlainTextComponentSerializer.plainText().serialize(component).startsWith(LINE_IDENTIFIER)
        ).toList();
    }

    /**
     * Works out how many baits are applied to an object based on the nbt data.
     *
     * @param itemStack The fishing rod with baits applied
     * @return How many baits have been applied to this fishing rod.
     */
    private static int getNumBaitsApplied(ItemStack itemStack) {
        String rodNBT = NbtUtils.getString(itemStack, NbtKeys.EMF_APPLIED_BAIT);
        if (rodNBT == null) {
            return 1;
        }

        return rodNBT.split(",").length;
    }

    /**
     * Checks the bait from baitID to see if it has a displayname and returns that if necessary - else it just returns
     * the baitID itself.
     *
     * @param baitID The baitID the bait is registered under in baits.yml
     * @return How the bait should look in the lore of the fishing rod, for example.
     */
    private static EMFSingleMessage getBaitFormatted(String baitID) {
        BaitHandler bait = BaitManager.getInstance().getBait(baitID);
        if (bait == null) {
            EvenMoreFish.getInstance().getLogger().warning(() -> "Bait %s is not a valid bait!".formatted(baitID));
            return EMFSingleMessage.fromString("Invalid Bait");
        }
        return EMFSingleMessage.fromString(bait.getDisplayName());
    }

    // Conversion methods

    /**
     * Removes all lore from the fishing rod
     */
    private static boolean removeOldLoreFormat(@NotNull ItemStack item) {
        if (NbtUtils.hasKey(item, NbtKeys.EMF_BAIT_REFORMATTED)) {
            return false;
        }

        if (!item.hasItemMeta() || item.getItemMeta() == null || !item.getItemMeta().hasLore()) {
            return true;
        }

        List<Component> lore = item.lore();
        if (lore == null || lore.isEmpty()) {
            return true;
        }

        if (MainConfig.getInstance().getBaitShowUnusedSlots()) {
            // starting at 1, because at least one bait replacing {baits} is repeated.
            int maxBaits = MainConfig.getInstance().getBaitsPerRod() + ConfigMessage.BAIT_ROD_LORE.getMessage().getPlainTextListMessage().size();
            for (int i = 1; i < maxBaits; i++) {
                try {
                    lore.remove(lore.size() - 1);
                } catch (IndexOutOfBoundsException exception) {
                    break;
                }
            }
        } else {
            // starting at 1, because at least one bait replacing {baits} is repeated.
            int numBaitsApplied = getNumBaitsApplied(item) + ConfigMessage.BAIT_ROD_LORE.getMessage().getPlainTextListMessage().size();
            //compliant version
            for (int i = 1; i < numBaitsApplied; i++) {
                try {
                lore.remove(lore.size() - 1);
                } catch (IndexOutOfBoundsException exception) {
                    break;
                }
            }
        }

        item.lore(lore);
        NBT.modify(item, nbt -> {
            ReadWriteNBT compound = nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND);
            compound.setBoolean(NbtKeys.EMF_BAIT_REFORMATTED, true);
        });

        return true;
    }

}