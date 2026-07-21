package com.oheers.fish;

import ca.spottedleaf.dataconverter.minecraft.MCDataConverter;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.commands.admin.AdminCommand;
import com.oheers.fish.commands.main.MainCommand;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.ItemConfigProviderImpl;
import com.oheers.fish.items.configs.ItemConfigProvider;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import com.oheers.fish.nbt.ItemStackNBTHolder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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

import java.util.UUID;

public class EMFVersion extends EMFVersionProvider {

    public EMFVersion(@NotNull EMFPlugin plugin) {
        super(plugin);
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
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(uuid);
            meta.setPlayerProfile(profile);
        });
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
            CompoundTag tag = net.minecraft.nbt.TagParser.parseTag(raw);
            int dataVersion = tag.getInt("DataVersion");
            tag = MCDataConverter.convertTag(MCTypeRegistry.ITEM_STACK, tag, dataVersion, CraftMagicNumbers.INSTANCE.getDataVersion());

            String id = tag.contains("id") ? tag.getString("id") : "minecraft:air";
            return id.equals("minecraft:air")
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
