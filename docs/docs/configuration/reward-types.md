---
title: Reward Types
---
To use, change the identifier to the one you want using the table below:
```yaml title="Example"
rewards:
  1:
    - "ITEM:diamond,5"
    - "MESSAGE:<yellow>Congratulations, {player}!"
  2:
    - "ITEM:diamond,1"
```

## Default Types
| Reward Type | Example                             | Description                                       | Available placeholders                     |
|-------------|-------------------------------------|---------------------------------------------------|--------------------------------------------|
| Command     | `COMMAND:msg {player} Reward Type!` | Run a command.                                    | `{player}`, `{x}`, `{y}`, `{z}`, `{world}` |
| Effect      | `EFFECT:POISON,1,2`                 | Adds a potion effect. (name,amplifier,duration)   | -                                          |
| Health      | `HEALTH:5`                          | Adds health.                                      | -                                          |
| Hunger      | `HUNGER:20`                         | Sets hunger level                                 | -                                          |
| Items       | `ITEM:cobblestone,1`                | Gives an item. This supports Item Addons.         | -                                          |
| Message     | `MESSAGE:<green>Reward Type!`       | Sends a message                                   | `{player}`, PlaceholderAPI                 |
| Exp         | `EXP:5000`                          | Gives experience points.                          | -                                          |
| Sound       | `SOUND:ENTITY_WOLF_GROWL,1,5`       | Plays a sound to the player. (sound,volume,pitch) | -                                          |

## External Plugin Types
_These will only load if the relevant external plugin is installed._
| Reward Type                  | Plugin                    | Example                                 | Description            |
|------------------------------|---------------------------|-----------------------------------------|------------------------|
| PlayerPoints                 | PlayerPoints              | `PlayerPoints:10`                       | Gives player points.   |
| GriefPrevention Claim Blocks | GriefPrevention           | `GP_CLAIM_BLOCKS:5`                     | Gives GP claim blocks. |
| AuraSkills XP                | AuraSkills                | `AURASKILLS_XP:auraskills/fishing,10`   | Gives AuraSkills xp.   |
| McMMO XP                     | McMMO                     | `MCMMO_XP:Woodcutting,15`               | Gives McMMO xp.        |
| Permission                   | Vault + Permission Plugin | `PERMISSION:emf.example`                | Gives a permission.    |
| Money                        | Vault + Economy Plugin    | `MONEY:1000`                            | Gives money.           |