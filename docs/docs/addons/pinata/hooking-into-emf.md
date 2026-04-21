---
title: Hooking into EvenMoreFish
sidebar_position: 2
---

EMFPiñata provides a custom RewardType for hooking into the main plugin, explained on the [Reward Types](https://evenmorefish.github.io/EvenMoreFish/docs/configuration/reward-types#external-plugin-types) page.

Here is an example fish that uses this type:
```
fish:
  Default_Pinata:
    item:
      raw-material: air
      displayname: "<gray><bold>Default Piñata"
    catch-event:
      - "PINATA:default"
```
Setting the material to air will remove the fish item, and using the catch-event to spawn the Piñata will give the illusion that you caught the Piñata itself.