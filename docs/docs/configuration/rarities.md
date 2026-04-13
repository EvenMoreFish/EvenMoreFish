---
title: Rarities
---

## The files
As of EvenMoreFish 2.0, all rarity configs are located in `plugins/EvenMoreFish/rarities`.

## Creating Rarities
To create a new rarity, you need to create a new yml file in the rarities folder.

The following configs are required in each rarity config file:
- `id` - Allows the plugin to identify this rarity.

All other configs are optional, however you will most likely want to add fish to your rarity. You can see how to do this in the example file.

## Disabling Rarities
To disable a rarity, you have two choices:
- Set `disabled` to true inside the file and reload.
- Rename the file to start with an underscore.

Doing either of these will prevent the rarity from being registered into the plugin.

## Example Config
An example config will always be available inside your rarities folder, and contains every possible config option.
This file will reset every time the plugin loads, meaning it will always be up to date.

You can view this example file [here](https://github.com/EvenMoreFish/EvenMoreFish/blob/master/even-more-fish-plugin/src/main/resources/rarities/_example.yml)

## Configuring a Rarity for Vanilla fish.
It is possible to configure a rarity for vanilla fish if you do not wish to use the toggle command.

It is recommended to use a combination of `raw-material` and `silent` configs if you wish to keep the vanilla experience, however this is optional.

Below is a fully configured rarity file for vanilla fish. You can simply copy and paste this into a .yml file, change the weight as you wish, and it will work.

```yaml
id: Vanilla
disabled: false
# You likely want to change this.
weight: 100
item:
  material: air
format: ""
worth-multiplier: 0.0
broadcast:
  enabled: false
  only-rods: false
  range: -1
catch-type: catch
size:
  minSize: -1
  maxSize: -1

fish:
  Cod:
    item:
      raw-material: cod
    silent: true
  Salmon:
    item:
      raw-material: salmon
    silent: true
  Pufferfish:
    item:
      raw-material: pufferfish
    silent: true
  Tropical Fish:
    item:
      raw-material: tropical_fish
    silent: true
```