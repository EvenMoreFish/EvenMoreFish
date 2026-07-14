---
title: Boosting Rarity Chances
sidebar_position: 4
---

## What are rarity boosts?

The [RarityWeightBoost](https://github.com/EvenMoreFish/EvenMoreFish/blob/master/even-more-fish-api/src/main/java/com/oheers/fish/api/boost/RarityWeightBoost.java)
interface lets your plugin change the chance of specific rarities being rolled, per catch.
Every time EvenMoreFish picks a rarity, it asks each registered boost for a weight multiplier
for each candidate rarity:

- `1.0` leaves the rarity unchanged,
- values above `1.0` make it more likely,
- values between `0.0` and `1.0` make it less likely.

When several plugins register boosts, their multipliers are combined by multiplication.

This makes it easy to build things like area-of-effect fishing buffs, timed events or
custom enchantments that improve catches, without touching EvenMoreFish's configs.

## Example

```java
public class LuckyHourBoost implements RarityWeightBoost {

    /**
     * A unique key for this boost, conventionally your plugin's name.
     */
    @Override
    public @NotNull String getKey() {
        return "MyPlugin";
    }

    /**
     * Called on the server thread for every candidate rarity of every catch,
     * so keep this fast and never touch blocking IO.
     */
    @Override
    public double weightMultiplier(@NotNull Player fisher, @NotNull Location location, @NotNull String rarityId) {
        // Double the legendary chance while the lucky hour is active.
        if (isLuckyHour() && rarityId.equals("legendary")) {
            return 2.0;
        }
        return 1.0;
    }
}
```

The `rarityId` is the id of the rarity as configured in EvenMoreFish's `rarities` folder
(for example `common`, `rare`, `epic` or `legendary`).

## Registering your boost

Register the boost when your plugin enables and unregister it when it disables:

```java
@Override
public void onEnable() {
    RarityBoostRegistry.getInstance().register(new LuckyHourBoost());
}

@Override
public void onDisable() {
    RarityBoostRegistry.getInstance().unregister("MyPlugin");
}
```

Your plugin should declare `depend` or `softdepend` on `EvenMoreFish` in its `plugin.yml`
so the api classes are available when it loads.
