---
title: Baits
---

## The files
As of EvenMoreFish 2.0, all bait configs are located in `plugins/EvenMoreFish/baits`.

![Epic Elixir](./assets/bait-epic-elixir-example.webp)

## Creating Baits
To create a new bait, you need to create a new yml file in the baits folder.

The following configs are required in each bait config file:
- `id` - Allows the plugin to identify this bait.

All other configs are optional, however you will most likely want to add fish and rarities to your bait. You can see how to do this in the example file.


## Disabling Baits
To disable a bait, you have two choices:
- Set `disabled` to true inside the file and reload.
- Rename the file to start with an underscore.

Doing either of these will prevent the bait from being registered into the plugin.

## Example Config
An example config will always be available inside your baits folder, and contains every possible config option.
This file will reset every time the plugin loads, meaning it will always be up to date.

You can view this example file [here](https://github.com/EvenMoreFish/EvenMoreFish/blob/master/even-more-fish-plugin/src/main/resources/baits/_example.yml)