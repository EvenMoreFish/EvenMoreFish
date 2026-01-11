package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ClearBaitsSubcommand {

    private final String name;

    public ClearBaitsSubcommand(@NotNull String name) {
        this.name = name;
    }

    public LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(name)
            // No args
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                clear(player, player);
                return 1;
            })
            .then(
                Commands.argument("targets", ArgumentTypes.players())
                    // [targets]
                    .executes(ctx -> {
                        PlayerSelectorArgumentResolver resolver = ctx.getArgument("targets", PlayerSelectorArgumentResolver.class);
                        List<Player> targets = BrigCommandUtils.resolvePlayers(ctx, resolver);
                        if (targets.isEmpty()) {
                            throw BrigCommandUtils.ERROR_NO_PLAYERS.create();
                        }
                        CommandSender sender = ctx.getSource().getSender();
                        targets.forEach(target -> clear(sender, target));
                        return 1;
                    })
            );
    }

    private void clear(@NotNull CommandSender sender, @NotNull Player target) {
        if (target.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD) {
            ConfigMessage.ADMIN_NOT_HOLDING_ROD.getMessage().send(sender);
            return;
        }

        ItemStack fishingRod = target.getInventory().getItemInMainHand();
        if (!BaitNBTManager.isBaitedRod(fishingRod)) {
            ConfigMessage.NO_BAITS.getMessage().send(sender);
            return;
        }

        int totalDeleted = BaitNBTManager.deleteAllBaits(fishingRod);
        if (totalDeleted > 0) {
            fishingRod.editMeta(meta -> meta.lore(BaitNBTManager.deleteOldLore(fishingRod)));
        }

        EMFMessage message = ConfigMessage.BAITS_CLEARED.getMessage();
        message.setAmount(Integer.toString(totalDeleted));
        message.send(target);
        if (!target.equals(sender)) {
            message.send(sender);
        }
    }

}
