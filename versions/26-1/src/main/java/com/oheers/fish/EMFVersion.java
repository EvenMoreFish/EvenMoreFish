package com.oheers.fish;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.items.ItemConfigProviderImpl;
import com.oheers.fish.items.configs.ItemConfigProvider;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import com.oheers.fish.nbt.ItemStackNBTHolder;
import com.oheers.fish.plugin.loading.EMFVersionProvider;
import com.oheers.fish.commands.admin.AdminCommand;
import com.oheers.fish.commands.main.MainCommand;
import com.oheers.fish.config.MainConfig;
import dev.dejvokep.boostedyaml.block.implementation.Section;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public EMFVersion(@NotNull EMFPlugin plugin) {
        super(plugin);
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
    public @NotNull ItemStack getSkullFromUUID(@NotNull UUID uuid) {
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
    @NotNull
    @Override
    public ItemStack getSkullFromBase64(@NotNull String base64) {
        ResolvableProfile profile = ResolvableProfile.resolvableProfile()
            .uuid(FishUtils.B64_SKULL_UUID)
            .addProperty(new ProfileProperty("textures", base64))
            .build();

        ItemStack skull = ItemStack.of(Material.PLAYER_HEAD);
        skull.setData(DataComponentTypes.PROFILE, profile);
        return skull;
    }

    @Override
    public @NotNull NBTHolder<ItemStack> createItemStackNbtHolder(@NotNull ItemStack item) {
        return new ItemStackNBTHolder(item);
    }

    @Nullable
    @Override
    public ItemStack deserializeItemStack(@NotNull String raw) {
        try {
            CompoundTag tag = net.minecraft.nbt.TagParser.parseCompoundFully(raw);
            return (ItemStack) deserializeItem.invoke(CraftMagicNumbers.INSTANCE, tag);
        } catch (CommandSyntaxException | IllegalAccessException | InvocationTargetException exception) {
            Logging.warn("Failed to parse an ItemStack from raw NBT: " + raw);
            return null;
        }
    }

    @NotNull
    @Override
    public String serializeItemStack(@NotNull ItemStack item) {
        return CraftMagicNumbers.INSTANCE.serializeItemAsJson(item).toString();
    }

    @Override
    public @NotNull ItemConfigProvider createItemConfigProvider(@NotNull Section section) {
        return new ItemConfigProviderImpl(section);
    }

    // Ignored Methods

    @Override
    public void load() {}

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
