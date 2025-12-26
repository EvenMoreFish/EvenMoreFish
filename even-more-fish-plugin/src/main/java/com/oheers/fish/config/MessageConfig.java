package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.messages.EMFConfigLoader;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

public class MessageConfig extends ConfigBase {

    private static MessageConfig instance = null;

    private EMFConfigLoader messageLoader;

    public MessageConfig() {
        super("messages.yml", "locales/" + "messages_" + MainConfig.getInstance().getLocale() + ".yml", EvenMoreFish.getInstance(), true);
        instance = this;
        this.messageLoader = new EMFConfigLoader(getConfig());
    }

    public static MessageConfig getInstance() {
        return instance;
    }

    public EMFConfigLoader getMessageLoader() {
        return messageLoader;
    }

    @Override
    public void reload() {
        super.reload();
        this.messageLoader = new EMFConfigLoader(getConfig());
    }

    public int getLeaderboardCount() {
        return getConfig().getInt("leaderboard-count", 5);
    }

    @Override
    public UpdaterSettings getUpdaterSettings() {
        return UpdaterSettings.builder(super.getUpdaterSettings())
            // Config Version 1 - Rework bossbar config layout
            .addCustomLogic("1", document -> {
                if (document.contains("bossbar.hour-color")) {
                    String hourColor = document.getString("bossbar.hour-color", "<white>");
                    String hour = document.getString("bossbar.hour", "h");
                    document.set("bossbar.hour", hourColor + "{hour}" + hour);
                    document.remove("bossbar.hour-color");
                }

                if (document.contains("bossbar.minute-color")) {
                    String minuteColor = document.getString("bossbar.minute-color", "<white>");
                    String minute = document.getString("bossbar.minute", "m");
                    document.set("bossbar.minute", minuteColor + "{minute}" + minute);
                    document.remove("bossbar.minute-color");
                }

                if (document.contains("bossbar.second-color")) {
                    String secondColor = document.getString("bossbar.second-color", "<white>");
                    String second = document.getString("bossbar.second", "s");
                    document.set("bossbar.second", secondColor + "{second}" + second);
                    document.remove("bossbar.second-color");
                }
            })

            // Config Version 1 - Rework prefix config layout
            .addCustomLogic("1", document -> {
                if (!document.contains("prefix")) {
                    return;
                }
                String prefix = document.getString("prefix");

                String oldRegular = document.getString("prefix-regular");
                document.set("prefix-regular", oldRegular + prefix);

                String oldAdmin = document.getString("prefix-admin");
                document.set("prefix-admin", oldAdmin + prefix);

                String oldError = document.getString("prefix-error");
                document.set("prefix-error", oldError + prefix);

                document.remove("prefix");
            })

            // Config Version 6 - Making the bossbar time format accessible outside the bossbar.
            .addCustomLogic("6", document -> {
                document.move("bossbar.hour", "duration.hour");
                document.move("bossbar.minute", "duration.minute");
                document.move("bossbar.second", "duration.second");
            })

            // Config Version 7 - Moving the toggle messages to the toggle section.
            .addCustomLogic("7", document -> {
                document.move("toggle-on", "toggle.fishing.on");
                document.move("toggle-off", "toggle.fishing.off");
            })

            // Config Version 9 - Rework the time left placeholder.
            .addCustomLogic("9", document -> {
                document.move("emf-time-remaining", "emf-time-remaining.inactive");
                document.remove("emf-time-remaining-during-comp");
            })

            .build();
    }

}
