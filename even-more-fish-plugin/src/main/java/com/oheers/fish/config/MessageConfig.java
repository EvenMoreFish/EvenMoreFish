package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.messages.EMFMessage;
import com.oheers.fish.messages.PrefixType;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

public class MessageConfig extends ConfigBase {

    private static MessageConfig instance = null;

    public MessageConfig() {
        super("messages.yml", "locales/" + "messages_" + MainConfig.getInstance().getLocale() + ".yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }

    public static MessageConfig getInstance() {
        return instance;
    }

    public String getSTDPrefix() {
        EMFMessage message = EMFMessage.empty();
        message.prependMessage(PrefixType.DEFAULT.getPrefix());
        message.appendString("&r");
        return message.getLegacyMessage();
    }

    public int getLeaderboardCount() {
        return getConfig().getInt("leaderboard-count", 5);
    }

    @Override
    public UpdaterSettings getUpdaterSettings() {
        UpdaterSettings.Builder builder = UpdaterSettings.builder(super.getUpdaterSettings());

        // Bossbar config relocations - config version 18
        builder.addCustomLogic("18", yamlDocument -> {
            if (yamlDocument.contains("bossbar.hour-color")) {
                String hourColor = yamlDocument.getString("bossbar.hour-color", "&f");
                String hour = yamlDocument.getString("bossbar.hour", "h");
                yamlDocument.set("bossbar.hour", hourColor + "{hour}" + hour);
                yamlDocument.remove("bossbar.hour-color");
            }

            if (yamlDocument.contains("bossbar.minute-color")) {
                String minuteColor = yamlDocument.getString("bossbar.minute-color", "&f");
                String minute = yamlDocument.getString("bossbar.minute", "m");
                yamlDocument.set("bossbar.minute", minuteColor + "{minute}" + minute);
                yamlDocument.remove("bossbar.minute-color");
            }

            if (yamlDocument.contains("bossbar.second-color")) {
                String secondColor = yamlDocument.getString("bossbar.second-color", "&f");
                String second = yamlDocument.getString("bossbar.second", "s");
                yamlDocument.set("bossbar.second", secondColor + "{second}" + second);
                yamlDocument.remove("bossbar.second-color");
            }
        });

        // Prefix config relocations - config version 19
        builder.addCustomLogic("19", yamlDocument -> {
            if (yamlDocument.contains("prefix")) {
                String prefix = yamlDocument.getString("prefix");

                String oldRegular = yamlDocument.getString("prefix-regular");
                yamlDocument.set("prefix-regular", oldRegular + prefix);

                String oldAdmin = yamlDocument.getString("prefix-admin");
                yamlDocument.set("prefix-admin", oldAdmin + prefix);

                String oldError = yamlDocument.getString("prefix-error");
                yamlDocument.set("prefix-error", oldError + prefix);

                yamlDocument.remove("prefix");
            }
        });

        return builder.build();
    }

}
