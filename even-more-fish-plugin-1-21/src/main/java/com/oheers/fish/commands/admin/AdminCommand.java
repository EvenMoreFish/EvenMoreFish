package com.oheers.fish.commands.admin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.utils.ManifestUtil;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.commands.AdminCommandProvider;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.CommandUtils;
import com.oheers.fish.commands.admin.subcommand.BaitSubcommand;
import com.oheers.fish.commands.admin.subcommand.ClearBaitsSubcommand;
import com.oheers.fish.commands.admin.subcommand.CompetitionSubcommand;
import com.oheers.fish.commands.admin.subcommand.CustomRodSubcommand;
import com.oheers.fish.commands.admin.subcommand.DatabaseSubcommand;
import com.oheers.fish.commands.admin.subcommand.FishSubcommand;
import com.oheers.fish.commands.admin.subcommand.ListSubcommand;
import com.oheers.fish.commands.arguments.BaitArgument;
import com.oheers.fish.commands.arguments.CustomRodArgument;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.PrefixType;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.permissions.AdminPerms;
import de.tr7zw.changeme.nbtapi.NBT;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.jar.Attributes;

@SuppressWarnings("UnstableApiUsage")
public class AdminCommand extends AdminCommandProvider<CommandNode<CommandSourceStack>, ArgumentBuilder<CommandSourceStack, ?>> {

    private final String name;

    public AdminCommand(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NonNull LiteralCommandNode<CommandSourceStack> get() {
        return getAsArgument().build();
    }

    @Override
    public @NonNull LiteralArgumentBuilder<CommandSourceStack> getAsArgument() {
        return Commands.literal(name)
            .requires(source -> source.getSender().hasPermission(AdminPerms.ADMIN))
            .then(database())
            .then(fish())
            .then(list())
            .then(competition())
            .then(customRod())
            .then(bait())
            .then(clearBaits())
            .then(reload())
            .then(version())
            .then(rawItem())
            .then(migrate())
            .then(help());
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> database() {
        return new DatabaseSubcommand("database").get();
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> fish() {
        return new FishSubcommand("fish").get();
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> list() {
        return new ListSubcommand("list").get();
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> competition() {
        return new CompetitionSubcommand("competition").get();
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> customRod() {
        return new CustomRodSubcommand("custom-rod").get();
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> bait() {
        return new BaitSubcommand("bait").get();
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> clearBaits() {
        return new ClearBaitsSubcommand("clearbaits").get();
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> reload() {
        return Commands.literal("reload")
            .executes(ctx -> {
                EvenMoreFish.getInstance().reload(ctx.getSource().getSender());
                return 1;
            });
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> version() {
        return Commands.literal("version")
            .executes(ctx -> {
                getVersionMessage().send(ctx.getSource().getSender());
                return 1;
            });
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> rawItem() {
        return Commands.literal("rawItem")
            .executes(ctx -> {
                Player player = BrigCommandUtils.requirePlayer(ctx);
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem.isEmpty()) {
                    return 1;
                }

                String handItemNbt = NBT.itemStackToNBT(handItem).toString();

                // Ensure the handItemNbt is escaped for use in YAML
                // This could be slightly inefficient, but it is the only way I can currently think of.
                YamlDocument document = new ConfigBase().getConfig();
                document.set("rawItem", handItemNbt);
                handItemNbt = document.dump().replaceFirst("rawItem: ", "");

                TextComponent.Builder builder = Component.text().content(handItemNbt);
                builder.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to copy to clipboard.")));
                builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, handItemNbt));
                player.sendMessage(builder.build());
                return 1;
            });
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> migrate() {
        return Commands.literal("migrate")
            .executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                if (!DatabaseUtil.isDatabaseOnline()) {
                    sender.sendPlainMessage("You cannot run migrations when the database is disabled. Please set database.enabled: true. And restart the server.");
                    return 1;
                }
                EvenMoreFish.getScheduler().runTaskAsynchronously(
                    () -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().migrateLegacy(sender)
                );
                return 1;
            });
    }

    @Override
    protected @NonNull ArgumentBuilder<CommandSourceStack, ?> help() {
        return Commands.literal("help")
            .executes(ctx -> {
                sendHelpMessage(ctx.getSource().getSender());
                return 1;
            });
    }

    private EMFSingleMessage getVersionMessage() {
        int fishCount = FishManager.getInstance().getRarityMap().values().stream()
            .mapToInt(rarity -> rarity.getFishList().size())
            .sum();

        String databaseEngine = "N/A";
        String databaseType = "N/A";
        final Database database = EvenMoreFish.getInstance().getPluginDataManager().getDatabase();
        if (database != null) {
            databaseEngine = database.getDatabaseVersion();
            databaseType = database.getType();
        }

        final String msgString =
            """
                {prefix} EvenMoreFish by Oheers {version}\s
                {prefix} Feature Branch: {branch}\s
                {prefix} Feature Build/Date: {build-date}\s
                {prefix} MCV: {mcv}\s
                {prefix} SSV: {ssv}\s
                {prefix} Online: {online}\s
                {prefix} Loaded Rarities({rarities}) Fish({fish}) Baits({baits}) Competitions({competitions})\s
                {prefix} Database Engine: {engine}\s
                {prefix} Database Type: {type}\s
                """;

        EMFSingleMessage message = EMFSingleMessage.fromString(msgString);

        message.setVariable("{prefix}", PrefixType.DEFAULT.getPrefix());
        message.setVariable("{version}", EvenMoreFish.getInstance().getPluginMeta().getVersion());
        message.setVariable("{branch}", getFeatureBranchName());
        message.setVariable("{build-date}", getFeatureBranchBuildOrDate());
        message.setVariable("{mcv}", Bukkit.getServer().getVersion());
        message.setVariable("{ssv}", Bukkit.getServer().getBukkitVersion());
        message.setVariable("{online}", String.valueOf(Bukkit.getServer().getOnlineMode()));
        message.setVariable("{rarities}", String.valueOf(FishManager.getInstance().getRarityMap().size()));
        message.setVariable("{fish}", String.valueOf(fishCount));
        message.setVariable("{baits}", String.valueOf(BaitManager.getInstance().getItemMap().size()));
        message.setVariable("{competitions}", String.valueOf(EvenMoreFish.getInstance().getCompetitionQueue().getSize()));
        message.setVariable("{engine}", databaseEngine);
        message.setVariable("{type}", databaseType);

        return message;
    }

    private String getFeatureBranchName() {
        return ManifestUtil.getAttributeFromManifest(Attributes.Name.IMPLEMENTATION_TITLE.toString(), "main");
    }

    private String getFeatureBranchBuildOrDate() {
        return ManifestUtil.getAttributeFromManifest(Attributes.Name.IMPLEMENTATION_VERSION.toString(), "");
    }

}