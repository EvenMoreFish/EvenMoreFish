---
title: Placeholders
---

## Placeholders

Below is a list of the plugin's placeholders and what they do:

### Competition:
- `%emf_competition_place_size_{place}%` - The size of the fish at the given place in the competition.
- `%emf_competition_place_fish_{place}%` - The fish at the given place in the competition.
- `%emf_competition_place_player_{place}%` - The player at the given place in the competition.
- `%emf_competition_time_left%` - The time until the next competition.
- `%emf_competition_active%` - Whether a competition is active or not. true/false.
- `%emf_competition_type%` - The type of the active competition. (e.g. `LARGEST_FISH` or `MOST_FISH`)
- `%emf_competition_type_format%` - The type of the active competition formatted for display. (e.g. `Largest Fish` or `Most Fish`)

### Economy:
- `%emf_total_money_earned_{uuid}%` - The total amount of money earned by the player with the given UUID.
- `%emf_total_fish_sold_{uuid}%` - The total amount of fish sold by the player with the given UUID.

### Player:
- `%emf_custom_fishing_boolean%` - Whether custom fishing is enabled for the linked player or not. true/false.
- `%emf_custom_fishing_status%` - Whether custom fishing is enabled for the linked player or not. Message is configurable in messages.yml.