---
title: Database
description: Configuring your database
version: "2.0.0"
---
:::info[Supported Types]

MySQL, SQLite

:::

# Database Configuration

:::tip[The database is not enabled by default, to enable set:]

```yaml enabled
database:
  // highlight-next-line
  enabled: true
```

:::
## Enabled `enabled`
```yaml enabled
database:
  // highlight-next-line
  enabled: false
```
Controls whether the database system is active.

---

## Type `type`
```yaml type
database:
  // highlight-next-line
  type: sqlite
```
Available options: `mysql`, `sqlite`  
Determines the database engine to use. SQLite is file-based (no server required), while MySQL requires a separate database server.

---

## Address `address`

- Default port `3306` is automatically appended for MySQL connections
- For custom ports, use `hostname:port` format
- Special values:
    - `localhost` resolves to `127.0.0.1`

```yaml address
database:
  // highlight-next-line
  address: localhost
```
---

## Database Name `database`

The name of the database where all plugin data will be stored. For MySQL, this database must exist before connection.

```yaml database
database:
  // highlight-next-line
  database: evenmorefish
```

---

### Table Prefix `table-prefix`
```yaml table-prefix
database:
  // highlight-next-line
  table-prefix: emf_
```

:::warning

- Changing this after initial setup will create new tables
- Only modify during first installation or planned migrations
- Affects all database tables: `emf_users`, `emf_competitions`, etc.

:::

---

### Credentials (`username` & `password`)

MySQL authentication credentials. For security:
- Avoid using `root` in production
- Create a dedicated database user with limited privileges
- For SQLite, these values are ignored
- Preferably use secrets if in environment that allows it

```yaml username & password
database:
  // highlight-start
  username: root
  password: ''
  // highlight-end
```

---

### Advanced: Save Interval `advanced.save-interval`
Controls how often data is saved to the database:
- `unit`: Time unit (SECONDS/MINUTES/HOURS)
- `user-fish-stats`: Player catch statistics save interval
- `competition`: Competition data save interval

Lower values = more frequent saves (higher server load)  
Higher values = risk of data loss during crashes


```yaml advanced.save-interval
database:
  advanced:
    // highlight-start
    save-interval:
      unit: SECONDS
      user-fish-stats: 5
      competition: 5
    // highlight-end
```

--- 


