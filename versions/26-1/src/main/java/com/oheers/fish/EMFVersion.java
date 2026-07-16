package com.oheers.fish;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.items.nbt.NBTHolder;
import com.oheers.fish.nbt.NBTProviderImpl;
import com.oheers.fish.nbt.SkullNBTHolder;
import com.oheers.fish.nbt.holder.BlockStateNBTHolder;
import com.oheers.fish.nbt.holder.ItemStackNBTHolder;
import com.oheers.fish.plugin.loading.EMFVersionProvider;
import com.oheers.fish.commands.admin.AdminCommand;
import com.oheers.fish.commands.main.MainCommand;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.ItemConfigResolver;
import com.oheers.fish.items.configs.FireResistantItemConfig;
import com.oheers.fish.items.configs.HideTooltipItemConfig;
import com.oheers.fish.items.configs.ItemRarityItemConfig;
import com.oheers.fish.items.configs.MaxStackSizeItemConfig;
import com.oheers.fish.items.configs.ModernGlowingItemConfig;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public @NotNull NBTHolder<ItemStack> createItemStackNbtHolder(@NotNull ItemStack item) {
        return new ItemStackNBTHolder(item);
    }

}
