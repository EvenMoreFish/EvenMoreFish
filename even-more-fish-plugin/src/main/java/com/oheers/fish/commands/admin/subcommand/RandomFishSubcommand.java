package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.PrefixType;
import com.oheers.fish.messages.abstracted.EMFMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.oheers.fish.commands.arguments.RarityArgument;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.command.CommandUtils;

// Branches:
// [rarity]
// [rarity] [quantity]
// [rarity] [quantity] [targets]
@SuppressWarnings("UnstableApiUsage")
public class RandomFishSubcommand {

    private final String name;

    public RandomFishSubcommand(@NonNull String name) {
        this.name = name;
    }

    public LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(name)
            .then(
                // [rarity]
                Commands.argument("rarity", new RarityArgument())
                    .executes(ctx -> execute(ctx, false))
                    .then(
                        Commands.argument("amount", IntegerArgumentType.integer(1))
                            // [rarity] [quantity]
                            .executes(ctx -> execute(ctx, false))
                            .then(
                                Commands.argument("target", ArgumentTypes.player())
                                    // [rarity] [quantity] [target]
                                    .executes(ctx -> execute(ctx, true))
                            )
                    )
            );
    }

    private int execute(@NonNull CommandContext<CommandSourceStack> ctx, boolean allowConsole) throws CommandSyntaxException {
        CommandSender sender = allowConsole ? ctx.getSource().getSender() : CommandUtils.requirePlayer(ctx);
        IRarity rarity = ctx.getArgument("rarity", IRarity.class);
        int amount = CommandUtils.getArgumentOrDefault(ctx, "amount", int.class, 1);
        Player target;
        try {
            target = CommandUtils.parsePlayerArgument(
                ctx.getSource(),
                ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
            );
        } catch (CommandSyntaxException exception) {
            target = null;
        }
        return execute(
            sender,
            rarity,
            amount,
            target
        );
    }

    private int execute(CommandSender sender, IRarity rarity, int amount, Player target) throws CommandSyntaxException {
        if (target == null) {
            if (!(sender instanceof Player player)) {
                throw CommandUtils.INVALID_TARGET.create();
            }
            target = player;
        }

        RequirementContext context = new RequirementContext(
            target.getWorld(),
            target.getLocation(),
            target,
            null,
            null,
            null
        );

        IFish fish = FishManager.getInstance().getFish(
            rarity,
            target.getLocation(),
            target,
            1,
            null,
            true,
            null,
            null,
            context
        );
        if (fish == null) {
            EMFSingleMessage message = PrefixType.ADMIN.getPrefix();
            message.appendString("<white>Failed to select fish. See console for information.");
            message.send(sender);
            return 1;
        }

        fish.init();

        for (Reward reward : fish.getCatchRewards()) {
            reward.give(target, target.getLocation());
        }

        fish.setFisherman(target);

        final ItemStack fishItem = fish.give();
        fishItem.setAmount(amount);

        FishUtils.giveItem(fishItem, target);

        EMFMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_FISH.getMessage();
        message.setPlayer(target);
        message.setFishCaught(fish.getId());
        message.send(sender);
        return 1;
    }

}
