---
title: Hooking into EvenMoreFish
sidebar_position: 2
---

EMFFishStew provides a custom RewardType for hooking into the main plugin, explained on the [Reward Types](https://evenmorefish.github.io/EvenMoreFish/docs/configuration/reward-types#external-plugin-types) page.

Here is an example fish that uses this type:
```
fish:
  StewFish:
    item:
      material: cod
      lore:
        - <gray>It has something in its mouth.
        - <gray><italic>Right click to interact.
    interact-event:
      - "FISHSTEW:example"
```