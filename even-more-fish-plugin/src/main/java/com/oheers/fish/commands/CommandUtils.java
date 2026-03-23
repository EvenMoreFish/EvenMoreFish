package com.oheers.fish.commands;

import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.requirement.RequirementType;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class CommandUtils {

    private CommandUtils() throws IllegalAccessException{
        throw new IllegalAccessException("Cannot create instance of static class.");
    }

    public static boolean isEconomyDisabled(CommandSender sender) {
        if (!Economy.getInstance().isEnabled()) {
            ConfigMessage.ECONOMY_DISABLED.getMessage().send(sender);
            return true;
        }
        return false;
    }

    public static boolean isEconomyEnabled(CommandSender sender) {
        return !isEconomyDisabled(sender);
    }

    public static boolean isLogDbError(final CommandSender sender) {
        if (!DatabaseUtil.isDatabaseOnline()) {
            sender.sendMessage("Database is offline.");
            return true;
        }
        return false;
    }

    public static String getPlayersVariable(List<Player> players) {
        int size = players.size();
        if (size == 0) {
            return "No Players.";
        } else if (size == 1) {
            return players.getFirst().getName();
        } else if (size == Bukkit.getOnlinePlayers().size()) {
            return "All Players";
        } else {
            return players.stream().map(Player::getName).collect(Collectors.joining(", "));
        }
    }

    public static void listRewardTypes(@NotNull Audience audience) {
        TextComponent.Builder builder = Component.text();

        EMFMessage listMessage = ConfigMessage.ADMIN_LIST_ADDONS.getMessage();
        listMessage.setVariable("{addon-type}", RewardType.class.getSimpleName());
        builder.append(listMessage.getComponentMessage());

        EMFRegistry.REWARD_TYPE.getRegistry().forEach((string, rewardType) -> {
            Component show = EMFSingleMessage.fromString(
                "Author: " + rewardType.getAuthor() + "\n" +
                    "Registered Plugin: " + rewardType.getPlugin().getName()
            ).getComponentMessage();

            TextComponent.Builder typeBuilder = Component.text().content(rewardType.getIdentifier());
            typeBuilder.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, show));
            builder.append(typeBuilder).append(Component.text(", "));
        });
        audience.sendMessage(builder.build());
    }

    public static void listRequirementTypes(@NotNull Audience audience) {
        TextComponent.Builder builder = Component.text();

        EMFMessage listMessage = ConfigMessage.ADMIN_LIST_ADDONS.getMessage();
        listMessage.setVariable("{addon-type}", RequirementType.class.getSimpleName());
        builder.append(listMessage.getComponentMessage());

        EMFRegistry.REQUIREMENT_TYPE.getRegistry().forEach((string, requirementType) -> {
            Component show = EMFSingleMessage.fromString(
                "Author: " + requirementType.getAuthor() + "\n" +
                    "Registered Plugin: " + requirementType.getPlugin().getName()
            ).getComponentMessage();

            TextComponent.Builder typeBuilder = Component.text().content(requirementType.getIdentifier());
            typeBuilder.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, show));
            builder.append(typeBuilder).append(Component.text(", "));
        });
        audience.sendMessage(builder.build());
    }

    public static void listItemAddons(@NotNull Audience audience) {
        TextComponent.Builder builder = Component.text();

        EMFMessage listMessage = ConfigMessage.ADMIN_LIST_ADDONS.getMessage();
        listMessage.setVariable("{addon-type}", ItemAddon.class.getSimpleName());
        builder.append(listMessage.getComponentMessage());

        EMFRegistry.ITEM_ADDON.getRegistry().forEach((string, itemAddon) -> {
            Component show = EMFSingleMessage.fromString(
                "Author: " + itemAddon.getAuthor()
            ).getComponentMessage();

            TextComponent.Builder typeBuilder = Component.text().content(itemAddon.getIdentifier());
            typeBuilder.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, show));
            builder.append(typeBuilder).append(Component.text(", "));
        });
        audience.sendMessage(builder.build());
    }

}
