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
- `%emf_fish_caught_out_of_rarity_{rarity}_{uuid}%` - The number of distinct fish the player has caught in the given rarity out of the total configured fish in that rarity. Example output: `5/10`.
- `%emf_fish_caught_out_of_rarity_{rarity}_player%` - The number of distinct fish the linked player has caught in the given rarity out of the total configured fish in that rarity.
- `%emf_fish_caught_out_of_total_{uuid}%` - The number of distinct fish the player has caught out of the total configured fish across all rarities. Example output: `25/120`.
- `%emf_fish_caught_out_of_total_player%` - The number of distinct fish the linked player has caught out of the total configured fish across all rarities.

### Player

- `%emf_custom_fishing_boolean%` - Whether custom fishing is enabled for the linked player or not. Returns `true` or `false`.
- `%emf_custom_fishing_status%` - Whether custom fishing is enabled for the linked player or not. The displayed message is configurable in `messages.yml`.

### Notes

- The `total_fish_caught` placeholders are raw catch totals. Catching the same fish multiple times increases this value each time.
- The `fish_caught_out_of_rarity` and `fish_caught_out_of_total` placeholders count distinct fish only. Catching the same fish multiple times still only counts as `1`.
- Placeholders using `_player` require a linked player context from PlaceholderAPI.
