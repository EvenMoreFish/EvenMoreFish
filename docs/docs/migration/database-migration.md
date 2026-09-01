---
title: Database Migration
---

:::info[Permission: `emf.admin.debug.database`]
:::

# Database Migration
EMF uses flyway to automatically migrate the database. Normally you shouldn't need to manually migrate the database.
In case things breaks you can use some commands to try and fix the issues.

`/emf admin database reset` is intentionally protected by a confirmation step. Run it once to arm it, then run the same command again within 30 seconds to actually clear EMF data tables.

:::info[Permission `emf.admin.debug.database.flyway`] 
:::

| Command                                 | Description                                            | Permission                         |
|-----------------------------------------|--------------------------------------------------------|------------------------------------|
| `/emf admin database drop-flyway`       | Drops the Flyway schema history table                  | `emf.admin.debug.database.flyway`  |
| `/emf admin database repair-flyway`     | Runs the Flyway repair command                         | `emf.admin.debug.database.flyway`  |
| `/emf admin database clean-flyway`      | Runs the Flyway clean command                          | `emf.admin.debug.database.flyway`  |
| `/emf admin database migrate-to-latest` | Attempts to migrate the database to the latest version | `emf.admin.debug.database.migrate` |
| `/emf admin database reset`             | Clears EMF data tables after a 30-second confirmation  | `emf.admin.debug.database.reset`   |

:::danger[Dangerous command]

`/emf admin database reset` removes EMF-managed database rows and resets identities. It does not use Flyway `clean`, and it should only be used when you explicitly want to wipe stored EMF data.

:::

## Migrating from Database V2

:::tip[Not sure what database version you have?]

Try running `/emf admin version`

:::

Running the `/emf admin migrate` will attempt to migrate from database version 2 to the latest version.
