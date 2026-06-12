---
title: Piñata Config
sidebar_position: 3
---

## Introduction
This page explains every possible Piñata setting you can use.

See the [example](https://github.com/EvenMoreFish/EMFPinata/blob/main/src/main/resources/pinatas/_example.yml) for a formatted yaml file.

### ID
The Piñata ID is configured with the `id` key. This is the only required key, and will be used to identify the Piñata in the command and reward type.

Example:
```yaml
id: myPinata
```

### Disabled
The Piñata can be disabled with the `disabled` key. This will stop the plugin from loading this Piñata.

Example:
```yaml
disabled: false
```

### Entity Type
Entity types can be configured with the `entity-type` key. Valid configurations are listed below:

| Plugin     | Example                       | Description                                       |
|------------|-------------------------------|---------------------------------------------------|
| None       | `llama`                       | Spawns a vanilla entity.                          |
| MythicMobs | `mythic:SkeletonKing`         | Spawns a MythicMob entity.                        |

Examples:
```yaml
entity-type: llama # Spawns a Llama entity
```
```yaml
entity-type: mythic:SkeletonKing # Spawns a Skeleton King from the MythicMobs plugin.
```

### Display Name
The Piñata's display name is configured with the `display-name` key. This can be anything, and supports [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting.

Example:
```yaml
display-name: <rainbow>Rainbow Piñata
```

### Health
The Piñata's health is configured with the `health` key. This can make your Piñatas more difficult to kill.

Example:
```yaml
health: 250
```

### Glowing
The Piñata can be set to glowing with the `glowing` key. This will apply a white glowing outline around the entity.

This color can be changed from white with the `glow-color` key, and supports any color listed on this page:
https://jd.advntr.dev/api/4.24.0/net/kyori/adventure/text/format/NamedTextColor.html

Example:
```yaml
glowing: true
glow-color: yellow
```

### Silent
The Piñata can be made silent with the `silent` key. This will stop the entity from making any sounds.

Example:
```yaml
silent: true
```

### Awareness
The Piñata can have its awareness removed with the `has-awareness` key. This will prevent the entity from wandering around.

Example:
```yaml
has-awareness: false
```

### Rewards
The Piñata can be assigned rewards using the `rewards` key. For valid rewards, see the [Reward Types](https://evenmorefish.github.io/EvenMoreFish/docs/configuration/reward-types) page:

Example:
```yaml
rewards:
  - ITEM:STONE,10
  - MESSAGE:<red>You killed the Piñata!
```

Note: You cannot spawn another Piñata as a reward, as the plugin will exclude the reward type from the list.

### Effects
As of EMFPiñata 2.0.2, you can assign effects to Piñatas with the `effects` key. This uses the same format as EvenMoreFish.

Example:
```yaml
effects:
  - "SPEED,5,100"
  - "OOZING,1,50"
```

### Equipment
As of EMFPiñata 2.0.2, you can assign equipment to Piñatas with the `equipment` key. The items are configured identically to EvenMoreFish.

Example:
```yaml
equipment:
  # Equip the main hand with glowing bedrock.
  hand:
    item:
      material: bedrock
      glowing: true
  # Equip the off hand with a glowing netherite pickaxe.
  off_hand:
    item:
      material: netherite_pickaxe
      glowing: true
  # Equip the head with a FireML player head.
  head:
   item:
     head-uuid: 8c97e9bc-ec74-4e02-bf9e-37d1852e7ea6
  # Equip the chest with a diamond chestplate.
  chest:
    item:
      material: diamond_chestplate
  # Equip the legs with diamond leggings.
  legs:
    item:
      material: diamond_leggings
  # Equip the feet with netherite boots.
  feet:
    item:
      material: netherite_boots
```
