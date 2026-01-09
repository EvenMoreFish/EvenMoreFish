package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.FishUtils;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.CommandUtils;
import com.oheers.fish.commands.arguments.FishArgument;
import com.oheers.fish.commands.arguments.RarityArgument;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Branches:
// [rarity] [fish-name]
// [rarity] [fish-name] [quantity]
// [rarity] [fish-name] [quantity] [targets]
@SuppressWarnings("UnstableApiUsage")
public class FishSubcommand {

    private final String name;

    public FishSubcommand(@NotNull String name) {
        this.name = name;
    }

    // Lots of nesting, but I tried to make it as clean as possible.
    public LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(name)
            .then(
                Commands.argument("rarity", new RarityArgument())
                    .then(
                        Commands.argument("fish", new FishArgument())
                            // [rarity] [fish-name]
                            .executes(ctx -> execute(ctx, false))
                            .then(
                                Commands.argument("amount", IntegerArgumentType.integer(1))
                                    // [rarity] [fish-name] [quantity]
                                    .executes(ctx -> execute(ctx, false))
                                    .then(
                                        Commands.argument("targets", ArgumentTypes.players())
                                            // [rarity] [fish-name] [quantity] [targets]
                                            .executes(ctx -> execute(ctx, true))
                                    )
                            )
                    )
            );
    }

    private int execute(@NotNull CommandContext<CommandSourceStack> ctx, boolean allowConsole) throws CommandSyntaxException {
        CommandSender sender = allowConsole ? ctx.getSource().getSender() : BrigCommandUtils.requirePlayer(ctx);
        Rarity rarity = ctx.getArgument("rarity", Rarity.class);
        String fishStr = ctx.getArgument("fish", String.class);
        int amount = BrigCommandUtils.getArgumentOrDefault(ctx, "amount", int.class, 1);
        PlayerSelectorArgumentResolver targets = BrigCommandUtils.getArgumentOrNull(ctx, "targets", PlayerSelectorArgumentResolver.class);
        return execute(
            sender,
            rarity,
            fishStr,
            amount,
            BrigCommandUtils.resolvePlayers(ctx, targets)
        );
    }

    private int execute(CommandSender sender, Rarity rarity, String fishStr, int amount, List<Player> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) {
            if (!(sender instanceof Player player)) {
                throw BrigCommandUtils.ERROR_NO_PLAYERS.create();
            }
            targets = List.of(player);
        }

        Fish initialFish = FishArgument.resolveFish(rarity, fishStr);
        if (initialFish == null) {
            throw FishArgument.INVALID_FISH.create(fishStr);
        }

        targets.forEach(target -> {
            Fish fish = initialFish.createCopy();
            fish.init();

            if (fish.hasFishRewards()) {
                fish.getFishRewards().forEach(reward -> reward.rewardPlayer(target, target.getLocation()));
            }

            fish.setFisherman(target.getUniqueId());

            final ItemStack fishItem = fish.give();
            fishItem.setAmount(amount);

            FishUtils.giveItem(fishItem, target);
        });

        EMFMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_FISH.getMessage();
        message.setVariable("{player}", CommandUtils.getPlayersVariable(targets));

        message.setFishCaught(initialFish.getName());
        message.send(sender);
        return 1;
    }

}
