package com.oheers.fish;

import ca.spottedleaf.moonrise.common.PlatformHooks;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.commands.admin.AdminCommand;
import com.oheers.fish.commands.main.MainCommand;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.ItemConfigResolver;
import com.oheers.fish.items.configs.FireResistantItemConfig;
import com.oheers.fish.items.configs.HideTooltipItemConfig;
import com.oheers.fish.items.configs.ItemRarityItemConfig;
import com.oheers.fish.items.configs.MaxStackSizeItemConfig;
import com.oheers.fish.items.configs.ModernGlowingItemConfig;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import com.oheers.fish.nbt.ItemStackNBTHolder;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.TooltipDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import com.oheers.fish.plugin.loading.EMFVersionProvider;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class EMFVersion extends EMFVersionProvider {

    public EMFVersion(@NotNull EMFPlugin plugin) {
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

    @SuppressWarnings("UnstableApiUsage")
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

    @Override
    public @NotNull ItemStack getSkullFromUUID(@NotNull UUID uuid) {
        ItemStack skull = ItemStack.of(Material.PLAYER_HEAD);

        // Set our data using NMS
        net.minecraft.world.item.ItemStack handle = ((CraftItemStack) skull).handle;
        handle.set(DataComponents.PROFILE, new ResolvableProfile(Optional.empty(), Optional.of(uuid), new PropertyMap()));
        TooltipDisplay display = handle.getOrDefault(
            DataComponents.TOOLTIP_DISPLAY,
            new TooltipDisplay(false, new ReferenceLinkedOpenHashSet<>()) // NMS seems to use this set, so we do the same.
        );
        // This hides the "dynamic" text in the tooltip. Do not remove.
        display = display.withHidden(DataComponents.PROFILE, true);
        handle.set(DataComponents.TOOLTIP_DISPLAY, display);

        return skull;
    }

    @NotNull
    @Override
    public ItemStack getSkullFromBase64(@NotNull String base64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(FishUtils.B64_SKULL_UUID, null);
            profile.setProperty(new ProfileProperty("textures", base64));
            meta.setPlayerProfile(profile);
        });
        return skull;
    }

    @Override
    public @NotNull NBTHolder<ItemStack> createItemStackNbtHolder(@NotNull ItemStack item) {
        return new ItemStackNBTHolder(item);
    }

    @Nullable
    @Override
    public ItemStack deserializeItemStack(@NotNull String raw) {
        // Manually deserializes as the newer CraftMagicNumbers methods aren't on this version.
        try {
            CompoundTag tag = net.minecraft.nbt.TagParser.parseCompoundFully(raw);
            int dataVersion = tag.getIntOr("DataVersion", 0);
            tag = PlatformHooks.get().convertNBT(
                References.ITEM_STACK,
                DataFixers.getDataFixer(),
                tag,
                dataVersion,
                CraftMagicNumbers.INSTANCE.getDataVersion()
            );
            return tag.getStringOr("id", "minecraft:air").equals("minecraft:air")
                ? CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.EMPTY)
                : CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.CODEC.parse(CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow());
        } catch (CommandSyntaxException | IllegalStateException exception) {
            Logging.warn("Failed to parse an ItemStack from raw NBT: " + raw);
            Logging.error(exception.getMessage(), exception);
            return null;
        }
    }

    @NotNull
    @Override
    public String serializeItemStack(@NotNull ItemStack item) {
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
