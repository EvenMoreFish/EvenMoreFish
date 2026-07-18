package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.DimensionFishingConfig;
import com.oheers.fish.config.GuiFillerConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.config.gui.GuiConfig;
import com.oheers.fish.config.gui.GuiConversions;
import com.oheers.fish.config.gui.impl.ApplyBaitsMenuGuiConfig;
import com.oheers.fish.config.gui.impl.BaitsMenuGuiConfig;
import com.oheers.fish.config.gui.impl.JournalFishGuiConfig;
import com.oheers.fish.config.gui.impl.JournalRaritiesGuiConfig;
import com.oheers.fish.config.gui.impl.MainMenuGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuConfirmGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuNormalGuiConfig;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import uk.firedev.messagelib.MessageLibSettings;
import uk.firedev.messagelib.ObjectProcessor;

import java.util.logging.Level;

public class ConfigurationManager {
    private final EvenMoreFish plugin;

    public ConfigurationManager(EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void loadConfigurations() {
        try {
            prepareMessageLib();

            new MainConfig();
            new MessageConfig();
            new GuiFillerConfig();

            // GUIs. Fetching the instance will initialize the class.
            ApplyBaitsMenuGuiConfig.getInstance();
            BaitsMenuGuiConfig.getInstance();
            JournalFishGuiConfig.getInstance();
            JournalRaritiesGuiConfig.getInstance();
            MainMenuGuiConfig.getInstance();
            SellMenuConfirmGuiConfig.getInstance();
            SellMenuNormalGuiConfig.getInstance();

            // Split guis.yml into the above files.
            new GuiConversions().performCheck();

            if (EvenMoreFish.getInstance().getDimensionFishing() != null) {
                DimensionFishingConfig.getInstance().reload();
            }

            plugin.getLogger().info("Successfully loaded all configurations");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load configurations", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public void reloadConfigurations() {
        try {
            plugin.reloadConfig();
            plugin.saveDefaultConfig();

            MainConfig.getInstance().reload();
            MessageConfig.getInstance().reload();

            ApplyBaitsMenuGuiConfig.getInstance().reload();
            BaitsMenuGuiConfig.getInstance().reload();
            JournalFishGuiConfig.getInstance().reload();
            JournalRaritiesGuiConfig.getInstance().reload();
            MainMenuGuiConfig.getInstance().reload();
            SellMenuConfirmGuiConfig.getInstance().reload();
            SellMenuNormalGuiConfig.getInstance().reload();

            GuiFillerConfig.getInstance().reload();

            plugin.getLogger().info("Successfully reloaded all configurations");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload configurations", e);
        }
    }

    private void prepareMessageLib() {
        MessageLibSettings settings = MessageLibSettings.get();
        settings.setEnableLegacy(true);
        settings.setAllowEmptyAppend(false);
        settings.setAllowEmptyPrepend(false);

        ObjectProcessor.registerProcessor(
            EMFSingleMessage.class,
            EMFSingleMessage::getComponentListMessage
        );
        ObjectProcessor.registerProcessor(
            EMFListMessage.class,
            EMFListMessage::getComponentListMessage
        );
    }

}