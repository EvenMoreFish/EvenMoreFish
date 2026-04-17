package com.oheers.fish.database.sql;

public class PostgresDatabaseSqlDialect implements DatabaseSqlDialect {

    @Override
    public String userFishStatsUpsert(String tableName) {
        return "insert into " + tableName + " (user_id, fish_name, fish_rarity, first_catch_time, shortest_length, longest_length, quantity) " +
                "values (:user_id, :fish_name, :fish_rarity, :first_catch_time, :shortest_length, :longest_length, :quantity) " +
                "on conflict(fish_name, fish_rarity, user_id) do update set " +
                "shortest_length = excluded.shortest_length, longest_length = excluded.longest_length, quantity = excluded.quantity";
    }

    @Override
    public String fishStatsUpsert(String tableName) {
        return "insert into " + tableName + " (fish_name, fish_rarity, first_fisher, discoverer, total_caught, largest_fish, largest_fisher, shortest_length, shortest_fisher, first_catch_time) " +
                "values (:fish_name, :fish_rarity, :first_fisher, :discoverer, :total_caught, :largest_fish, :largest_fisher, :shortest_length, :shortest_fisher, :first_catch_time) " +
                "on conflict(fish_name, fish_rarity) do update set " +
                "total_caught = excluded.total_caught, " +
                "largest_fish = case when " + tableName + ".largest_fish < excluded.largest_fish then excluded.largest_fish else " + tableName + ".largest_fish end, " +
                "largest_fisher = case when " + tableName + ".largest_fish < excluded.largest_fish then excluded.largest_fisher else " + tableName + ".largest_fisher end, " +
                "shortest_length = case when " + tableName + ".shortest_length > excluded.shortest_length or " + tableName + ".shortest_length is null then excluded.shortest_length else " + tableName + ".shortest_length end, " +
                "shortest_fisher = case when " + tableName + ".shortest_length > excluded.shortest_length or " + tableName + ".shortest_length is null then excluded.shortest_fisher else " + tableName + ".shortest_fisher end";
    }
}
