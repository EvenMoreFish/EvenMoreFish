ALTER TABLE `${table.prefix}competitions` ADD COLUMN report_id TEXT;

CREATE UNIQUE INDEX `${table.prefix}competition_report_id_idx`
ON `${table.prefix}competitions` (report_id);
