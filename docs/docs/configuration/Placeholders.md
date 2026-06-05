---
title: Placeholders
---

## Placeholders

Below is a list of the plugin's PlaceholderAPI placeholders and what they do.

All placeholders use the `emf` identifier, for example `%emf_total_fish_sold_player%`.

### Competition

- `%emf_competition_active%` - Whether a competition is active or not. Returns `true` or `false`.
- `%emf_competition_place_fish_{place}%` - The fish in the given leaderboard place in the active competition.
- `%emf_competition_place_player_{place}%` - The player in the given leaderboard place in the active competition.
- `%emf_competition_place_size_{place}%` - The fish size in the given leaderboard place in the active competition.
- `%emf_competition_time_left%` - The time until the next competition, or the remaining time in the active competition.
- `%emf_competition_type%` - The type of the active competition. For example `LARGEST_FISH` or `MOST_FISH`.
- `%emf_competition_type_format%` - The type of the active competition formatted for display. For example `Largest Fish` or `Most Fish`.

### Database Player Stats

- `%emf_total_competitions_joined_{uuid}%` - The total amount of competitions joined by the player with the given UUID.
- `%emf_total_competitions_joined_player%` - The total amount of competitions joined by the linked player.
- `%emf_total_competitions_won_{uuid}%` - The total amount of competitions won by the player with the given UUID.
- `%emf_total_competitions_won_player%` - The total amount of competitions won by the linked player.
- `%emf_total_fish_caught_{uuid}%` - The total raw amount of fish caught by the player with the given UUID.
- `%emf_total_fish_caught_player%` - The total raw amount of fish caught by the linked player.
- `%emf_total_fish_sold_{uuid}%` - The total amount of fish sold by the player with the given UUID.
- `%emf_total_fish_sold_player%` - The total amount of fish sold by the linked player.
- `%emf_total_money_earned_{uuid}%` - The total amount of money earned by the player with the given UUID.
- `%emf_total_money_earned_player%` - The total amount of money earned by the linked player.
- `%emf_distinct_fish_caught_in_rarity_{rarity}_{uuid}%` - The number of distinct fish the player has caught in the given rarity.
- `%emf_distinct_fish_caught_in_rarity_{rarity}_player%` - The number of distinct fish the linked player has caught in the given rarity.
- `%emf_fish_caught_out_of_rarity_{rarity}_{uuid}%` - The number of distinct fish the player has caught in the given rarity out of the total configured fish in that rarity. Example output: `5/10`.
- `%emf_fish_caught_out_of_rarity_{rarity}_player%` - The number of distinct fish the linked player has caught in the given rarity out of the total configured fish in that rarity.
- `%emf_fish_caught_out_of_total_{uuid}%` - The number of distinct fish the player has caught out of the total configured fish across all rarities. Example output: `25/120`.
- `%emf_fish_caught_out_of_total_player%` - The number of distinct fish the linked player has caught out of the total configured fish across all rarities.
- `%emf_first_uncaught_fish_{rarity}_{uuid}%` - The first uncaught fish id for the player in the given rarity. Returns an empty string if the rarity is complete.
- `%emf_first_uncaught_fish_{rarity}_player%` - The first uncaught fish id for the linked player in the given rarity.
- `%emf_has_caught_{rarity}:{fish}_{uuid}%` - Whether the player has caught the given fish at least once. Returns `true` or `false`.
- `%emf_has_caught_{rarity}:{fish}_player%` - Whether the linked player has caught the given fish at least once.
- `%emf_has_completed_collection_{uuid}%` - Whether the player has caught every configured fish at least once. Returns `true` or `false`.
- `%emf_has_completed_collection_player%` - Whether the linked player has caught every configured fish at least once.
- `%emf_has_completed_rarity_{rarity}_{uuid}%` - Whether the player has caught every configured fish in the given rarity at least once. Returns `true` or `false`.
- `%emf_has_completed_rarity_{rarity}_player%` - Whether the linked player has caught every configured fish in the given rarity at least once.
- `%emf_percent_caught_in_rarity_{rarity}_{uuid}%` - The player's distinct collection progress in the given rarity as a percentage. Example output: `66.7%`.
- `%emf_percent_caught_in_rarity_{rarity}_player%` - The linked player's distinct collection progress in the given rarity as a percentage.
- `%emf_percent_caught_total_{uuid}%` - The player's distinct collection progress across all configured fish as a percentage. Example output: `66.7%`.
- `%emf_percent_caught_total_player%` - The linked player's distinct collection progress across all configured fish as a percentage.
- `%emf_remaining_fish_in_rarity_{rarity}_{uuid}%` - The number of distinct fish in the given rarity the player has not caught yet.
- `%emf_remaining_fish_in_rarity_{rarity}_player%` - The number of distinct fish in the given rarity the linked player has not caught yet.
- `%emf_remaining_fish_total_{uuid}%` - The number of distinct fish across all rarities the player has not caught yet.
- `%emf_remaining_fish_total_player%` - The number of distinct fish across all rarities the linked player has not caught yet.
- `%emf_times_caught_{rarity}:{fish}_{uuid}%` - The raw number of times the player has caught the given fish.
- `%emf_times_caught_{rarity}:{fish}_player%` - The raw number of times the linked player has caught the given fish.
- `%emf_total_fish_caught_in_rarity_{rarity}_{uuid}%` - The raw number of fish the player has caught in the given rarity, including duplicates.
- `%emf_total_fish_caught_in_rarity_{rarity}_player%` - The raw number of fish the linked player has caught in the given rarity, including duplicates.

### Player

- `%emf_custom_fishing_boolean%` - Whether custom fishing is enabled for the linked player or not. Returns `true` or `false`.
- `%emf_custom_fishing_status%` - Whether custom fishing is enabled for the linked player or not. The displayed message is configurable in `messages.yml`.

### Notes

- The `total_fish_caught` placeholders are raw catch totals. Catching the same fish multiple times increases this value each time.
- The `fish_caught_out_of_rarity`, `fish_caught_out_of_total`, `distinct_fish_caught_in_rarity`, `remaining_fish_*`, `percent_caught_*`, and `has_completed_*` placeholders all use distinct fish counts. Catching the same fish multiple times still only counts as `1`.
- Fish-specific placeholders use the format `{rarity}:{fish}` before the final `_player` or `_{uuid}` suffix. Example: `%emf_times_caught_rare:angler_player%`.
- Placeholders using `_player` require a linked player context from PlaceholderAPI.
