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
            ≈ 0.9259

Percentage = 0.9259 × 100
           ≈ 92.59%
```

#### Epic

```text
Probability = Weight of Epic / Total Weight
            = 3 / 108
            ≈ 0.0278

Percentage = 0.0278 × 100
           ≈ 2.78%
```

#### Junk

```text
Probability = Weight of Junk / Total Weight
            = 5 / 108
            ≈ 0.0463

Percentage = 0.0463 × 100
           ≈ 4.63%
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
