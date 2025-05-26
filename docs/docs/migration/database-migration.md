---
title: Database Migration
---

:::info[Permission: `emf.admin.debug.database`]
:::

# Database Migration
EMF uses flyway to automatically migrate the database. Normally you shouldn't need to manually migrate the database.
In case things breaks you can use some commands to try and fix the issues.

:::info[Permission `emf.admin.debug.database.flyway`] 
:::

| Command                                 | Description                                            | Permission                         |
|-----------------------------------------|--------------------------------------------------------|------------------------------------|
| `/emf admin database drop-flyway`       | Drops the Flyway schema history table                  | `emf.admin.debug.database.flyway`  |
| `/emf admin database repair-flyway`     | Runs the Flyway repair command                         | `emf.admin.debug.database.flyway`  |
| `/emf admin database clean-flyway`      | Runs the Flyway clean command                          | `emf.admin.debug.database.flyway`  |
| `/emf admin database migrate-to-latest` | Attempts to migrate the database to the latest version | `emf.admin.debug.database.migrate` |

## Migrating from Database V2

:::tip[Not sure what database version you have?]

Try running `/emf admin version`

:::

Running the `/emf admin migrate` will attempt to migrate from database version 2 to the latest version.