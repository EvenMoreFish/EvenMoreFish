package com.oheers.fish.messages;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.messages.abstracted.EMFMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ConfigMessage {

    ADMIN_CANT_BE_CONSOLE("<white>Command cannot be run from console.", PrefixType.ERROR, false, true, "admin.cannot-run-on-console"),
    ADMIN_GIVE_PLAYER_BAIT("<white>You have given {player} a {bait}.", PrefixType.ADMIN, true, true, "admin.given-player-bait"),
    ADMIN_GIVE_PLAYER_FISH("<white>You have given {player} a {fish}.", PrefixType.ADMIN, true, true, "admin.given-player-fish"),
    ADMIN_OPEN_FISH_SHOP("<white>Opened a shop inventory for {player}.", PrefixType.ADMIN, true, true, "admin.open-fish-shop"),
    ADMIN_CUSTOM_ROD_GIVEN(
            "<white>You have given {player} a custom fishing rod.",
            PrefixType.ADMIN,
            true,
            true,
            "admin.custom-rod-given"
    ),
    ADMIN_NBT_NOT_REQUIRED("<white>Change \"require-nbt-rod\" to true in order to use this feature.", PrefixType.ERROR, false, true, "admin.nbt-not-required"),
    ADMIN_NO_BAIT_SPECIFIED("<white>You must specify a bait name.", PrefixType.ERROR, false, true, "admin.no-bait-specified"),
    ADMIN_NOT_HOLDING_ROD("<white>You need to be holding a fishing rod to run that command.", PrefixType.ERROR, false, true, "admin.must-be-holding-rod"),
    ADMIN_NUMBER_FORMAT_ERROR("<white>{amount} is not a valid number.", PrefixType.ERROR, false, true, "admin.number-format-error"),
    ADMIN_NUMBER_RANGE_ERROR("<white>{amount} is not a number between 1-64.", PrefixType.ERROR, false, true, "admin.number-range-error"),
    ADMIN_UNKNOWN_PLAYER("<white>{player} could not be found.", PrefixType.ERROR, false, true, "admin.player-not-found"),
    ADMIN_UPDATE_AVAILABLE("<white>There is an update available: " + "https://modrinth.com/plugin/evenmorefish/versions?l=paper", PrefixType.ADMIN, false, false, "admin.update-available"),
    ADMIN_LIST_ADDONS("<white>Registered {addon-type}s: ", PrefixType.ADMIN, false, false, "admin.list-addons"),
    ADMIN_LIST_REWARD_TYPES("<white>Registered Reward Types: ", PrefixType.ADMIN, false, false, "admin.list-reward-types"),

    BAITS_CLEARED("<white>You have removed all {amount} baits from your fishing rod.", PrefixType.ADMIN, true, true, "admin.all-baits-cleared"),
    BAIT_CAUGHT("<white><b>{player}</b> <white>has caught a <b>{bait}</b> <white>bait!", PrefixType.NONE, true, true, "bait-catch"),
    BAIT_USED("<white>You have used one of your rod's <b>{bait}</b> <white>bait.", PrefixType.DEFAULT, true, true, "bait-use"),
    BAIT_WRONG_GAMEMODE("<white>You must be in <u>survival or adventure mode</u> to apply baits to fishing rods.", PrefixType.ERROR, false, true, "bait-survival-limited"),
    BAITS_MAXED("<white>You have reached the maximum number of types of baits for this fishing rod.", PrefixType.DEFAULT, false, true, "max-baits-reached"),
    BAITS_MAXED_ON_ROD("<white>You have reached the maximum number of {bait} bait that can be applied to one rod.", PrefixType.ERROR, false, true, "max-baits-reached"),
    BAIT_ROD_PROTECTION("<white>Protected your baited fishing rod. If you are trying to repair it, please put it in the first slot instead.", PrefixType.ERROR, false, true, "bait-rod-protection"),
    BAIT_INVALID_ROD("<white>You cannot apply bait to this fishing rod!", PrefixType.ERROR, true, false, "bait-invalid-rod"),

    BAR_LAYOUT("{prefix}{time-formatted} {remaining}", PrefixType.NONE, true, false, "bossbar.layout"),
    BAR_SECOND("<white>{second}s", PrefixType.NONE, true, false, "bossbar.second"),
    BAR_MINUTE("<white>{minute}m", PrefixType.NONE, true, false, "bossbar.minute"),
    BAR_HOUR("<white>{hour}h", PrefixType.NONE, true, false, "bossbar.hour"),
    BAR_REMAINING("left", PrefixType.NONE, true, false, "bossbar.remaining"),

    COMPETITION_ALREADY_RUNNING("<white>There's already a competition running.", PrefixType.ADMIN, false, true, "admin.competition-already-running"),

    COMPETITION_END("<white>The fishing contest has ended.", PrefixType.DEFAULT, false, true, "contest-end"),
    COMPETITION_JOIN("<white>A fishing contest for {type} is going on.", PrefixType.DEFAULT, true, true, "contest-join"),
    COMPETITION_START("<white>A fishing contest for {type} has started.", PrefixType.DEFAULT, false, true, "contest-start"),

    COMPETITION_TYPE_LARGEST("the largest fish", PrefixType.NONE, true, true, "competition-types.largest"),
    COMPETITION_TYPE_LARGEST_TOTAL("the largest total fish length", PrefixType.NONE, true, true, "competition-types.largest-total"),
    COMPETITION_TYPE_MOST("the most fish", PrefixType.NONE, true, true, "competition-types.most"),
    COMPETITION_TYPE_SPECIFIC("{amount} <b>{rarity}</b> {fish}", PrefixType.NONE, true, true, "competition-types.specific"),
    COMPETITION_TYPE_SPECIFIC_RARITY("{amount} <b>{rarity}</b> fish", PrefixType.NONE, true, true, "competition-types.specific-rarity"),
    COMPETITION_TYPE_SHORTEST("the shortest fish", PrefixType.NONE, true, true, "competition-types.shortest"),
    COMPETITION_TYPE_SHORTEST_TOTAL("the shortest total fish length", PrefixType.NONE, true, true, "competition-types.shortest-total"),

    COMPETITION_SINGLE_WINNER("<white>{player} has won the competition for {type}. Congratulations!", PrefixType.DEFAULT, true, true, "single-winner"),



    ECONOMY_DISABLED("<white>EvenMoreFish's economy features are disabled.", PrefixType.ERROR, false, false, "admin.economy-disabled"),

    FISH_CANT_BE_PLACED("<white>You cannot place this fish.", PrefixType.ERROR, true, true, "place-fish-blocked"),
    FISH_CAUGHT("<white><b>{player}</b> has fished a {length}cm <b>{rarity}</b> {fish}!", PrefixType.NONE, true, true, "fish-caught"),
    FISH_LENGTHLESS_CAUGHT("<white><b>{player}</b> has fished a <b>{rarity}</b> {fish}!", PrefixType.NONE, true, true, "lengthless-fish-caught"),
    FISH_HUNTED("<white><b>{player}</b> has hunted a {length}cm <bold>{rarity}</bold> {fish}!", PrefixType.NONE, true, true, "fish-hunted"),
    FISH_LENGTHLESS_HUNTED("<white><b>{player}</b> has hunted a <bold>{rarity}</bold> {fish}!", PrefixType.NONE, true, true, "lengthless-fish-hunted"),
    FISH_LORE(Arrays.asList(
            "{fisherman_lore}",
            "{length_lore}",
            "",
            "{fish_lore}",
            "<b>{rarity}</b>"
    ), PrefixType.NONE, false, true, "fish-lore"),
    FISHERMAN_LORE(Collections.singletonList(
            "<white>Caught by {player}"
    ), PrefixType.NONE, false, true, "fisherman-lore"),
    LENGTH_LORE(Collections.singletonList(
            "<white>Measures {length}cm"
    ), PrefixType.NONE, false, true, "length-lore"),
    FISH_SALE("<white>You've sold <green>{amount} <white>fish for <green>{sell-price}<white>.", PrefixType.DEFAULT, true, true, "fish-sale"),
    HELP_FORMAT(
            "[noPrefix]<aqua>{command} <yellow>- {description}",
            PrefixType.DEFAULT,
            false,
            true,
            "help-format"
    ),
    HELP_GENERAL_TITLE(
            "[noPrefix]<gradient:#f1ffed:#f1ffed><st>         </st><bold><green>EvenMoreFish</green></bold><st><gradient:#73ff6b:#f1ffed>         ",
            PrefixType.DEFAULT,
            false,
            true,
            "help-general.title"
    ),
    HELP_GENERAL_TOP("[noPrefix]Shows an ongoing competition's leaderboard.", PrefixType.DEFAULT, false, true, "help-general.top"),
    HELP_GENERAL_HELP("[noPrefix]Shows you this page.", PrefixType.DEFAULT, false, true, "help-general.help"),
    HELP_GENERAL_SHOP("[noPrefix]Opens a shop to sell your fish.", PrefixType.DEFAULT, false, true, "help-general.shop"),
    HELP_GENERAL_TOGGLE("[noPrefix]Toggles whether or not you receive custom fish.", PrefixType.DEFAULT, false, true, "help-general.toggle"),
    HELP_GENERAL_GUI("[noPrefix]Opens the Main Menu GUI.", PrefixType.DEFAULT, false, true, "help-general.gui"),
    HELP_GENERAL_ADMIN("[noPrefix]Admin command help page.", PrefixType.DEFAULT, false, true, "help-general.admin"),
    HELP_GENERAL_NEXT("[noPrefix]Show how much time is until the next competition.", PrefixType.DEFAULT, false, true, "help-general.next"),
    HELP_GENERAL_SELLALL("[noPrefix]Sell all the fish in your inventory.", PrefixType.DEFAULT, false, true, "help-general.sellall"),
    HELP_GENERAL_APPLYBAITS("[noPrefix]Apply baits to your fishing rod.", PrefixType.DEFAULT, false, true, "help-general.applybaits"),
    HELP_GENERAL_JOURNAL("[noPrefix]View a journal of caught fish and their stats.", PrefixType.DEFAULT, false, true, "help-general.journal"),
    HELP_ADMIN_TITLE(
            "[noPrefix]<white><st> <#ffedeb><st> <#ffdcd7><st> <#ffcac3><st> <#ffb8b0><st> <#ffa69d><st> <#ff948a><st> <#ff8078><st> <#ff6c66><st> <red><st> <white> <red><b>EvenMoreFish</b> <red><st> <#ff6c66><st><st> <#ff8078><st> <#ff948a><st> <#ffa69d><st> <#ffb8b0><st> <#ffcac3><st> <#ffdcd7><st> <#ffedeb><st> <white><st> <white>",
            PrefixType.ADMIN,
            false,
            true,
            "help-admin.title"
    ),
    HELP_ADMIN_BAIT("[noPrefix]Gives baits to a player.", PrefixType.ADMIN, false, true, "help-admin.bait"),
    HELP_ADMIN_COMPETITION("[noPrefix]Starts or stops a competition", PrefixType.ADMIN, false, true, "help-admin.competition"),
    HELP_ADMIN_CLEARBAITS("[noPrefix]Removes all applied baits from a fishing rod.", PrefixType.ADMIN, false, true, "help-admin.clearbaits"),
    HELP_ADMIN_FISH("[noPrefix]Gives a fish to a player.", PrefixType.ADMIN, false, true, "help-admin.fish"),
    HELP_ADMIN_CUSTOMROD("[noPrefix]Gives a custom fishing rod to a player.", PrefixType.ADMIN, false, true, "help-admin.custom-rod"),
    HELP_ADMIN_NBTROD("[noPrefix]Gives a custom NBT rod to a player required for catching EMF fish.", PrefixType.ADMIN, false, true, "help-admin.nbt-rod"),
    HELP_ADMIN_RELOAD("[noPrefix]Reloads the plugin's config files", PrefixType.ADMIN, false, true, "help-admin.reload"),
    HELP_ADMIN_VERSION("[noPrefix]Displays plugin information.", PrefixType.ADMIN, false, true, "help-admin.version"),
    HELP_ADMIN_MIGRATE("[noPrefix]Migrate the database from Legacy (V2) to V3", PrefixType.ADMIN, false, true, "help-admin.migrate"),
    HELP_ADMIN_REWARDTYPES("[noPrefix]Display all registered reward types", PrefixType.ADMIN, false, true, "help-admin.rewardtypes"),
    HELP_ADMIN_ADDONS("[noPrefix]Show all registered addons", PrefixType.ADMIN, false, true, "help-admin.addons"),
    HELP_ADMIN_RAWITEM("[noPrefix]Displays the item in your main hand as raw NBT.", PrefixType.ADMIN, false, true, "help-admin.rawitem"),
    HELP_LIST_FISH("[noPrefix]Display all fish in a specific rarity.", PrefixType.ADMIN, false, true, "help-list.fish"),
    HELP_LIST_RARITIES("[noPrefix]Display all rarities.", PrefixType.ADMIN, false, true, "help-list.rarities"),
    HELP_COMPETITION_START("[noPrefix]Starts a competition of a specified duration", PrefixType.ADMIN, false, true, "help-competition.start"),
    HELP_COMPETITION_END("[noPrefix]Ends the current competition (if there is one)", PrefixType.ADMIN, false, true, "help-competition.end"),
    INVALID_COMPETITION_TYPE("<white>That isn't a type of competition type, available types: MOST_FISH, LARGEST_FISH, SPECIFIC_FISH", PrefixType.ADMIN, false, false, "admin.competition-type-invalid"),
    INVALID_COMPETITION_ID("<white>That isn't a valid competition id.", PrefixType.ADMIN, false, false, "admin.competition-id-invalid"),

    LEADERBOARD_LARGEST_FISH(
            "<white>#{position} | {player} (<b>{rarity}</b> {fish}, {length}cm)",
            PrefixType.DEFAULT,
            false,
            true,
            "leaderboard-largest-fish"
    ),
    LEADERBOARD_LARGEST_TOTAL("<white>#{position} | {player} ({amount}cm)", PrefixType.DEFAULT, false, true, "leaderboard-largest-total"),
    LEADERBOARD_MOST_FISH("<white>#{position} | {player} ({amount} fish)", PrefixType.DEFAULT, false, true, "leaderboard-most-fish"),
    LEADERBOARD_TOTAL_PLAYERS("<white>There are a total of {amount} player(s) in the leaderboard.", PrefixType.DEFAULT, true, true, "total-players"),
    LEADERBOARD_SHORTEST_FISH(
            "<white>#{position} | {player} (<b>{rarity}</b> {fish}, {length}cm)",
            PrefixType.DEFAULT,
            false,
            true,
            "leaderboard-shortest-fish"
    ),
    LEADERBOARD_SHORTEST_TOTAL("<white>#{position} | {player} ({amount}cm)", PrefixType.DEFAULT, false, true, "leaderboard-shortest-total"),

    NEW_FIRST_PLACE_NOTIFICATION("<white>{player} is now #1", PrefixType.DEFAULT, true, true, "new-first"),

    NO_BAITS("<white>The fishing rod does not have any baits applied.", PrefixType.ERROR, true, true, "admin.no-baits-on-rod"),
    NO_COMPETITION_RUNNING("<white>No competition running right now.", PrefixType.ERROR, false, false, "no-competition-running"),
    NO_FISH_CAUGHT("<white>You didn't catch any fish.", PrefixType.DEFAULT, true, true, "no-record"),
    NO_PERMISSION_FISHING("<red>You don't have permission to fish using this rod, you will catch vanilla fish.", PrefixType.DEFAULT, true, true, "no-permission-fishing"),
    NO_PERMISSION("<red>You don't have permission to run that command.", PrefixType.ERROR, false, true, "no-permission"),
    NO_WINNERS("<white>There were no fishing records.", PrefixType.DEFAULT, true, true, "no-winners"),
    NOT_ENOUGH_PLAYERS("<white>There's not enough players online to start the scheduled fishing competition.", PrefixType.ERROR, true, false, "not-enough-players"),

    CUSTOM_FISHING_ENABLED("<green>Enabled", PrefixType.NONE, false, true, "custom-fishing-enabled"),
    CUSTOM_FISHING_DISABLED("<red>Disabled", PrefixType.NONE, false, true, "custom-fishing-disabled"),

    PLACEHOLDER_FISH_FORMAT("{length}cm <b>{rarity}</b> {fish}", PrefixType.NONE, true, false, "emf-competition-fish-format"),
    PLACEHOLDER_FISH_LENGTHLESS_FORMAT("<b>{rarity}</b> {fish}", PrefixType.NONE, true, false, "emf-lengthless-fish-format"),
    PLACEHOLDER_FISH_MOST_FORMAT("{amount} fish", PrefixType.NONE, true, false, "emf-most-fish-format"),
    PLACEHOLDER_NO_COMPETITION_RUNNING("No competition running right now.", PrefixType.NONE, true, false, "no-competition-running"),
    PLACEHOLDER_NO_COMPETITION_RUNNING_FISH("No competition running right now.", PrefixType.NONE, true, false, "no-competition-running-fish"),
    PLACEHOLDER_NO_COMPETITION_RUNNING_SIZE("No competition running right now.", PrefixType.NONE, true, false, "no-competition-running-size"),

    PLACEHOLDER_NO_PLAYER_IN_PLACE("Start fishing to take this place", PrefixType.NONE, true, false, "no-player-in-place"),
    PLACEHOLDER_NO_FISH_IN_PLACE("Start fishing to take this place", PrefixType.NONE, true, false, "no-fish-in-place"),
    PLACEHOLDER_NO_SIZE_IN_PLACE("Start fishing to take this place", PrefixType.NONE, true, false, "no-size-in-place"),
    PLACEHOLDER_SIZE_DURING_MOST_FISH("N/A", PrefixType.NONE, true, false, "emf-size-during-most-fish"),
    PLACEHOLDER_TIME_REMAINING("Time left until next competition: {days}d, {hours}h, {minutes}m.", PrefixType.NONE, true, false, "emf-time-remaining"),
    PLACEHOLDER_TIME_REMAINING_DURING_COMP("There is a competition running right now.", PrefixType.NONE, true, false, "emf-time-remaining-during-comp"),

    RELOAD_SUCCESS("<white>Successfully reloaded the plugin.", PrefixType.ADMIN, false, false, "admin.reload"),
    SELL_PRICE_FORMAT("#,##0.0", PrefixType.NONE, false, false, "sell-price-format"),
    TIME_ALERT("<white>There is {time_formatted} left on the competition for {type}", PrefixType.DEFAULT, false, true, "time-alert"),

    TOGGLE_ON("<white>You will now catch custom fish.", PrefixType.DEFAULT, false, true, "toggle-on"),
    TOGGLE_OFF("<white>You will no longer catch custom fish.", PrefixType.DEFAULT, false, true, "toggle-off"),

    WORTH_GUI_NAME("<dark_blue><b>Sell Fish</b>", PrefixType.NONE, false, false, "worth-gui-name"),
    WORTH_GUI_CONFIRM_ALL_BUTTON_NAME("<gold><b>CONFIRM</b>", PrefixType.NONE, false, false, "confirm-sell-all-gui-name"),
    WORTH_GUI_CONFIRM_BUTTON_NAME("<gold><b>CONFIRM</b>", PrefixType.NONE, false, false, "confirm-gui-name"),
    WORTH_GUI_NO_VAL_BUTTON_NAME("<red><b>Can't Sell</b>", PrefixType.NONE, false, false, "error-gui-name"),
    WORTH_GUI_NO_VAL_BUTTON_LORE(Arrays.asList(
            "<dark_gray>Fish Shop",
            "",
            "<gray>Total Value » <red>{sell-price}",
            "",
            "<gray>Sell your fish here to make",
            "<gray>some extra money.",
            "",
            "<red>» (Left-click) sell the fish.",
            "<gray>» (Right-click) cancel."
    ), PrefixType.NONE, false, false, "error-gui-lore"),
    WORTH_GUI_NO_VAL_ALL_BUTTON_NAME("<red><b>Can't Sell</b>", PrefixType.NONE, false, false, "error-sell-all-gui-name"),
    WORTH_GUI_SELL_ALL_BUTTON_NAME("<gold><b>SELL ALL</b>", PrefixType.NONE, false, false, "sell-all-name"),
    WORTH_GUI_SELL_ALL_BUTTON_LORE(Arrays.asList(
            "<yellow><b>Value</b>: <yellow>${sell-price}", "<gray>LEFT CLICK to sell all fish in your inventory."
    ), PrefixType.NONE, false, false, "sell-all-lore"),
    WORTH_GUI_SELL_BUTTON_NAME("<gold><b>SELL</b>", PrefixType.NONE, false, false, "sell-gui-name"),
    WORTH_GUI_SELL_BUTTON_LORE(Arrays.asList(
            "<dark_gray>Inventory",
            "",
            "<gray>Total Value » <red>{sell-price}",
            "",
            "<gray>Click this button to sell",
            "<gray>the fish in your inventory to",
            "<gray>make some extra money.",
            "",
            "<red>» (Left-click) sell the fish."
    ), PrefixType.NONE, false, false, "error-sell-all-gui-lore"),
    WORTH_GUI_SELL_LORE(Arrays.asList(
            "<dark_gray>Fish Shop",
            "",
            "<gray>Total Value » <yellow>{sell-price}",
            "",
            "<gray>Sell your fish here to make",
            "<gray>some extra money.",
            "",
            "<yellow>» (Left-click) sell the fish.",
            "<gray>» (Right-click) cancel."
    ), PrefixType.NONE, false, false, "sell-gui-lore"),
    RARITY_INVALID("<white>That is not a valid rarity!", PrefixType.ERROR, false, true, "rarity-invalid"),
    BAIT_ROD_LORE(List.of(
            "<white>",
            "<gray>Bait Slots: <yellow>({current_baits}/{max_baits})",
            "<white>",
            "{baits}",
            "<white>"
    ), PrefixType.NONE, false, false, "bait.rod-lore"),
    BAIT_BAIT_LORE(List.of(
            "<white>",
            "Increases the catch rates for:",
            "{boosts}",
            "{lore}",
            "<white>",
            "<#dadada>Drop onto a fishing rod to apply,",
            "<#dadada>or hold <u>SHIFT<#dadada> to apply all.",
            "<white>"
    ), PrefixType.NONE, false, false, "bait.bait-lore"),
    BAIT_BAITS("<gold>► {amount} {bait}", PrefixType.NONE, false, false, "bait.baits"),
    BAIT_BOOSTS_RARITY("► <white>1 Rarity", PrefixType.NONE, false, false, "bait.boosts-rarity"),
    BAIT_BOOSTS_RARITIES("► <white>{amount} Rarities", PrefixType.NONE, false, false, "bait.boosts-rarities"),
    BAIT_BOOSTS_FISH("► <white>{amount} Fish", PrefixType.NONE, false, false, "bait.boosts-fish"),
    BAIT_UNUSED_SLOT("<gray>► ? <i>Available Slot", PrefixType.NONE, false, false, "bait.unused-slot");

    private final String id;
    private final boolean canSilent, canHidePrefix;
    private final PrefixType prefixType;
    private String normal;
    private List<String> normalList;

    /**
     * This is the config enum for a value in the messages.yml file. It does not store the actual data but references
     * where to look in the file for the data. This must be passed through an EMFSingleMessage object before it can be sent to
     * players. In there, it is possible to add variable options, and it will be colour formatted too.
     *
     * @param id            The id in messages.yml for the ConfigMessage.
     * @param normal        The default value in the base messages.yml.
     * @param prefixType    The type of prefix that should be used in this instance.
     * @param canSilent     If the message can be sent silently (not sent).
     * @param canHidePrefix If the message can have the [noPrefix] applied to remove the prefix.
     */
    ConfigMessage(String normal, PrefixType prefixType, boolean canSilent, boolean canHidePrefix, String id) {
        this.id = id;
        this.normal = normal;
        this.canSilent = canSilent;
        this.canHidePrefix = canHidePrefix;
        this.prefixType = prefixType;
    }

    /**
     * This is the config enum for a list value in the messages.yml file. It does not store the actual data but references
     * where to look in the file for the data. This must be passed through an EMFSingleMessage object before it can be sent to
     * players. In there, it is possible to add variable options, and it will be colour formatted too. It also must be
     * a list within the file.
     *
     * @param id            The id in messages.yml for the ConfigMessage.
     * @param normalList    The default value for the list in the base messages.yml.
     * @param prefixType    The type of prefix that should be used in this instance.
     * @param canSilent     If the message can be sent silently (not sent).
     * @param canHidePrefix If the message can have the [noPrefix] applied to remove the prefix.
     */
    ConfigMessage(List<String> normalList, PrefixType prefixType, boolean canSilent, boolean canHidePrefix, String id) {
        this.id = id;
        this.normalList = normalList;
        this.canSilent = canSilent;
        this.canHidePrefix = canHidePrefix;
        this.prefixType = prefixType;
    }

    public String getId() {
        return this.id;
    }

    public String getNormal() {
        return this.normal;
    }

    public List<String> getNormalList() {
        return this.normalList;
    }

    public boolean isCanSilent() {
        return this.canSilent;
    }

    public boolean isCanHidePrefix() {
        return this.canHidePrefix;
    }

    public boolean isListForm() {
        List<String> strings = MessageConfig.getInstance().getConfig().getStringList(getId());
        return normalList != null || !strings.isEmpty();
    }

    public PrefixType getPrefixType() {
        return prefixType;
    }

    public EMFMessage getMessage() {
        if (isListForm()) {
            EMFListMessage listMessage = EMFListMessage.empty();
            listMessage.setCanSilent(this.canSilent);
            List<String> list = getStringList(getNormalList(), getId());
            if (list.isEmpty()) {
                return listMessage;
            }
            for (String line : list) {
                if (this.canHidePrefix && line.startsWith("[noPrefix]")) {
                    listMessage.appendString(line.substring(10));
                } else {
                    EMFMessage prefix = getPrefixType().getPrefix();
                    prefix.appendString(line);
                    listMessage.appendMessage(prefix);
                }
            }
            return listMessage;
        } else {
            String line = getString(getNormal(), getId());
            EMFSingleMessage singleMessage = EMFSingleMessage.empty();
            singleMessage.setCanSilent(this.canSilent);
            if (line != null && !line.isEmpty()) {
                if (this.canHidePrefix && line.startsWith("[noPrefix]")) {
                    singleMessage.appendString(line.substring(10));
                } else {
                    EMFMessage prefix = getPrefixType().getPrefix();
                    prefix.appendString(line);
                    singleMessage.appendMessage(prefix);
                }
            }
            return singleMessage;
        }
    }

    /**
     * If there is a value in the config that matches the id of this enum, that is returned. If not though, the default
     * value stored will be returned and a message is outputted to the console alerting of a missing value.
     *
     * @return The string from config that matches the value of id.
     */
    private String getString(String normal, String id) {
        MessageConfig messageConfig = MessageConfig.getInstance();
        String string = messageConfig.getConfig().getString(id, null);
        if (string == null) {
            EvenMoreFish.getInstance().getLogger().warning("No valid value in messages.yml for: " + id + ". Attempting to insert the default value.");
            messageConfig.getConfig().set(id, normal);
            messageConfig.save();
            EvenMoreFish.getInstance().getLogger().info("Filled " + id + " in your messages.yml with the default value.");
            return normal;
        }
        return string;
    }

    /**
     * If there is a value in the config that matches the id of this enum, that is returned. If not though, the default
     * value stored will be returned and a message is outputted to the console alerting of a missing value. This is for
     * string values however rather than just strings.
     *
     * @return The string list from config that matches the value of id.
     */
    private List<String> getStringList(List<String> normal, String id) {
        MessageConfig messageConfig = MessageConfig.getInstance();
        List<String> list = messageConfig.getConfig().getStringList(id);
        if (list.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().warning("No valid value in messages.yml for: " + id + ". Attempting to insert the default value.");
            messageConfig.getConfig().set(id, null);
            messageConfig.save();
            EvenMoreFish.getInstance().getLogger().info("Filled " + id + " in your messages.yml with the default value.");
            return normal;
        }
        return list;
    }

}
