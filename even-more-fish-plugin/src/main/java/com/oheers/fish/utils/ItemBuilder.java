package com.oheers.fish.utils;

import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private Material material;
    private String display = null;
    private List<String> lore = new ArrayList<>();
    private boolean glowing = false;

    public ItemBuilder(@NotNull Material material) {
        this.material = material;
    }

    public ItemBuilder(@Nullable String materialName, @NotNull Material defaultMaterial) {
        this.material = ItemUtils.getMaterial(materialName, defaultMaterial);
    }

    public ItemBuilder withMaterial(@NotNull Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder withMaterial(@Nullable String materialName, @NotNull Material defaultMaterial) {
        this.material = ItemUtils.getMaterial(materialName, defaultMaterial);
        return this;
    }

    public ItemBuilder withDisplay(@NotNull String display) {
        this.display = display;
        return this;
    }

    public ItemBuilder withLore(@NotNull List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder addLore(@NotNull String line) {
        this.lore.add(line);
        return this;
    }

    public ItemBuilder addLore(@NotNull List<String> lines) {
        this.lore.addAll(lines);
        return this;
    }

    public ItemBuilder setGlowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }

    public ItemStack build() {
        if (this.material == null) {
            return null;
        }
        ItemStack stack = new ItemStack(this.material);
        stack.editMeta(meta -> {
            if (this.display != null) {
                meta.displayName(EMFSingleMessage.fromString(this.display).getComponentMessage());
            }
            if (!this.lore.isEmpty()) {
                meta.lore(EMFListMessage.fromStringList(this.lore).getComponentListMessage());
            }
        });
        if (this.glowing) {
            ItemUtils.glowify(stack);
        }
        return stack;
    }

}
