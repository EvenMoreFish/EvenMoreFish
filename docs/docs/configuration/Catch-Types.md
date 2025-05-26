---
title: Catch Types
---

## What are they?
As of EvenMoreFish 2.0, there are three different catch types:
- `CATCH` - These fish are obtained by fishing with a rod.
- `HUNT` - These fish are obtained by killing naturally spawning fish.
- `BOTH` - These fish can be obtained by either method listed above.

## Configuration
The catch type for each fish is defined in the fish or rarity configs. The default is `BOTH` if not specified. Below are the relevant sections of the config:

Rarity Config:
```yaml
# The catch type for this rarity. Defaults to BOTH if missing.
# Current supported values: CATCH, HUNT, BOTH
catch-type: BOTH
```

Fish Config:
```yaml
# The catch type for this fish. This overrides the rarity's value.
catch-type: BOTH
```

These configs allow you to reserve whole rarities or specific fish for certain methods of obtaining them.

For example, you could have a rarity that is only obtainable by fishing with a rod, and then a fish within that rarity that is only obtainable by killing naturally spawning fish.

## Type: BOTH
This is the default type for every fish and rarity, and allows both fishing and hunting to obtain the fish.

## Type: CATCH
These fish are obtained via a fishing rod. You can configure this in your config.yml:
```yaml
fishing:
  # Should fish be caught from fishing rods?
  catch-enabled: true
  # Should EMF fish only be given during a competition?
  catch-only-in-competition: false
```

## Type: HUNT
These fish are obtained by killing naturally spawning fish. You can configure this in your config.yml:
```yaml
fishing:
  # Should fish be caught by killing fish entities?
  hunt-enabled: false
  # Should fish hunting only be enabled during a competition?
  hunt-only-in-competition: false
  # Allows spawner fish to be ignored for hunting
  hunt-ignore-spawner-fish: true
```

Baits are always excluded from this type, as it would not make sense for a fish entity to drop a bait.