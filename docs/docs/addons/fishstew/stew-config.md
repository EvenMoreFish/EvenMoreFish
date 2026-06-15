---
title: Stew Config
sidebar_position: 3
---

## Introduction
This page explains every possible stew setting you can use.

See the [example](https://github.com/EvenMoreFish/FishStew/blob/main/src/main/resources/items/_example.yml) for a formatted yaml file.

### ID
The Stew ID is configured with the `id` key. This is the only required key, and will be used to identify the Stew in the command and reward type.

Example:
```yaml
id: myStew
```

### Disabled
The Stew can be disabled with the `disabled` key. This will stop the plugin from loading it.

Example:
```yaml
disabled: false
```

### Competition ID
The id of the competition to start. This uses the id inside the competition file.

Example:
```yaml
competition-id: mainCompetition
```

### Respect Minimum Players
Whether this stew should respect minimum players. If false, the competition will start regardless of player count.

Example:
```yaml
respect-minimum-players: true
```

### Item
Configuration for the physical item.

See [Item Configs](https://evenmorefish.github.io/EvenMoreFish/docs/configuration/items) for detailed information.

Example:
```yaml
item:
  material: mushroom_stew
  displayname: Fish Stew
  lore:
    - <gray>Right click to start a fishing competition.
```