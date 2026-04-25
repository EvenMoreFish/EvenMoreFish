---
title: Commands Reference
sidebar_position: 4
---

## Aliases

The main command (`/emf`) can have custom aliases configured in `MainConfig`.

## EvenMoreFish Player Commands

| Command              | Arguments  | Permission                             | Description                            |
|----------------------|------------|----------------------------------------|----------------------------------------|
| `/emf`               | None       | None                                   | Shows help message (default)           |
| `/emf next`          | None       | `emf.next`                             | Shows when the next competition starts |
| `/emf toggle`        | None       | `emf.toggle`                           | Toggles fishing rewards on/off         |
| `/emf gui`           | None       | `emf.gui`                              | Opens the main menu GUI                |
| `/emf help`          | None       | `emf.help`                             | Shows command help                     |
| `/emf top`           | None       | `emf.top`                              | Shows competition leaderboard          |
| `/emf shop`          | `[target]` | `emf.shop` (or `emf.admin` for target) | Opens fish selling GUI                 |
| `/emf sellall`       | None       | `emf.sellall`                          | Sells all fish in inventory            |
| `/emf applybaits`    | None       | `emf.applybaits`                       | Opens bait application GUI             |
| `/emf journal`       | `[rarity]` | `emf.journal`                          | Opens fish collection journal          |



### EvenMoreFish Admin Commands

| Command                                 | Arguments                           | Permission                         | Description                           |
|-----------------------------------------|-------------------------------------|------------------------------------|---------------------------------------|
| `/emf admin`                            | None                                | `emf.admin`                        | Shows admin command help              |
| `/emf admin fish`                       | `<rarity> <fish> [amount] [target]` | `emf.admin`                        | Gives a specific fish to a player     |
| `/emf admin list fish`                  | `<rarity>`                          | `emf.admin`                        | Lists fish in a rarity                |
| `/emf admin list rarities`              | None                                | `emf.admin`                        | Lists all rarities                    |
| `/emf admin list requirementTypes`      | None                                | `emf.admin`                        | Lists requirement types               |
| `/emf admin list rewardTypes`           | None                                | `emf.admin`                        | Lists reward types                    |
| `/emf admin list itemAddons`            | None                                | `emf.admin`                        | Lists item addons                     |
| `/emf admin nbt-rod`                    | `[target]`                          | `emf.admin`                        | Gives NBT fishing rod                 |
| `/emf admin bait`                       | `<bait> [quantity] [target]`        | `emf.admin`                        | Gives bait to player                  |
| `/emf admin bait debug`                 | `<bait>`                            | `emf.admin`                        | Shows resolved bait chances for you   |
| `/emf admin bait debug`                 | `<bait> <target>`                   | `emf.admin`                        | Shows resolved bait chances for target |
| `/emf admin clearbaits`                 | `[target]`                          | `emf.admin`                        | Clears baits from held rod            |
| `/emf admin reload`                     | None                                | `emf.admin`                        | Reloads plugin config                 |
| `/emf admin version`                    | None                                | `emf.admin`                        | Shows plugin version info             |
| `/emf admin rewardtypes`                | None                                | `emf.admin`                        | Lists reward types with authors       |
| `/emf admin migrate`                    | None                                | `emf.admin`                        | Runs database migrations              |
| `/emf admin rawItem`                    | None                                | `emf.admin`                        | Gets NBT of held item                 |
| `/emf admin help`                       | None                                | `emf.admin`                        | Shows help again                      |
| `/emf admin competition start`          | `<competitionId> [duration]`        | `emf.admin`                        | Starts competition                    |
| `/emf admin competition end`            | None                                | `emf.admin`                        | Ends current competition              |
| `/emf admin competition test`           | `[duration] [type]`                 | `emf.admin`                        | Starts test competition               |
| `/emf admin database drop-flyway`       | None                                | `emf.admin.debug.database.flyway`  | Drops Flyway schema history (debug)   |
| `/emf admin database repair-flyway`     | None                                | `emf.admin.debug.database.flyway`  | Attempts to repair migrations         |
| `/emf admin database clean-flyway`      | None                                | `emf.admin.debug.database.clean`   | Cleans Flyway tables                  |
| `/emf admin database migrate-to-latest` | None                                | `emf.admin.debug.database.migrate` | Forces migration to latest DB version |

