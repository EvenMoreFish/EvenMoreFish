# 🐠 EvenMoreFish Paper Plugin 🐟

Improve your server's fishing experience with an incredible **fishing competition plugin** based on elsiff's MoreFish
plugin

With over 60 custom fish in the default configurations, and the ability to add your own, this is the best competition
plugin for your server.

---

Supports 1.20.1+

If you encounter any issues with the plugin, please do the following before reporting the problem:
- Create a Paper test server using one of the supported versions.
- Add the latest [dev build](https://ci.codemc.io/job/EvenMoreFish/job/EvenMoreFish/) to the server.
- Test your issue.
- If your issue is still happening, open a GitHub issue or send a message in the Discord server.

---

## Sponsors
![YourKit Logo](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.

YourKit is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/), [YourKit .NET Profiler](https://www.yourkit.com/dotnet-profiler/), and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).

---

## 📦 Download 📦

There are three sources for the plugin:
- [Modrinth](https://modrinth.com/plugin/evenmorefish)
- [GitHub Releases](https://github.com/EvenMoreFish/EvenMoreFish/releases)
- [Jenkins](https://ci.codemc.io/job/EvenMoreFish/job/EvenMoreFish/) (Experimental)

---

## ⭐ Features ⭐

* ### Custom item support

Use any in-game item, or even a base-64 head in place of a fish and with the option to give the player effects, it makes
this plugin highly customizable for use in your own server.

* ### Rarities

There's 4 rarities in the default config, but it's possible to add your own in the `rarities` folder, with its own
colour, drop frequency, standard fish length. It's also possible to set per-fish lengths.

* ### Baits

Boost the chances players have of catching certain fish or rarities with "baits", you can choose these to be disabled
during competitions to give equal fairness to new players and existing players. With the /emf admin bait command you can
give baits to players using the **-p:** variable.

* ### Competitions

Competitions are scheduled, or staff-triggered events where players compete to get the biggest, fattest fish. It's
recommended to change the rewards to your liking, and multiple reward types can be given (Command, Message, Money,
Effect, Item, [and more](https://evenmorefish.github.io/EvenMoreFish/docs/configuration/reward-types)). A customizable bossbar also exists, and kindly vanishes once the competition is up. The permission node
for being able to start competitions via /emf admin competition start is: `emf.admin` (defaulting to op)

* ### Shop

Each fish rarity has its own shop multiplier value. This is multiplied by the fish's length to get the amount of money
the fish will sell for in the clean /emf shop, providing another source of income for your players. Items placed into
the shop are protected, and will drop to the player's feet in the event of the inventory being closed, or a server
shutdown.

* ### Configuration

This plugin is *highly* customisable, pretty much everything can be modified to your liking, including but not limited
to the messages (`messages.yml`). There is a detailed explanation for each line in the config to guide you through the
process.

--- 

## ⚙ Contributing ⚙

Contributions of any size will be massively appreciated, and help support the plugin.

### Translating

To contribute to translations, please visit our Crowdin page and suggest changes over there.

If Crowdin doesn't have the language you want, feel free to request it, and we will add it as soon as we can.

Cheers, Oheers 

