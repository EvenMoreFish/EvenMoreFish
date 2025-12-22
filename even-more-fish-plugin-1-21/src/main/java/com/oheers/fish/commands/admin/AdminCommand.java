package com.oheers.fish.commands.admin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.utils.ManifestUtil;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.commands.BrigCommandUtils;
import com.oheers.fish.commands.CommandUtils;
import com.oheers.fish.commands.HelpMessageBuilder;
import com.oheers.fish.commands.admin.subcommand.CompetitionSubcommand;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.DefaultExecutes;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Permission;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.annotations.arguments.CustomArg;
import net.strokkur.commands.annotations.arguments.IntArg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.jar.Attributes;

@SuppressWarnings("UnstableApiUsage")
@NullMarked
@Command("admin")
@Permission(AdminPerms.ADMIN)
public class AdminCommand {

    private static final HelpMessageBuilder HELP_MESSAGE = HelpMessageBuilder.create()
        .addUsage("admin fish", ConfigMessage.HELP_ADMIN_FISH::getMessage)
        .addUsage("admin custom-rod", ConfigMessage.HELP_ADMIN_CUSTOMROD::getMessage)
        .addUsage("admin bait", ConfigMessage.HELP_ADMIN_BAIT::getMessage)
        .addUsage("admin clearbaits", ConfigMessage.HELP_ADMIN_CLEARBAITS::getMessage)
        .addUsage("admin reload", ConfigMessage.HELP_ADMIN_RELOAD::getMessage)
        .addUsage("admin version", ConfigMessage.HELP_ADMIN_VERSION::getMessage)
        .addUsage("admin migrate", ConfigMessage.HELP_ADMIN_MIGRATE::getMessage)
        .addUsage("admin rawItem", ConfigMessage.HELP_ADMIN_RAWITEM::getMessage)
        .addUsage("admin help", ConfigMessage.HELP_GENERAL_HELP::getMessage)
        .addUsage("admin competition", ConfigMessage.HELP_ADMIN_COMPETITION::getMessage);

    @Subcommand("database")
    @Permission("emf.admin.debug.database")
    DatabaseSubcommand databaseSubcommand;

    @Subcommand("fish")
    FishSubcommand fishSubcommand;

    @Subcommand("list")
    ListSubcommand listSubcommand;

    @Subcommand("competition")
    CompetitionSubcommand competitionSubcommand;

    @DefaultExecutes
    public void onDefault(CommandSender sender) {
        sendHelpMessage(sender);
    }

    @Executes("custom-rod")
    public void onCustomRod(CommandSender sender, @CustomArg(CustomRodArgument.class) CustomRod rod, List<Player> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) {
            throw BrigCommandUtils.ERROR_NO_PLAYERS.create();
        }

        ItemStack rodItem = rod.create();

        for (Player player : targets) {
            FishUtils.giveItems(List.of(rodItem), player);
        }

        EMFMessage giveMessage = ConfigMessage.ADMIN_CUSTOM_ROD_GIVEN.getMessage();
        giveMessage.setVariable("{player}", CommandUtils.getPlayersVariable(targets));

        giveMessage.send(sender);
    }

    @Executes("custom-rod")
    public void onCustomRod(CommandSender sender, @CustomArg(CustomRodArgument.class) CustomRod rod) throws CommandSyntaxException {
        if (sender instanceof Player player) {
            onCustomRod(sender, rod, List.of(player));
            return;
        }
        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
    }

    @Executes("bait")
    public void onBait(CommandSender sender, @CustomArg(BaitArgument.class) BaitHandler bait, @IntArg(min = 1) int quantity, List<Player> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) {
            throw BrigCommandUtils.ERROR_NO_PLAYERS.create();
        }
        for (Player target : targets) {
            ItemStack baitItem = bait.create(target);
            baitItem.setAmount(quantity);
            FishUtils.giveItems(List.of(baitItem), target);
        }
        EMFMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_BAIT.getMessage();
        message.setVariable("{player}", CommandUtils.getPlayersVariable(targets));
        message.setBait(bait.getId());
        message.send(sender);
    }

    @Executes("bait")
    public void onBait(CommandSender sender, @CustomArg(BaitArgument.class) BaitHandler bait, @IntArg(min = 1) int quantity) throws CommandSyntaxException {
        if (sender instanceof Player player) {
            onBait(sender, bait, quantity, List.of(player));
            return;
        }
        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
    }

    @Executes("bait")
    public void onBait(CommandSender sender, @CustomArg(BaitArgument.class) BaitHandler bait) throws CommandSyntaxException {
        onBait(sender, bait, 1);
    }

    @Executes("clearbaits")
    public void onClearBaits(CommandSender sender, List<Player> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) {
            throw BrigCommandUtils.ERROR_NO_PLAYERS.create();
        }
        targets.forEach(player -> {
            if (player.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD) {
                ConfigMessage.ADMIN_NOT_HOLDING_ROD.getMessage().send(player);
                return;
            }

            ItemStack fishingRod = player.getInventory().getItemInMainHand();
            if (!BaitNBTManager.isBaitedRod(fishingRod)) {
                ConfigMessage.NO_BAITS.getMessage().send(player);
                return;
            }

            int totalDeleted = BaitNBTManager.deleteAllBaits(fishingRod);
            if (totalDeleted > 0) {
                fishingRod.editMeta(meta -> meta.lore(BaitNBTManager.deleteOldLore(fishingRod)));
            }

            EMFMessage message = ConfigMessage.BAITS_CLEARED.getMessage();
            message.setAmount(Integer.toString(totalDeleted));
            message.send(player);
        });
    }

    @Executes("clearbaits")
    public void onClearBaits(CommandSender sender) throws CommandSyntaxException {
        if (sender instanceof Player player) {
            onClearBaits(sender, List.of(player));
            return;
        }
        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
    }

    @Executes("reload")
    public void onReload(CommandSender sender) {
        EvenMoreFish.getInstance().reload(sender);
    }

    @Executes("version")
    public void onVersion(CommandSender sender) {
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

        message.send(sender);
    }

    @Executes("rawItem")
    public void onRawItem(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
            return;
        }
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.isEmpty()) {
            return;
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
    }

    private String getFeatureBranchName() {
        return ManifestUtil.getAttributeFromManifest(Attributes.Name.IMPLEMENTATION_TITLE.toString(), "main");
    }

    private String getFeatureBranchBuildOrDate() {
        return ManifestUtil.getAttributeFromManifest(Attributes.Name.IMPLEMENTATION_VERSION.toString(), "");
    }

    @Executes("migrate")
    public void onMigrate(CommandSender sender) {
        if (!DatabaseUtil.isDatabaseOnline()) {
            sender.sendPlainMessage("You cannot run migrations when the database is disabled. Please set database.enabled: true. And restart the server.");
            return;
        }
        EvenMoreFish.getScheduler().runTaskAsynchronously(() -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().migrateLegacy(sender));
    }

    @Executes("help")
    public void onHelp(CommandSender sender) {
        sendHelpMessage(sender);
    }

    public static void sendHelpMessage(CommandSender sender) {
        HELP_MESSAGE.sendMessage(sender);
    }

}