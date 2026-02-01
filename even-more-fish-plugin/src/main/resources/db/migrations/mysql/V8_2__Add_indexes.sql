CREATE INDEX idx_fish_log_user_fish
ON `${table.prefix}fish_log` (user_id, fish_name, fish_rarity);

CREATE INDEX idx_user_fish_stats_user_fish
ON `${table.prefix}user_fish_stats` (user_id, fish_name, fish_rarity);