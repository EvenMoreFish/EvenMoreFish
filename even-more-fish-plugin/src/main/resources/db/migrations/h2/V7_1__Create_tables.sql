CREATE SCHEMA IF NOT EXISTS `{db.name}`;
SET SCHEMA `{db.name}`;

CREATE TABLE IF NOT EXISTS `${table.prefix}competitions` (
   id INTEGER NOT NULL AUTO_INCREMENT, -- H2 uses AUTO_INCREMENT for compatibility
   competition_name VARCHAR(256) NOT NULL,
   winner_uuid VARCHAR(128) NOT NULL,
   winner_fish VARCHAR(256) NOT NULL,
   winner_score REAL NOT NULL,
   contestants TEXT NOT NULL,
   PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS `${table.prefix}fish` (
   fish_name VARCHAR(100) NOT NULL,
   fish_rarity VARCHAR(100) NOT NULL,
   first_fisher VARCHAR(36) NOT NULL,
   total_caught INTEGER NOT NULL,
   largest_fish REAL NOT NULL,
   largest_fisher VARCHAR(36) NOT NULL,
   first_catch_time TIMESTAMP NOT NULL,
   PRIMARY KEY (fish_name)
);

CREATE TABLE IF NOT EXISTS `${table.prefix}users` (
   id INTEGER NOT NULL AUTO_INCREMENT, -- user_id, H2 uses AUTO_INCREMENT for compatibility
   uuid VARCHAR(128) NOT NULL,
   first_fish VARCHAR(256) NOT NULL,
   last_fish VARCHAR(256) NOT NULL,
   largest_fish VARCHAR(256) NOT NULL,
   largest_length REAL NOT NULL,
   num_fish_caught INTEGER NOT NULL,
   total_fish_length REAL NOT NULL,
   competitions_won INTEGER NOT NULL,
   competitions_joined INTEGER NOT NULL,
   fish_sold INTEGER DEFAULT 0,
   money_earned DOUBLE DEFAULT 0,
   PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS `${table.prefix}fish_log` (
   id INTEGER NOT NULL AUTO_INCREMENT, -- H2 uses AUTO_INCREMENT for compatibility
   user_id INTEGER NOT NULL, -- user_id
   rarity VARCHAR(128) NOT NULL,
   fish VARCHAR(128) NOT NULL,
   quantity INTEGER NOT NULL,
   first_catch_time TIMESTAMP NOT NULL,
   largest_length REAL NOT NULL,
   CONSTRAINT FK_FishLog_User
   -- [jooq ignore start]
   FOREIGN KEY (user_id) REFERENCES `${table.prefix}users`(id),
   -- [jooq ignore stop]
   PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS `${table.prefix}transactions` (
  id VARCHAR(22) NOT NULL,
  user_id INTEGER NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  CONSTRAINT FK_Transactions_User
  -- [jooq ignore start]
  FOREIGN KEY (user_id) REFERENCES `${table.prefix}users`(id),
  -- [jooq ignore stop]
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS `${table.prefix}users_sales` (
  id INTEGER NOT NULL AUTO_INCREMENT, -- H2 uses AUTO_INCREMENT for compatibility
  transaction_id VARCHAR(22) NOT NULL,
  fish_name VARCHAR(256) NOT NULL,
  fish_rarity VARCHAR(256) NOT NULL,
  fish_amount INTEGER NOT NULL,
  fish_length DOUBLE NOT NULL,
  price_sold DOUBLE NOT NULL,
  CONSTRAINT FK_UsersSales_Transaction
  -- [jooq ignore start]
  FOREIGN KEY (transaction_id) REFERENCES `${table.prefix}transactions`(id),
  -- [jooq ignore stop]
  PRIMARY KEY (id)
);
