---
title: Requirements
---
## How do requirements work?
Requirements set a list of rules for a fish to appear and can be applied to any fish to limit fish to only appear when the rule(s) are met.

A fish with no requirements will be able to be caught. If a player fails the requirements for every fish, an error will be sent to console.

## Rarity requirements
It is possible to add in requirements to rarities. These are applied exactly the same as they are to fish and all requirements available to fish are available to rarities - if a user doesn't meet the criteria for a rarity they won't be able to catch any fish in that rarity at all.

An example of using requirements for a rarity:
```yaml
# The id for this rarity.
id: Common
# How likely is the rarity to be chosen? Having a greater weight means the rarity is more likely to be chosen (the total weights don't have to add to 100)
weight: 100
# What colour should the fish's name be?
format: '<gray>{name}</gray>'
# Prices in /emf shop are calculated by using the calculation "length * worth-multiplier". You can change the worth-multiplier here.
worth-multiplier: 0.1
# Should a catch of this fish be broadcasted to everyone (true) or just the fisher (false)?
broadcast: false
# Change the sizing ranges of the fish.
size:
  # Absolute minimum size the fish can be, this must be an integer
  minSize: 1
  # Absolute maximum size the fish can be, this must be an integer
  maxSize: 30
requirements:
  permission: "emf.rarity.common"
```

***

## Requirements

### Biome
This lets you define a list of biomes the fish will only appear in, allowing you to create fish from cool areas and fish from hot areas. This takes in a <u>list</u> value, allowing for multiple biomes allowed per fish.

A config example of using the biome requirement:
```yaml
Atlantic Cod:
  requirements:
    biome:
      - COLD_OCEAN
      - DEEP_COLD_OCEAN
      - DEEP_LUKEWARM_OCEAN
      - DEEP_OCEAN
      - LUKEWARM_OCEAN
      - OCEAN
      - WARM_OCEAN
```

- Note: To create groups and reuse them, try Biome Sets (explained below this section).

### Biome Sets

Biome Sets are configurable in config.yml. Here is the default config for them:
```yaml
biome-sets:
  # oceans biome set. you can add more sets as you please.
  oceans: # This is the name of the biome set.
    - COLD_OCEAN
    - DEEP_COLD_OCEAN
    - DEEP_LUKEWARM_OCEAN
    - DEEP_OCEAN
    - LUKEWARM_OCEAN
    - OCEAN
    - WARM_OCEAN
```

A config example of using the biome set requirement:
```yaml title="Biome Sets Example"
    Atlantic Cod:
      requirements:
        biome-set:
        - oceans
```

### In-game Time
This restricts your users to only catching fish during certain times of in-game day, this time will be inputted based on the current in-game tick, which happens 20 times every second. For example, 15 seconds past midnight would be `20*15` and four minutes would be `4*60*20`. Like minSize/maxSize, you need to specify a minTime & maxTime in the format of `min-max`.

A config example of using the ingame-time requirement:
```yaml title="In-game Time Example"
Atlantic Cod:
  requirements:
    ingame-time: 2000-4000 # Any time between the first 2000 ticks (100 seconds) and 4000 ticks (200 seconds) of the ingame day.
```

### IRL Time
This restricts your users to only catching fish during certain times of the real-world day, this is inputted similar to times for [Competition Configs](https://evenmorefish.github.io/EvenMoreFish/docs/features/competitions/configs#option-1-times) where they are formatted by 00:00 in 24-hour format. Like ingame-time, you need to specify the minimum and maximum time.

A config example of using the irl-time requirement:
```yaml title="Irl Time Example"
Atlantic Cod:
  requirements:
    irl-time: 01:00-03:00 # Any time between 1AM and 3AM in real life.
```

### Moon Phases
This lets you restrict fish to certain moon phases, it is recommended to combine this with the ingame-time requirement as otherwise the fish may appear during daytime too, when the moon phase is irrelevant. Below is a list of all the moon phase types as referenced in EvenMoreFish and a screenshot from the Minecraft fandom wiki showing all the moon phases.

<details>
  <summary>View the moon phases ( click to open )</summary>

* `FULL_MOON`
* `WANING_GIBBOUS`
* `LAST_QUARTER`
* `WANING_CRESCENT`
* `NEW_MOON`
* `WAXING_CRESCENT`
* `FIRST_QUARTER`
* `WAXING_GIBBOUS`

Moon Phases listed on the Minecraft Wiki: https://minecraft.wiki/w/Moon#Phases

</details>

A config example of using the moon-phase requirement:
```yaml title="Moon Phases Example"
Atlantic Cod:
  requirements:
    moon-phase:
      # When the moon phase matches either of these.
      - WAXING_GIBBOUS
      - NEW_MOON
```

### Permission
This will fail if the player does not have the correct permission.

A config example of using the permission requirement:
```yaml title="Permission Example"
Atlantic Cod:
  requirements:
    permission: "emf.fish.atlanticcod" # When the player has this permission.
```

### Region
This lets you limit fish to a specific region(s) to appear in however, this will override your global value for this set in config.yml, for example, if you whitelist "region" in your config.yml but don't include it in your region requirement, the fish won't be able to spawn in that region. 
[This requires WorldGuard or RedProtect]

A config example of using the region requirement:
```yaml title="Region Example"
Atlantic Cod:
  requirements:
    region: FishingRegion # When the player is inside this region.
```

### Weather
This limits fish to only appear in certain weather.

Valid Options:
- CLEAR
- DOWNFALL

A config example of using the weather requirement:
```yaml title="Weather Example"
Atlantic Cod:
  requirements:
    weather: DOWNFALL # When it is raining or snowing.
```

### Disabled
This will automatically fail the requirement check.

A config example of using the disabled requirement:
```yaml title="Disabled Example"
Atlantic Cod:
  requirements:
    disabled: true # Ensures the check always fails.
```

### Group
This limits the fish to specific permission groups.

A config example of using the group requirement:
```yaml title="Group Example"
Atlantic Cod:
  requirements:
    group: donator # When the player is in the "donator" permission group.
```

### Nearby Players
This limits the fish to only be caught when the provided amount of players is nearby.

The distance this will check is configured in the config.yml file. Higher values may cause lag.

A config example of using the nearby players requirement:
```yaml title="Nearby Players Example"
Atlantic Cod:
  requirements:
    nearby-players: 5 # When there are 5 players within range of the player.
```

### Placeholder
This checks the output of a PlaceholderAPI placeholder against the value you choose.

This will allow you to check values from plugins that EMF cannot hook into via their placeholders.

Valid Operators:
- == - Equals
- = - Equals
- != - Not Equals
- \> - More than (Only works on numbers)
- \>= - More than or equal to (Only works on numbers)
- \< - Less than (Only works on numbers)
- \<= - Less than or equal to (Only works on numbers)

A config example of using the placeholder requirement:
```yaml title="Placeholder Example"
Atlantic Cod:
  requirements:
    placeholder:
      # The requirement passes if the player name is KitterKatter.
      - "%player_name% == KitterKatter"
      # The requirement passes if the player is not in a desert biome.
      - "%player_biome% != Desert"
      # The requirement passes if the player's exp is more than or equal to 1000.
      - "%player_current_exp% >= 1000"
```

### Reward Type
This checks that all provided RewardTypes are available.

```yaml title="Reward Type Example"
Atlantic Cod:
  requirements:
    reward-type:
      # The requirement passes if the MONEY RewardType is available.
      - MONEY
  catch-event:
    # Because our requirement passed, we can safely use the MONEY RewardType.
    - MONEY:5000
```

### Fishing Type
Checks the type of fishing being done. This requires the DimensionFishing plugin.

Valid values are:
- Vanilla
- Lava
- Void

If not configured, the fish will be available to all three types.

```yaml title="Fishing Type Example"
Cod:
  requirements:
    fishing-type:
      - Vanilla
Emberfish:
  requirements:
    fishing-type:
      - Lava
Enderfish:
  requirements:
    fishing-type:
      - Void
```

### World
And finally, this limits fish to certain worlds. By default, there are only three worlds, `overworld`, `nether` and `end`, but with a plugin like [Multiverse-Core](https://modrinth.com/plugin/multiverse-core), you can create worlds to allow fish to only be caught in.

A config example of using the world requirement:
```yaml title="World Example"
Atlantic Cod:
  requirements:
    world: FishingWorld # When the player is in this world.
```