---
title: Frequently Asked Questions
sidebar_label: FAQs
description: Answers to common questions about EvenMoreFish plugin
---

# FAQs

This answers a few questions we recognize are asked pretty frequently on [the discord help server](https://discord.gg/9fRbqWTnHS):

## How do I add commands when a fish is caught?

This can be done using the `catch-event` setting for the fish.
```yml
Rusty Repair Spoon:
  item:
    material: IRON_SHOVEL
  catch-event:
    - "COMMAND:repair hand {player}"
    - "MESSAGE:<green>Your fishing rod has been repaired."
```

## How do I give a fish / bait to another player?

To give a fish to a player, use the following command:
`/emf admin fish Common Bluefish 4 Oheers`

To give a bait to a player, use the following command:
`/emf admin bait Shrimp 1 Oheers`

- Note: If using another plugin, you'll want to replace Oheers with the variable used for the player, this varies from plugin to plugin and will look like `[playerName]`, `{player}`, `{playername}` etc.

## How do I change the name of the default fish?

All you have to do is change the name in the .yml files, so

```yml
Blue Shark:
```
Becomes
```yml
Blahaj: 
```
This is exactly the same for rarities - also make sure to also change the name of the fish & rarities in the baits.yml file.
- Note: You should always use english characters for the config keys. To name things in your language, you should use the display name config instead.

## How do I change how much fish sell for in /emf shop?

As explained in the rarity example above the setting, each fish's rarity will have a `worth-multiplier` setting, this is then multiplied by the length of the fish to create the value seen in the `/emf shop`. By increasing or decreasing the worth-multiplier, you'll increase/decrease their worth in the shop.

## How do weights work?

See [Weights](/docs/features/weights)

## How do I use items from other plugins?

See https://evenmorefish.github.io/EvenMoreFish/docs/category/addons

## There is something missing in the documentation! Or there is a mistake!

See [Contributing to the documentation](./contribute-docs.mdx)