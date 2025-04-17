package com.oheers.fish.commands;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.addons.AddonManager;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.requirement.RequirementType;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitManager;
import com.oheers.fish.baits.BaitNBTManager;
import com.oheers.fish.commands.arguments.ArgumentHelper;
import com.oheers.fish.commands.arguments.BaitArgument;
import com.oheers.fish.commands.arguments.CompetitionTypeArgument;
import com.oheers.fish.commands.arguments.FishArgument;
import com.oheers.fish.commands.arguments.RarityArgument;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.permissions.AdminPerms;
import com.oheers.fish.utils.ManifestUtil;
import de.tr7zw.changeme.nbtapi.NBT;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.kyori.adventure.audience.Audience;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;

public class AdminCommand {

    private final HelpMessageBuilder helpMessageBuilder = HelpMessageBuilder.create();

    private final CommandAPICommand command;

    public AdminCommand(@NotNull String name) {
        this.command = new CommandAPICommand(name)
                .withPermission(AdminPerms.ADMIN)
                .executes(info -> {
                    sendHelpMessage(info.sender());
                })
                .withSubcommands(
                        getFish(),
                        getList(),
                        getCompetition(),
                        getNbtRod(),
                        getBait(),
                        getClearBaits(),
                        getReload(),
                        getVersion(),
                        getList(),
                        getMigrate(),
                        getRawItem(),
                        getHelp(),
                        new AdminDatabaseCommand()
                );
    }

    public CommandAPICommand getCommand() {
        return command;
    }

    private void sendHelpMessage(@NotNull CommandSender sender) {
        helpMessageBuilder.sendMessage(sender);
    }

    private CommandAPICommand getFish() {
        helpMessageBuilder.addUsage(
                "admin fish",
                ConfigMessage.HELP_ADMIN_FISH::getMessage
        );
        return new CommandAPICommand("fish")
                .withArguments(
                        RarityArgument.create(),
                        FishArgument.create(),
                        new IntegerArgument("amount", 1).setOptional(true),
                        ArgumentHelper.getPlayerArgument("target").setOptional(true)
                )
                .executes((sender, arguments) -> {
                    final Fish fish = arguments.getUnchecked("fish");
                    if (fish == null) {
                        return;
                    }
                    final int amount = (Integer) arguments.getOptional("amount").orElse(1);
                    final Player target = (Player) arguments.getOptional("target").orElseGet(() -> {
                        if (!(sender instanceof Player player)) {
                            return null;
                        }
                        return player;
                    });

                    if (target == null) {
                        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
                        return;
                    }

                    fish.init();
                    fish.checkFishEvent();
                    if (fish.hasFishRewards()) {
                        fish.getFishRewards().forEach(reward -> reward.rewardPlayer(target, target.getLocation()));
                    }
                    fish.setFisherman(target.getUniqueId());

                    final ItemStack fishItem = fish.give(-1);
                    fishItem.setAmount(amount);

                    FishUtils.giveItem(fishItem, target);

                    EMFMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_FISH.getMessage();
                    message.setPlayer(target);
                    message.setFishCaught(fish.getName());
                    message.send(sender);
                });
    }

    private CommandAPICommand getList() {
        return new CommandAPICommand("list")
                .withArguments(
                        new MultiLiteralArgument(
                                "listTarget",
                                "fish", "rarities", "requirementTypes", "rewardTypes", "itemAddons"
                        ),
                        RarityArgument.create().setOptional(true)
                )
                .executes((sender, args) -> {
                    String listTarget = Objects.requireNonNull(args.getUnchecked("listTarget"));
                    switch (listTarget) {
                        case "fish" -> {
                            final Rarity rarity = args.getUnchecked("rarity");
                            if (rarity == null) {
                                ConfigMessage.RARITY_INVALID.getMessage().send(sender);
                                return;
                            }
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
                        case "rarities" -> {
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
                        case "requirementTypes" -> listRequirementTypes(sender);
                        case "rewardTypes" -> listRewardTypes(sender);
                        case "itemAddons" -> listItemAddons(sender);
                    }
                });
    }

    private CommandAPICommand getNbtRod() {
        helpMessageBuilder.addUsage(
                "admin nbt-rod",
                ConfigMessage.HELP_ADMIN_NBTROD::getMessage
        );
        return new CommandAPICommand("nbt-rod")
                .withArguments(
                        ArgumentHelper.getPlayerArgument("target").setOptional(true)
                )
                .executes(((sender, args) -> {
                    if (!MainConfig.getInstance().requireNBTRod()) {
                        ConfigMessage.ADMIN_NBT_NOT_REQUIRED.getMessage().send(sender);
                        return;
                    }
                    final Player player = (Player) args.getOptional("target").orElseGet(() -> {
                        if (sender instanceof Player p) {
                            return p;
                        }
                        return null;
                    });

                    if (player == null) {
                        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
                        return;
                    }

                    FishUtils.giveItems(Collections.singletonList(EvenMoreFish.getInstance().getCustomNBTRod()), player);
                    EMFMessage giveMessage = ConfigMessage.ADMIN_NBT_ROD_GIVEN.getMessage();
                    giveMessage.setPlayer(player);
                    giveMessage.send(sender);
                }));
    }

    private CommandAPICommand getBait() {
        helpMessageBuilder.addUsage(
                "admin bait",
                ConfigMessage.HELP_ADMIN_BAIT::getMessage
        );
        return new CommandAPICommand("bait")
                .withArguments(
                        BaitArgument.create(),
                        new IntegerArgument("quantity", 1).setOptional(true),
                        ArgumentHelper.getPlayerArgument("target").setOptional(true)
                )
                .executes((sender, args) -> {
                    final Bait bait = Objects.requireNonNull(args.getUnchecked("bait"));
                    final int quantity = (int) args.getOptional("quantity").orElse(1);
                    final Player target = (Player) args.getOptional("target").orElseGet(() -> {
                        if (sender instanceof Player p) {
                            return p;
                        }
                        return null;
                    });

                    if (target == null) {
                        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
                        return;
                    }

                    ItemStack baitItem = bait.create(target);
                    baitItem.setAmount(quantity);
                    FishUtils.giveItems(List.of(baitItem), target);
                    EMFMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_BAIT.getMessage();
                    message.setPlayer(target);
                    message.setBait(bait.getId());
                    message.send(sender);
                });
    }

    private CommandAPICommand getClearBaits() {
        helpMessageBuilder.addUsage(
                "admin clearbaits",
                ConfigMessage.HELP_ADMIN_CLEARBAITS::getMessage
        );
        return new CommandAPICommand("clearbaits")
                .withArguments(
                        ArgumentHelper.getPlayerArgument("target").setOptional(true)
                )
                .executes(((sender, args) -> {
                    final Player player = (Player) args.getOptional("target").orElseGet(() -> {
                        if (sender instanceof Player p) {
                            return p;
                        }
                        return null;
                    });

                    if (player == null) {
                        ConfigMessage.ADMIN_CANT_BE_CONSOLE.getMessage().send(sender);
                        return;
                    }

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
                }));
    }

    private CommandAPICommand getReload() {
        helpMessageBuilder.addUsage(
                "admin reload",
                ConfigMessage.HELP_ADMIN_RELOAD::getMessage
        );
        return new CommandAPICommand("reload")
                .executes(info -> {
                    EvenMoreFish.getInstance().reload(info.sender());
                });
    }

    @SuppressWarnings("UnstableApiUsage")
    private CommandAPICommand getVersion() {
        helpMessageBuilder.addUsage(
            "admin version",
            ConfigMessage.HELP_ADMIN_VERSION::getMessage
        );
        return new CommandAPICommand("version")
            .executes(info -> {
                int fishCount = FishManager.getInstance().getRarityMap().values().stream()
                    .mapToInt(rarity -> rarity.getFishList().size())
                    .sum();

                String databaseEngine = "N/A";
                String databaseType = "N/A";
                final Database database = EvenMoreFish.getInstance().getDatabase();
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

                message.setVariable("{prefix}", MessageConfig.getInstance().getSTDPrefix());
                message.setVariable("{version}", EvenMoreFish.getInstance().getPluginMeta().getVersion());
                message.setVariable("{branch}", getFeatureBranchName());
                message.setVariable("{build-date}", getFeatureBranchBuildOrDate());
                message.setVariable("{mcv}", Bukkit.getServer().getVersion());
                message.setVariable("{ssv}", Bukkit.getServer().getBukkitVersion());
                message.setVariable("{online}", String.valueOf(Bukkit.getServer().getOnlineMode()));
                message.setVariable("{rarities}", String.valueOf(FishManager.getInstance().getRarityMap().size()));
                message.setVariable("{fish}", String.valueOf(fishCount));
                message.setVariable("{baits}", String.valueOf(BaitManager.getInstance().getBaitMap().size()));
                message.setVariable("{competitions}", String.valueOf(EvenMoreFish.getInstance().getCompetitionQueue().getSize()));
                message.setVariable("{engine}", databaseEngine);
                message.setVariable("{type}", databaseType);

                message.send(info.sender());
            });
    }

    private String getFeatureBranchName() {
        return ManifestUtil.getAttributeFromManifest(Attributes.Name.IMPLEMENTATION_TITLE.toString(), "main");
    }

    private String getFeatureBranchBuildOrDate() {
        return ManifestUtil.getAttributeFromManifest(Attributes.Name.IMPLEMENTATION_VERSION.toString(), "");
    }

    // TODO fix stray comma in all of these messages
    private void listRewardTypes(@NotNull Audience audience) {
        TextComponent.Builder builder = Component.text();

        EMFMessage listMessage = ConfigMessage.ADMIN_LIST_ADDONS.getMessage();
        listMessage.setVariable("{addon-type}", RewardType.class.getSimpleName());
        builder.append(listMessage.getComponentMessage());

        RewardType.getLoadedTypes().forEach((string, rewardType) -> {
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

    private void listRequirementTypes(@NotNull Audience audience) {
        TextComponent.Builder builder = Component.text();

        EMFMessage listMessage = ConfigMessage.ADMIN_LIST_ADDONS.getMessage();
        listMessage.setVariable("{addon-type}", RequirementType.class.getSimpleName());
        builder.append(listMessage.getComponentMessage());

        RequirementType.getLoadedTypes().forEach((string, requirementType) -> {
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

    private void listItemAddons(@NotNull Audience audience) {
        TextComponent.Builder builder = Component.text();

        EMFMessage listMessage = ConfigMessage.ADMIN_LIST_ADDONS.getMessage();
        listMessage.setVariable("{addon-type}", ItemAddon.class.getSimpleName());
        builder.append(listMessage.getComponentMessage());

        ItemAddon.getLoadedAddons().forEach((string, itemAddon) -> {
            Component show = EMFSingleMessage.fromString(
                "Author: " + itemAddon.getAuthor()
            ).getComponentMessage();

            TextComponent.Builder typeBuilder = Component.text().content(itemAddon.getIdentifier());
            typeBuilder.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, show));
            builder.append(typeBuilder).append(Component.text(", "));
        });
        audience.sendMessage(builder.build());
    }

    private CommandAPICommand getMigrate() {
        helpMessageBuilder.addUsage(
                "admin migrate",
                ConfigMessage.HELP_ADMIN_MIGRATE::getMessage
        );
        return new CommandAPICommand("migrate")
                .executes(info -> {
                    if (!MainConfig.getInstance().databaseEnabled()) {
                        EMFSingleMessage.fromString("You cannot run migrations when the database is disabled. Please set database.enabled: true. And restart the server.")
                            .send(info.sender());
                        return;
                    }
                    EvenMoreFish.getScheduler().runTaskAsynchronously(() -> EvenMoreFish.getInstance().getDatabase().getMigrationManager().migrateLegacy(info.sender()));
                });
    }

    private CommandAPICommand getRawItem() {
        helpMessageBuilder.addUsage(
                "admin rawItem",
                ConfigMessage.HELP_ADMIN_RAWITEM::getMessage
        );
        return new CommandAPICommand("rawItem")
                .executesPlayer(info -> {
                    ItemStack handItem = info.sender().getInventory().getItemInMainHand();
                    String handItemNbt = NBT.itemStackToNBT(handItem).toString();

                    // Ensure the handItemNbt is escaped for use in YAML
                    // This could be slightly inefficient, but it is the only way I can currently think of.
                    YamlDocument document = new ConfigBase().getConfig();
                    document.set("rawItem", handItemNbt);
                    handItemNbt = document.dump().replaceFirst("rawItem: ", "");

                    TextComponent.Builder builder = Component.text().content(handItemNbt);
                    builder.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to copy to clipboard.")));
                    builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, handItemNbt));
                    info.sender().sendMessage(builder.build());
                });

    }

    private CommandAPICommand getHelp() {
        helpMessageBuilder.addUsage(
                "admin help",
                ConfigMessage.HELP_GENERAL_HELP::getMessage
        );
        return new CommandAPICommand("help")
                .executes(info -> {
                    sendHelpMessage(info.sender());
                });
    }

    // COMPETITION BRANCH

    private CommandAPICommand getCompetition() {
        helpMessageBuilder.addUsage(
                "admin competition",
                ConfigMessage.HELP_ADMIN_COMPETITION::getMessage
        );
        return new CommandAPICommand("competition")
                .withSubcommands(
                        getCompetitionStart(),
                        getCompetitionEnd(),
                        getCompetitionTest()
                );
    }

    private CommandAPICommand getCompetitionStart() {
        return new CommandAPICommand("start")
                .withArguments(
                        // StringArgument containing all loaded competition ids
                        ArgumentHelper.getAsyncStringsArgument(
                                "competitionId",
                                info -> EvenMoreFish.getInstance().getCompetitionQueue().getFileMap().keySet().toArray(String[]::new)
                        ),
                        new IntegerArgument("duration", 1).setOptional(true)
                )
                .executes((sender, arguments) -> {
                    final String id = Objects.requireNonNull(arguments.getUnchecked("competitionId"));
                    final Integer duration = arguments.getUnchecked("duration");
                    if (Competition.isActive()) {
                        ConfigMessage.COMPETITION_ALREADY_RUNNING.getMessage().send(sender);
                        return;
                    }
                    CompetitionFile file = EvenMoreFish.getInstance().getCompetitionQueue().getFileMap().get(id);
                    if (file == null) {
                        ConfigMessage.INVALID_COMPETITION_ID.getMessage().send(sender);
                        return;
                    }
                    Competition competition = new Competition(file);
                    competition.setAdminStarted(true);
                    if (duration != null) {
                        competition.setMaxDuration(duration);
                    }
                    competition.begin();
                });
    }

    private CommandAPICommand getCompetitionEnd() {
        return new CommandAPICommand("end")
                .executes(info -> {
                    Competition active = Competition.getCurrentlyActive();
                    if (active != null) {
                        active.end(false);
                        return;
                    }
                    ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(info.sender());
                });
    }

    private CommandAPICommand getCompetitionTest() {
        return new CommandAPICommand("test")
                .withArguments(
                        new IntegerArgument("duration", 1).setOptional(true),
                        CompetitionTypeArgument.create().setOptional(true)
                )
                .executes((sender, args) -> {
                    if (Competition.isActive()) {
                        ConfigMessage.COMPETITION_ALREADY_RUNNING.getMessage().send(sender);
                        return;
                    }
                    final int duration = (int) args.getOptional("duration").orElse(1);
                    final CompetitionType type = (CompetitionType) args.getOptional("competitionType").orElse(CompetitionType.LARGEST_FISH);
                    CompetitionFile file = new CompetitionFile("adminTest", type, duration);
                    Competition competition = new Competition(file);
                    competition.setAdminStarted(true);
                    competition.begin();
                });
    }

}
