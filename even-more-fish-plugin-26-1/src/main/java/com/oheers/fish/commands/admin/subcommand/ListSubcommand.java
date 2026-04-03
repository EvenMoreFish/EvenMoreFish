package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.oheers.fish.commands.CommandUtils;
import com.oheers.fish.commands.arguments.RarityArgument;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.EMFSingleMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class ListSubcommand {

    private final String name;

    public ListSubcommand(@NotNull String name) {
        this.name = name;
    }

    public LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(name)
            .then(rarities())
            .then(fish())
            .then(rewardTypes())
            .then(requirementTypes())
            .then(itemAddons());
    }

    private ArgumentBuilder<CommandSourceStack, ?> rarities() {
        return Commands.literal("rarities")
            .executes(ctx -> {
                showRarities(ctx.getSource().getSender());
                return 1;
            });
    }

    private ArgumentBuilder<CommandSourceStack, ?> fish() {
        return Commands.literal("fish")
            .then(
                Commands.argument("rarity", new RarityArgument())
                    .executes(ctx -> {
                        Rarity rarity = ctx.getArgument("rarity", Rarity.class);
                        showFish(ctx.getSource().getSender(), rarity);
                        return 1;
                    })
            );
    }

    private ArgumentBuilder<CommandSourceStack, ?> rewardTypes() {
        return Commands.literal("rewardTypes")
            .executes(ctx -> {
                CommandUtils.listRewardTypes(ctx.getSource().getSender());
                return 1;
            });
    }

    private ArgumentBuilder<CommandSourceStack, ?> requirementTypes() {
        return Commands.literal("requirementTypes")
            .executes(ctx -> {
                CommandUtils.listRequirementTypes(ctx.getSource().getSender());
                return 1;
            });
    }

    private ArgumentBuilder<CommandSourceStack, ?> itemAddons() {
        return Commands.literal("itemAddons")
            .executes(ctx -> {
                CommandUtils.listItemAddons(ctx.getSource().getSender());
                return 1;
            });
    }

    private void showRarities(@NotNull CommandSender sender) {
        TextComponent.Builder builder = Component.text();
        for (Rarity rarity : FishManager.getInstance().getRarityMap().values()) {
            TextComponent.Builder rarityBuilder = Component.text();
            EMFSingleMessage message = EMFSingleMessage.fromString("<gray>[</gray>{rarity}<gray>]</gray>");
            message.setVariable("{rarity}", rarity.getDisplayName());
            rarityBuilder.append(message.getComponentMessage());
            rarityBuilder.hoverEvent(HoverEvent.hoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                EMFSingleMessage.fromString("Click to view " + rarity.getId() + " fish.").getComponentMessage()
            ));
            rarityBuilder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin list fish " + rarity.getId()));
            builder.append(rarityBuilder);
        }
        sender.sendMessage(builder.build());
    }

    private void showFish(@NotNull CommandSender sender, @NotNull Rarity rarity) {
        TextComponent.Builder builder = Component.text();
        builder.append(rarity.getDisplayName().getComponentMessage());
        builder.append(Component.space());
        for (Fish fish : rarity.getOriginalFishList()) {
            TextComponent.Builder fishBuilder = Component.text();
            EMFSingleMessage message = EMFSingleMessage.fromString("<gray>[</gray>{fish}<gray>]</gray>");
            message.setVariable("{fish}", fish.getDisplayName());
            fishBuilder.append(message.getComponentMessage());
            fishBuilder.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to receive fish")));
            fishBuilder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/emf admin fish " + rarity.getId() + " " + fish.getName().replace(" ", "_")));
            builder.append(fishBuilder);
        }
        sender.sendMessage(builder.build());
    }

}
