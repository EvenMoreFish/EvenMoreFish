---
title: Item Configs
---

## Introduction
EvenMoreFish has a variety of item configs that can be set and changed to customize how items behave. 

This page will cover every item config available in EvenMoreFish (as of 2.1.5), and how to use them.

### Setting the base item
To set the base item, you can use one of the following settings:
- `material` - This takes a Material name as shown on [this page](https://jd.papermc.io/paper/1.21.10/org/bukkit/Material.html).
- `materials` - This takes a list of Material names, and will randomly choose one when the item is created.
- `raw-material` - This acts the same as `material`, but none of the plugin's custom data is set on the item.
- `raw-nbt` - This takes the raw NBT data for an item, which can be obtained via `/emf admin raw-nbt`. None of the plugin's custom data is set on the item.
- `headdb` - This takes a HeadDatabase ID, and will be a custom head from that plugin.
- `multiple-headdb` - This takes a list of HeadDatabase IDs, and will randomly choose one when the item is created.
- `head-64` - This takes a base64 encoded string for a custom head texture.
- `multiple-head-64` - This takes a list of base64 encoded strings, and will randomly choose one when the item is created.
- `head-uuid` - This takes a player's UUID, and will use that player's head as the item.
- `multiple-head-uuid` - This takes a list of player UUIDs, and will randomly choose one when the item is created.
- `own-head` - This will use the head of the player who owns the item, if available.

If none of these are present, the item defaults to raw cod.

### CustomModelData
CustomModelData is applied using the `custom-model-data` setting.

Example:
```yaml
item:
    custom-model-data: 123456
```

### Display Name
The display name is applied using the `displayname` setting. 

This uses [MiniMessage](https://docs.papermc.io/adventure/minimessage/) tags for formatting.

Example:
```yaml
item:
  displayname: "<light_purple>My Display Name"
```

### Lore
The lore is applied using the `lore` setting.

This uses [MiniMessage](https://docs.papermc.io/adventure/minimessage/) tags for formatting.

Example:
```yaml
item:
  lore:
    - "<gray>This is the first line of lore."
    - "<gold>This is the second line of lore."
```

### Dye Colour
If the item is dyeable, you can set the dye colour using the `dye-colour` setting.

This requires a valid hex colour code (e.g. `#FF5733`).

Example:
```yaml
item:
  dye-colour: "#FF5733"
```

### Enchantments
You can add enchantments to the item using the `enchantments` setting.

This takes a list of enchantments in the format `NAME,LEVEL`, so for example:
```yaml
item:
  enchantments:
    - UNBREAKING,3
    - LURE,2
```

On 1.21 servers and above, you can also use custom enchantments by using their key:
```yaml
item:
  enchantments:
    - veinminer-enchantment:veinminer,1
```

### Glowing 
The glowing effect looks like an enchantment glow. 

On 1.21+ servers, this is done using the built-in data component. On older servers, this is done by adding and hiding an unbreaking enchantment.

This can be set using the `glowing` setting.`

Example:
```yaml
item:
  glowing: true
```

### Item Damage
You can set the item damage (durability) using the `durability` setting.

This is a percentage of the item's maximum durability.

Example:
```yaml
item:
  # Sets the item to 25% durability
  durability: 25
```

### Potion
If the item is a potion, you can customize it using the `potion` setting.

This requires a valid potion effect in the format `NAME,AMPLIFIER,DURATION`.

Example:
```yaml
item:
  # Poison 1 for 15 seconds.
  potion: POISON,1,15
```

### Quantity

You can set the quantity of the item using the `quantity` setting.

Example:
```yaml
item:
  quantity: 5
```

### Unbreakable

You can make the item unbreakable using the `unbreakable` setting.

Example:
```yaml
item:
  unbreakable: true
```

### Item Model
On 1.21.3 and above, you can set a custom item model using the `item-model` setting.

This requires a valid item model key, or it will be a broken texture.

Example:
```yaml
item:
  item-model: my:key
```

### Fire Resistant
On 1.21 and above, you can make the item resistant to fire and lava using the `fire-resistant` config.

Example:
```yaml
item:
  fire-resistant: true
```

### Hide Tooltip
On 1.21 and above, you can hide the item's tooltip using the `hide-tooltip` config.

This is particularly useful for GUI items.

Example:
```yaml
item:
  hide-tooltip: true
```

### Item Rarity
On 1.21 and above, you can set the item's vanilla rarity using the `item-rarity` config.

Example:
```yaml
item:
  item-rarity: RARE
```

### Tooltip Style
On 1.21.3 and above, you can set a custom tooltip style using the `tooltip-style` setting.

This requires a valid style key, or it will be a broken texture.

Example:
```yaml
item:
  tooltip-style: my:key
```