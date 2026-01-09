package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.commands.CommandUtils;
import com.oheers.fish.permissions.AdminPerms;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("UnstableApiUsage")
public class DatabaseSubcommand {

    private final String name;

    public DatabaseSubcommand(@NonNull String name) {
        this.name = name;
    }

    public @NotNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(name)
            .requires(source -> source.getSender().hasPermission(AdminPerms.DATABASE))
            .then(dropFlyway())
            .then(repairFlyway())
            .then(cleanFlyway())
            .then(migrateToLatest())
            .then(help());
    }

    private @NotNull ArgumentBuilder<CommandSourceStack, ?> dropFlyway() {
        return Commands.literal("drop-flyway")
            .requires(source -> source.getSender().hasPermission(AdminPerms.DATABASE_FLYWAY))
            .executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                if (CommandUtils.isLogDbError(sender)) {
                    return 1;
                }
                EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().dropFlywaySchemaHistory();
                sender.sendMessage("Dropped flyway schema history.");
                return 1;
            });
    }

    private @NotNull ArgumentBuilder<CommandSourceStack, ?> repairFlyway() {
        return Commands.literal("repair-flyway")
            .requires(source -> source.getSender().hasPermission(AdminPerms.DATABASE_FLYWAY))
            .executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                if (CommandUtils.isLogDbError(sender)) {
                    return 1;
                }
                sender.sendMessage("Attempting to repair the migrations, check the logs.");
                EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().repairFlyway();
                return 1;
            });
    }

    private @NotNull ArgumentBuilder<CommandSourceStack, ?> cleanFlyway() {
        return Commands.literal("clean-flyway")
            .requires(source -> source.getSender().hasPermission(AdminPerms.DATABASE_CLEAN))
            .executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                if (CommandUtils.isLogDbError(sender)) {
                    return 1;
                }
                sender.sendMessage("Attempting to clean flyway, check the logs.");
                EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().cleanFlyway();
                return 1;
            });
    }

    private @NotNull ArgumentBuilder<CommandSourceStack, ?> migrateToLatest() {
        return Commands.literal("migrate-to-latest")
            .requires(source -> source.getSender().hasPermission(AdminPerms.DATABASE_MIGRATE))
            .executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                if (CommandUtils.isLogDbError(sender)) {
                    return 1;
                }
                EvenMoreFish.getInstance().getPluginDataManager().getDatabase().migrateFromDatabaseVersionToLatest();
                return 1;
            });
    }

    private @NotNull ArgumentBuilder<CommandSourceStack, ?> help() {
        return Commands.literal("help")
            .executes(ctx -> {
                ctx.getSource().getSender().sendPlainMessage(
                    "Available Commands: migrate-to-latest, clean-flyway, repair-flyway, drop-flyway"
                );
                return 1;
            });
    }

}
