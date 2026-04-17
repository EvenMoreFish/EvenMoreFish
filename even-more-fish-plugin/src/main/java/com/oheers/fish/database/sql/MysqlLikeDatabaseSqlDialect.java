package com.oheers.fish.database.sql;

public class MysqlLikeDatabaseSqlDialect implements DatabaseSqlDialect {

    @Override
    public String userFishStatsUpsert(String tableName) {
        return "insert into " + tableName + " (user_id, fish_name, fish_rarity, first_catch_time, shortest_length, longest_length, quantity) " +
                "values (:user_id, :fish_name, :fish_rarity, :first_catch_time, :shortest_length, :longest_length, :quantity) " +
                "on duplicate key update shortest_length = values(shortest_length), longest_length = values(longest_length), quantity = values(quantity)";
    }

    @Override
    public String fishStatsUpsert(String tableName) {
        return "insert into " + tableName + " (fish_name, fish_rarity, first_fisher, discoverer, total_caught, largest_fish, largest_fisher, shortest_length, shortest_fisher, first_catch_time) " +
                "values (:fish_name, :fish_rarity, :first_fisher, :discoverer, :total_caught, :largest_fish, :largest_fisher, :shortest_length, :shortest_fisher, :first_catch_time) " +
                "on duplicate key update total_caught = values(total_caught), " +
                "largest_fish = case when largest_fish < values(largest_fish) then values(largest_fish) else largest_fish end, " +
                "largest_fisher = case when largest_fish < values(largest_fish) then values(largest_fisher) else largest_fisher end, " +
                "shortest_length = case when shortest_length > values(shortest_length) or shortest_length is null then values(shortest_length) else shortest_length end, " +
                "shortest_fisher = case when shortest_length > values(shortest_length) or shortest_length is null then values(shortest_fisher) else shortest_fisher end";
    }
}
