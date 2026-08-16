package org.evenmorefish.fish;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.ItemConfigResolver;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import com.oheers.fish.plugin.loading.EMFVersionProvider;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.commands.admin.AdminCommand;
import org.evenmorefish.fish.commands.main.MainCommand;
import org.evenmorefish.fish.items.configs.FireResistantItemConfig;
import org.evenmorefish.fish.items.configs.HideTooltipItemConfig;
import org.evenmorefish.fish.items.configs.ItemRarityItemConfig;
import org.evenmorefish.fish.items.configs.MaxStackSizeItemConfig;
import org.evenmorefish.fish.items.configs.ModernGlowingItemConfig;
import org.evenmorefish.fish.nbt.ItemStackNBTHolder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class EMFVersion extends EMFVersionProvider {

    private static final Method deserializeItem;

    static {
        try {
            deserializeItem = CraftMagicNumbers.class.getDeclaredMethod("deserializeItem", CompoundTag.class);
            deserializeItem.setAccessible(true);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Failed to load EvenMoreFish.", exception);
        }
    }

    public EMFVersion(@NonNull EMFPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        registerItemConfigs();
    }

    private void registerItemConfigs() {
        ItemConfigResolver inst = ItemConfigResolver.getInstance();
        inst.setGlowingResolver(ModernGlowingItemConfig::new);
        inst.setFireResistantResolver(FireResistantItemConfig::new);
        inst.setHideTooltipResolver(HideTooltipItemConfig::new);
        inst.setItemRarityResolver(ItemRarityItemConfig::new);
        inst.setMaxStackSizeResolver(MaxStackSizeItemConfig::new);
    }

    @Override
    public void loadCommands() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            event.registrar().register(new MainCommand().get(), MainConfig.getInstance().getMainCommandAliases());
            if (MainConfig.getInstance().isAdminShortcutCommandEnabled()) {
                String shortcut = MainConfig.getInstance().getAdminShortcutCommandName();
                event.registrar().register(new AdminCommand(shortcut).get());
            }
        }));
    }

    @Override
    public void resendCommands() {
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NonNull ItemStack getSkullFromUUID(@NonNull UUID uuid) {
        ResolvableProfile profile = ResolvableProfile.resolvableProfile()
            .uuid(uuid)
            .build();
        TooltipDisplay tooltip = TooltipDisplay.tooltipDisplay()
            .addHiddenComponents(DataComponentTypes.PROFILE)
            .build();

        ItemStack skull = ItemStack.of(Material.PLAYER_HEAD);
        skull.setData(DataComponentTypes.PROFILE, profile);
        skull.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltip);
        return skull;
    }

    @SuppressWarnings("UnstableApiUsage")
    @NonNull
    @Override
    public ItemStack getSkullFromBase64(@NonNull String base64) {
        ResolvableProfile profile = ResolvableProfile.resolvableProfile()
            .uuid(FishUtils.B64_SKULL_UUID)
            .addProperty(new ProfileProperty("textures", base64))
            .build();

        ItemStack skull = ItemStack.of(Material.PLAYER_HEAD);
        skull.setData(DataComponentTypes.PROFILE, profile);
        return skull;
    }

    @Override
    public @NonNull NBTHolder<ItemStack> createItemStackNbtHolder(@NonNull ItemStack item) {
        return new ItemStackNBTHolder(item);
    }

    @Nullable
    @Override
    public ItemStack deserializeItemStack(@NonNull String raw) {
        try {
            CompoundTag tag = net.minecraft.nbt.TagParser.parseCompoundFully(raw);
            return (ItemStack) deserializeItem.invoke(CraftMagicNumbers.INSTANCE, tag);
        } catch (CommandSyntaxException | IllegalAccessException | InvocationTargetException exception) {
            Logging.warn("Failed to parse an ItemStack from raw NBT: " + raw);
            return null;
        }
    }

    @NonNull
    @Override
    public String serializeItemStack(@NonNull ItemStack item) {
        return CraftMagicNumbers.INSTANCE.serializeItemAsJson(item).toString();
    }

    // Ignored Methods

    @Override
    public void enable() {}

    @Override
    public void reload() {}

    @Override
    public void enableCommands() {}

    @Override
    public void registerCommands() {}

    @Override
    public void disableCommands() {}

}
