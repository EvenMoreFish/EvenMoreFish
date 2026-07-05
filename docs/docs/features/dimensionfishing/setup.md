---
title: Setup
sidebar_position: 1
---

## Introduction
Dimension Fishing is a fancy new feature which allows players to fish inside lava or the end's void!

All configs are located under `dimension-fishing` inside config.yml.

This page explains what each setting does.

### Permission
This is the permission the player needs to have to start this type of fishing.

If the player does not have it, the hook will act like vanilla.

### Allowed Worlds
This is a list of worlds this type of fishing is enabled in.

If this is missing or empty, the plugin will default to the following:
- Void is enabled in worlds with the THE_END environment.
- Lava is enabled in worlds with the NETHER environment.

### Swallow Sound
This sound is played when the player misses the fish and the hook is "swallowed" by the lava or void.

It follows the same format as the [Sound RewardType](https://evenmorefish.github.io/EvenMoreFish/docs/configuration/reward-types).

### Bite Sound
This sound is played when the fish bites the hook.

It follows the same format as the [Sound RewardType](https://evenmorefish.github.io/EvenMoreFish/docs/configuration/reward-types).

### Lure Particles
These particles are shown when the fish is being lured towards the hook.

It is configured in a map format, so you can stack as many particles as you'd like.

- `particle` is the type of particle to use. See the [Paper JavaDocs](https://jd.papermc.io/paper/26.2/org/bukkit/Particle.html) for a full list.
- `amount` is the amount to show at the same time. This will allow you to stack the same particle on top of itself.
- `color` - If the particle says it uses Color, Spell, or DustOptions, you can use this config. Example: `color: "#55FFFF"`.
- `color-transition` - If the particle uses DustTransition, you can use this config. Example: `color-transition: "#55FFFF,#FF55FF,10"`.
- `data` - Allows control over the DataType a particle might need. This is shown on the JavaDoc page linked above.
  - For BlockData, you need to provide a block Material: `data: cobblestone`.
  - For ItemStack, you need to provide an item Material: `data: diamond_pickaxe`.
  - For Float, you provide a float value: `data: 1.0`.
  - For Integer, you provide an integer value: `data: 1`.
  - Unsupported types are: Vibration, Trail, Geyser, GeyserBase.

If particles are set up incorrectly, you will be warned in console, and they will no longer be shown.
When a player runs the reload command, all particles are spawned at their current location to filter out broken ones.