package com.oheers.fish.messages;

import com.oheers.fish.api.Logging;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.messages.abstracted.EMFMessage;
import uk.firedev.messagelib.message.ComponentListMessage;
import uk.firedev.messagelib.message.ComponentMessage;
import uk.firedev.messagelib.message.ComponentSingleMessage;

import static uk.firedev.messagelib.message.ComponentMessage.componentMessage;

/**
 * Represents a message inside the messages.yml file.
 * Determines the prefix type it should use, and handles fetching the message from config.
 */
public enum ConfigMessage {

    ADMIN_CANT_BE_CONSOLE(PrefixType.ERROR, "admin.cannot-run-on-console"),
    ADMIN_GIVE_PLAYER_BAIT(PrefixType.ADMIN, "admin.given-player-bait"),
    ADMIN_GIVE_PLAYER_FISH(PrefixType.ADMIN, "admin.given-player-fish"),
    ADMIN_OPEN_FISH_SHOP(PrefixType.ADMIN, "admin.open-fish-shop"),
    ADMIN_CUSTOM_ROD_GIVEN(
        PrefixType.ADMIN,
        "admin.custom-rod-given"
    ),
    ADMIN_NBT_NOT_REQUIRED(PrefixType.ERROR, "admin.nbt-not-required"),
    ADMIN_NO_BAIT_SPECIFIED(PrefixType.ERROR, "admin.no-bait-specified"),
    ADMIN_NOT_HOLDING_ROD(PrefixType.ERROR, "admin.must-be-holding-rod"),
    ADMIN_NUMBER_FORMAT_ERROR(PrefixType.ERROR, "admin.number-format-error"),
    ADMIN_NUMBER_RANGE_ERROR(PrefixType.ERROR, "admin.number-range-error"),
    ADMIN_UNKNOWN_PLAYER(PrefixType.ERROR, "admin.player-not-found"),
    ADMIN_UPDATE_AVAILABLE(PrefixType.ADMIN, "admin.update-available"),
    ADMIN_LIST_ADDONS(PrefixType.ADMIN, "admin.list-addons"),
    ADMIN_LIST_REWARD_TYPES(PrefixType.ADMIN, "admin.list-reward-types"),

    BAITS_CLEARED(PrefixType.ADMIN, "admin.all-baits-cleared"),
    BAIT_CAUGHT(PrefixType.NONE, "bait-catch"),
    BAIT_USED(PrefixType.DEFAULT, "bait-use"),
    BAIT_WRONG_GAMEMODE(PrefixType.ERROR, "bait-survival-limited"),
    BAITS_MAXED(PrefixType.DEFAULT, "max-baits-reached"),
    BAITS_MAXED_ON_ROD(PrefixType.ERROR, "max-baits-reached"),
    BAIT_ROD_PROTECTION(PrefixType.ERROR, "bait-rod-protection"),
    BAIT_INVALID_ROD(PrefixType.ERROR, "bait-invalid-rod"),

    // Bait Shop
    BAIT_PURCHASED(PrefixType.DEFAULT, "bait-purchased"),
    BAIT_CONFIRM_PURCHASE(PrefixType.DEFAULT, "bait-confirm-purchase"),
    BAIT_CANNOT_AFFORD(PrefixType.ERROR, "bait-cannot-afford"),
    BAIT_NOT_FOR_SALE(PrefixType.ERROR, "bait-not-for-sale"),

    BAR_LAYOUT(PrefixType.NONE, "bossbar.layout"),
    BAR_REMAINING(PrefixType.NONE, "bossbar.remaining"),

    DURATION_SECOND(PrefixType.NONE, "duration.second"),
    DURATION_MINUTE(PrefixType.NONE, "duration.minute"),
    DURATION_HOUR(PrefixType.NONE, "duration.hour"),
    DURATION_DAY(PrefixType.NONE, "duration.day"),

    COMPETITION_ALREADY_RUNNING(PrefixType.ADMIN, "admin.competition-already-running"),

    COMPETITION_END(PrefixType.DEFAULT, "contest-end"),
    COMPETITION_JOIN(PrefixType.DEFAULT, "contest-join"),
    COMPETITION_START(PrefixType.DEFAULT, "contest-start"),

    COMPETITION_TYPE_LARGEST(PrefixType.NONE, "competition-types.largest"),
    COMPETITION_TYPE_LARGEST_TOTAL(PrefixType.NONE, "competition-types.largest-total"),
    COMPETITION_TYPE_MOST(PrefixType.NONE, "competition-types.most"),
    COMPETITION_TYPE_SPECIFIC(PrefixType.NONE, "competition-types.specific"),
    COMPETITION_TYPE_SPECIFIC_RARITY(PrefixType.NONE, "competition-types.specific-rarity"),
    COMPETITION_TYPE_SHORTEST(PrefixType.NONE, "competition-types.shortest"),
    COMPETITION_TYPE_SHORTEST_TOTAL(PrefixType.NONE, "competition-types.shortest-total"),

    COMPETITION_SINGLE_WINNER(PrefixType.DEFAULT, "single-winner"),



    ECONOMY_DISABLED(PrefixType.ERROR, "admin.economy-disabled"),

    FISH_CAUGHT(PrefixType.NONE, "fish-caught"),
    FISH_LENGTHLESS_CAUGHT(PrefixType.NONE, "lengthless-fish-caught"),
    FISH_HUNTED(PrefixType.NONE, "fish-hunted"),
    FISH_LENGTHLESS_HUNTED(PrefixType.NONE, "lengthless-fish-hunted"),
    FISH_LORE(PrefixType.NONE, "fish-lore"),
    FISHERMAN_LORE(PrefixType.NONE, "fisherman-lore"),
    LENGTH_LORE(PrefixType.NONE, "length-lore"),
    FISH_SALE(PrefixType.DEFAULT, "fish-sale"),
    NO_SELLABLE_FISH(PrefixType.ERROR, "no-sellable-fish"),
    HELP_FORMAT(
        PrefixType.DEFAULT,
        "help-format"
    ),
    HELP_GENERAL_TITLE(
        PrefixType.DEFAULT,
        "help-general.title"
    ),
    HELP_GENERAL_TOP(PrefixType.DEFAULT, "help-general.top"),
    HELP_GENERAL_HELP(PrefixType.DEFAULT, "help-general.help"),
    HELP_GENERAL_SHOP(PrefixType.DEFAULT, "help-general.shop"),
    HELP_GENERAL_TOGGLE(PrefixType.DEFAULT, "help-general.toggle"),
    HELP_GENERAL_GUI(PrefixType.DEFAULT, "help-general.gui"),
    HELP_GENERAL_ADMIN(PrefixType.DEFAULT, "help-general.admin"),
    HELP_GENERAL_NEXT(PrefixType.DEFAULT, "help-general.next"),
    HELP_GENERAL_SELLALL(PrefixType.DEFAULT, "help-general.sellall"),
    HELP_GENERAL_APPLYBAITS(PrefixType.DEFAULT, "help-general.applybaits"),
    HELP_GENERAL_JOURNAL(PrefixType.DEFAULT, "help-general.journal"),
    HELP_ADMIN_TITLE(
        PrefixType.ADMIN,
        "help-admin.title"
    ),
    HELP_ADMIN_BAIT(PrefixType.ADMIN, "help-admin.bait"),
    HELP_ADMIN_COMPETITION(PrefixType.ADMIN, "help-admin.competition"),
    HELP_ADMIN_CLEARBAITS(PrefixType.ADMIN, "help-admin.clearbaits"),
    HELP_ADMIN_FISH(PrefixType.ADMIN, "help-admin.fish"),
    HELP_ADMIN_CUSTOMROD(PrefixType.ADMIN, "help-admin.custom-rod"),
    HELP_ADMIN_NBTROD(PrefixType.ADMIN, "help-admin.nbt-rod"),
    HELP_ADMIN_RELOAD(PrefixType.ADMIN, "help-admin.reload"),
    HELP_ADMIN_VERSION(PrefixType.ADMIN, "help-admin.version"),
    HELP_ADMIN_MIGRATE(PrefixType.ADMIN, "help-admin.migrate"),
    HELP_ADMIN_ADDONS(PrefixType.ADMIN, "help-admin.addons"),
    HELP_ADMIN_RAWITEM(PrefixType.ADMIN, "help-admin.rawitem"),
    HELP_LIST_FISH(PrefixType.ADMIN, "help-list.fish"),
    HELP_LIST_RARITIES(PrefixType.ADMIN, "help-list.rarities"),
    HELP_COMPETITION_START(PrefixType.ADMIN, "help-competition.start"),
    HELP_COMPETITION_END(PrefixType.ADMIN, "help-competition.end"),
    INVALID_COMPETITION_TYPE(PrefixType.ADMIN, "admin.competition-type-invalid"),
    INVALID_COMPETITION_ID(PrefixType.ADMIN, "admin.competition-id-invalid"),

    LEADERBOARD_LARGEST_FISH(
        PrefixType.DEFAULT,
        "leaderboard-largest-fish"
    ),
    LEADERBOARD_LARGEST_TOTAL(PrefixType.DEFAULT, "leaderboard-largest-total"),
    LEADERBOARD_MOST_FISH(PrefixType.DEFAULT, "leaderboard-most-fish"),
    LEADERBOARD_TOTAL_PLAYERS(PrefixType.DEFAULT, "total-players"),
    LEADERBOARD_SHORTEST_FISH(
        PrefixType.DEFAULT,
        "leaderboard-shortest-fish"
    ),
    LEADERBOARD_SHORTEST_TOTAL(PrefixType.DEFAULT, "leaderboard-shortest-total"),

    NEW_FIRST_PLACE_NOTIFICATION(PrefixType.DEFAULT, "new-first"),

    NO_BAITS(PrefixType.ERROR, "admin.no-baits-on-rod"),
    NO_COMPETITION_RUNNING(PrefixType.ERROR, "no-competition-running"),
    COMPETITION_TIME_EXTENDED(PrefixType.DEFAULT, "competition-time-extended"),
    NO_FISH_CAUGHT(PrefixType.DEFAULT, "no-record"),
    NO_PERMISSION_FISHING(PrefixType.DEFAULT, "no-permission-fishing"),
    NO_PERMISSION(PrefixType.ERROR, "no-permission"),
    NO_WINNERS(PrefixType.DEFAULT, "no-winners"),
    NOT_ENOUGH_PLAYERS(PrefixType.ERROR, "not-enough-players"),

    CUSTOM_FISHING_ENABLED(PrefixType.NONE, "custom-fishing-enabled"),
    CUSTOM_FISHING_DISABLED(PrefixType.NONE, "custom-fishing-disabled"),

    PLACEHOLDER_FISH_FORMAT(PrefixType.NONE, "emf-competition-fish-format"),
    PLACEHOLDER_FISH_LENGTHLESS_FORMAT(PrefixType.NONE, "emf-lengthless-fish-format"),
    PLACEHOLDER_FISH_MOST_FORMAT(PrefixType.NONE, "emf-most-fish-format"),
    PLACEHOLDER_NO_COMPETITION_RUNNING(PrefixType.NONE, "no-competition-running"),
    PLACEHOLDER_NO_COMPETITION_RUNNING_FISH(PrefixType.NONE, "no-competition-running-fish"),
    PLACEHOLDER_NO_COMPETITION_RUNNING_SIZE(PrefixType.NONE, "no-competition-running-size"),

    PLACEHOLDER_NO_PLAYER_IN_PLACE(PrefixType.NONE, "no-player-in-place"),
    PLACEHOLDER_NO_FISH_IN_PLACE(PrefixType.NONE, "no-fish-in-place"),
    PLACEHOLDER_NO_SIZE_IN_PLACE(PrefixType.NONE, "no-size-in-place"),
    PLACEHOLDER_SIZE_DURING_MOST_FISH(PrefixType.NONE, "emf-size-during-most-fish"),
    PLACEHOLDER_TIME_REMAINING_INACTIVE(PrefixType.NONE, "emf-time-remaining.inactive"),
    PLACEHOLDER_TIME_REMAINING_ACTIVE(PrefixType.NONE, "emf-time-remaining.active"),

    RELOAD_SUCCESS(PrefixType.ADMIN, "admin.reload"),
    TIME_ALERT(PrefixType.DEFAULT, "time-alert"),

    TOGGLE_FISHING_ON(PrefixType.DEFAULT, "toggle.fishing.on"),
    TOGGLE_FISHING_OFF(PrefixType.DEFAULT, "toggle.fishing.off"),
    TOGGLE_BOSSBAR_ON(PrefixType.DEFAULT, "toggle.bossbar.on"),
    TOGGLE_BOSSBAR_OFF(PrefixType.DEFAULT, "toggle.bossbar.off"),

    WORTH_GUI_NAME(PrefixType.NONE, "worth-gui-name"),
    WORTH_GUI_CONFIRM_ALL_BUTTON_NAME(PrefixType.NONE, "confirm-sell-all-gui-name"),
    WORTH_GUI_CONFIRM_BUTTON_NAME(PrefixType.NONE, "confirm-gui-name"),
    WORTH_GUI_NO_VAL_BUTTON_NAME(PrefixType.NONE, "error-gui-name"),
    WORTH_GUI_NO_VAL_BUTTON_LORE(PrefixType.NONE, "error-gui-lore"),
    WORTH_GUI_NO_VAL_ALL_BUTTON_NAME(PrefixType.NONE, "error-sell-all-gui-name"),
    WORTH_GUI_SELL_ALL_BUTTON_NAME(PrefixType.NONE, "sell-all-name"),
    WORTH_GUI_SELL_ALL_BUTTON_LORE(PrefixType.NONE, "sell-all-lore"),
    WORTH_GUI_SELL_BUTTON_NAME(PrefixType.NONE, "sell-gui-name"),
    WORTH_GUI_SELL_BUTTON_LORE(PrefixType.NONE, "error-sell-all-gui-lore"),
    WORTH_GUI_SELL_LORE(PrefixType.NONE, "sell-gui-lore"),
    RARITY_INVALID(PrefixType.ERROR, "rarity-invalid"),
    JOURNAL_DISABLED(PrefixType.ERROR, "journal-disabled"),
    BAIT_ROD_LORE(PrefixType.NONE, "bait.rod-lore"),
    BAIT_BAIT_LORE(PrefixType.NONE, "bait.bait-lore"),
    BAIT_BAITS(PrefixType.NONE, "bait.baits"),
    BAIT_BOOSTS_RARITY(PrefixType.NONE, "bait.boosts-rarity"),
    BAIT_BOOSTS_RARITIES(PrefixType.NONE, "bait.boosts-rarities"),
    BAIT_BOOSTS_FISH(PrefixType.NONE, "bait.boosts-fish"),
    BAIT_UNUSED_SLOT(PrefixType.NONE, "bait.unused-slot");

    private final String id;
    private final PrefixType prefixType;

    ConfigMessage(PrefixType prefixType, String id) {
        this.id = id;
        this.prefixType = prefixType;
    }

    public String getId() {
        return this.id;
    }

    public PrefixType getPrefixType() {
        return prefixType;
    }

    public EMFMessage getMessage() {
        MessageConfig config = MessageConfig.getInstance();
        ComponentMessage message = componentMessage(
            config.getMessageLoader(),
            getId()
        );
        if (message == null) {
            Logging.warn("No valid value in messages.yml for: " + id + ". Using the default value.");
            message = componentMessage(
                config.getDefaultMessageLoader(),
                getId()
            );
        }

        if (message instanceof ComponentListMessage listMessage) {
            return processList(listMessage);
        } else if (message instanceof ComponentSingleMessage singleMessage) {
            return processSingle(singleMessage);
        } else {
            Logging.warn("Unknown message type for " + getId() + ". Returning an empty message.");
            return EMFSingleMessage.empty();
        }
    }

    private EMFMessage processList(ComponentListMessage list) {
        list = list.editAllLines(line -> {
            // If silent, return null to remove the line.
            if (line.contains("-s")) {
                return null;
            }

            // If hide prefix, remove the [noPrefix] tag and don't add a prefix.
            if (line.contains("[noPrefix]")) {
                return line.replace("[noPrefix]", "");
            }
            // Otherwise, add the prefix.
            return line.prepend(getPrefixType().getPrefix());
        });
        return EMFListMessage.ofUnderlying(list);
    }

    private EMFMessage processSingle(ComponentSingleMessage single) {
        // If silent, return an empty message.
        if (single.contains("-s")) {
            return EMFSingleMessage.empty();
        }

        // If hide prefix, remove the [noPrefix] tag and don't add a prefix.
        if (single.contains("[noPrefix]")) {
            single = single.replace("[noPrefix]", "");
        // Otherwise, add the prefix.
        } else {
            single = single.prepend(getPrefixType().getPrefix());
        }
        return EMFSingleMessage.ofUnderlying(single);
    }

}
