package com.oheers.fish.baits;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.items.config.DisplayNameItemConfig;
import com.oheers.fish.items.config.ItemConfig;
import com.oheers.fish.items.config.LoreItemConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class BaitItemFactory {

    private final String baitId;
    private final List<IRarity> rarities;
    private final List<IFish> fish;
    private final YamlDocument config;

    public BaitItemFactory(String baitId, List<IRarity> rarities, List<IFish> fish, YamlDocument config) {
        this.baitId = baitId;
        this.rarities = rarities;
        this.fish = fish;
        this.config = config;
    }

    public ItemFactory createFactory() {
        ItemFactory factory = ItemFactory.itemFactory(config);

        DisplayNameItemConfig displayNameConfig = factory.getItemConfig(DisplayNameItemConfig.class);
        if (displayNameConfig != null) {
            displayNameConfig.setEnabled(true);
            displayNameConfig.setDefault(Component.text(baitId).color(NamedTextColor.YELLOW));
        }
        LoreItemConfig loreConfig = factory.getItemConfig(LoreItemConfig.class);
        if (loreConfig != null) {
            loreConfig.setEnabled(true);
            loreConfig.setTransformer(this::createBoostLore);
        }

        factory.setFinalChanges(item -> {
            item.setAmount(config.getInt("drop-quantity", 1));
            BaitNBTManager.applyBaitNBT(item, baitId);
        });

        return factory;
    }

    /**
     * This fetches the boost's lore from the config and inserts the boost-rates into the {boosts} variable. This needs
     * to be called after the bait theme is set and the boosts have been initialized, since it uses those variables.
     *
     * @return A list of formatted Adventure components for the bait's lore
     */
    private @NonNull List<Component> createBoostLore(@Nullable List<Component> original) {
        final EMFListMessage lore = getBaseLoreTemplate();
        lore.setVariableWithListInsertion("{boosts}", createBoostsVariable());
        lore.setVariableWithListInsertion("{lore}", original == null ? EMFListMessage.empty() : original);
        lore.setVariable("{bait_theme}", Component.empty());

        return lore.getComponentListMessage();
    }

    private EMFListMessage getBaseLoreTemplate() {
        return ConfigMessage.BAIT_BAIT_LORE.getMessage().toListMessage();
    }

    private @NonNull EMFMessage createBoostsVariable() {
        EMFListMessage message = EMFListMessage.empty();
        message.appendMessage(appendRarityBoosts());
        message.appendMessage(appendFishBoosts());
        return message;
    }

    private EMFMessage appendRarityBoosts() {
        if (rarities.isEmpty()) return EMFListMessage.empty();

        EMFMessage boost = rarities.size() > 1
                ? ConfigMessage.BAIT_BOOSTS_RARITIES.getMessage()
                : ConfigMessage.BAIT_BOOSTS_RARITY.getMessage();
        boost.setAmount(rarities.size());
        return boost;
    }

    private EMFMessage appendFishBoosts() {
        if (fish.isEmpty()) return EMFListMessage.empty();

        EMFMessage boost = ConfigMessage.BAIT_BOOSTS_FISH.getMessage();
        boost.setAmount(fish.size());
        return boost;
    }

}
