---
title: Weight System
description: Explaining the weight system
---
# Weight System Explanation

This system uses weights to define the importance or likelihood of an object being selected. The weights are translated into percentages to reflect the probability of each object being chosen.

---

## How It Works

1. **Weights are assigned to each object** based on their importance or likelihood of being selected.
2. **The total weight** is calculated by adding up all the individual weights.
3. **The probability** of each object being selected is calculated by dividing its weight by the total weight.
4. **The probability is converted to a percentage** by multiplying by 100.

---

## Example with Three Items

### Given Weights:

- **Common**: Weight = 100
- **Epic**: Weight = 3
- **Junk**: Weight = 5

### Step 1: Calculate the Total Weight
```text
Total Weight = Weight of Common + Weight of Epic + Weight of Junk
             = 100 + 3 + 5
             = 108
```

---

### Step 2: Calculate the Probability for Each Item

#### Common

```text
Probability = Weight of Common / Total Weight
            = 100 / 108
            ~= 0.9259

Percentage = 0.9259 x 100
           ~= 92.59%
```

#### Epic

```text
Probability = Weight of Epic / Total Weight
            = 3 / 108
            ~= 0.0278

Percentage = 0.0278 x 100
           ~= 2.78%
```

#### Junk

```text
Probability = Weight of Junk / Total Weight
            = 5 / 108
            ~= 0.0463

Percentage = 0.0463 x 100
           ~= 4.63%
```

---

### Final Chances

- **Common**: ~92.59% chance of being selected.
- **Epic**: ~2.78% chance of being selected.
- **Junk**: ~4.63% chance of being selected.

---

## Summary

- The **Common** item is the most likely to be selected due to its high weight.
- The **Epic** item has a very low chance of being selected.
- The **Junk** item has a slightly higher chance than the **Epic** item but is still much less likely than the **Common** item.

This system ensures that objects with higher weights are prioritized in the selection process.

---

## Baits and Modified Weights

Baits modify the selection chances of specific items by changing their weights. A bait can add, subtract, multiply, or divide the base weight of affected rarities and fish.

---

### How It Works

1. **Each item has a base weight**, which defines its normal chance of being selected.
2. **Baits can target whole rarities or specific fish**.
3. **Each configured modifier** is applied to the base weight to produce the effective weight.
4. **Items without a modifier** retain their original base weight.

---

### Effective Weight Calculation

The effective weight of an item is determined using the following logic:

```text
+N  -> baseWeight + N
-N  -> baseWeight - N
*N  -> baseWeight * N
/N  -> baseWeight / N
```

Bare numbers are treated the same as `+N`.

* If the item's base weight is `0` or less, it will not be considered.
* Results are clamped to `0`, so negative outcomes never produce a negative effective weight.

---

### Example

Suppose you have the following bait configuration:

```yaml
rarity-modifiers:
  Epic: "*2"

fish-modifiers:
  Rare:
    Nemo: "+15"
```

And the base weights are:

* **Common**: 100
* **Epic**: 3
* **Junk**: 5

The **effective weights** would be:

* **Common**: 100
* **Epic**: 3 x 2 = 6
* **Junk**: 5

### Recalculated Total Weight

```text
Total Weight = 100 (Common) + 6 (Epic) + 5 (Junk)
             = 111
```

### New Selection Chances

* **Common**: 100 / 111 ~= 90.09%
* **Epic**: 6 / 111 ~= 5.41%
* **Junk**: 5 / 111 ~= 4.50%

---

### Summary

* Baits dynamically shift the probability distribution by changing the weight of specific rarities and fish.
* This system allows both promotion and reduction without altering the base configuration.
* Bait configs from before `2.3.0` are automatically migrated to the modifier-based format using `bait.boost`.

---
