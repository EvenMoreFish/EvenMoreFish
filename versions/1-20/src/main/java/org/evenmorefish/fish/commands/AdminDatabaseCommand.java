package org.evenmorefish.fish.commands;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.utils.Scheduling;
import com.oheers.fish.commands.CommandUtils;
import dev.jorel.commandapi.CommandAPICommand;

import java.util.List;

public class AdminDatabaseCommand extends CommandAPICommand {
    public AdminDatabaseCommand() {
        super("database");

        withPermission("emf.admin.debug.database");
        setSubcommands(List.of(
                dropFlywayCommand(),
                repairFlywayCommand(),
                cleanFlywayCommand(),
                migrateToLatest()
        ));
    }

    public CommandAPICommand dropFlywayCommand() {
        return new CommandAPICommand("drop-flyway")
                .withPermission("emf.admin.debug.database.flyway")
                .withShortDescription("Drops the flyway schema history, useful for when the database breaks")
                .executes((commandSender, commandArguments) -> {
                            if (CommandUtils.isLogDbError(commandSender)) {
                                return;
                            }
                            EvenMoreFish.getInstance().getPluginDataManager().getDatabaseWorker()
                                .write(() -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().dropFlywaySchemaHistory())
                                .thenRun(() -> Scheduling.getInstance().runTask(() -> commandSender.sendMessage("Dropped flyway schema history.")));
                        }
                );
    }

    public CommandAPICommand repairFlywayCommand() {
        return new CommandAPICommand("repair-flyway")
                .withPermission("emf.admin.debug.database.flyway")
                .withShortDescription("Attempt to repair the database")
                .executes((commandSender, commandArguments) -> {
                    if (CommandUtils.isLogDbError(commandSender)) {
                        return;
                    }
                    commandSender.sendMessage("Attempting to repair the migrations, check the logs.");
                    EvenMoreFish.getInstance().getPluginDataManager().getDatabaseWorker()
                        .write(() -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().repairFlyway());
                });
    }

    public CommandAPICommand cleanFlywayCommand() {
        return new CommandAPICommand("clean-flyway")
                .withShortDescription("Attempt to clean the database")
                .withPermission("emf.admin.debug.database.clean")
                .executes((commandSender, commandArguments) -> {
                    if (CommandUtils.isLogDbError(commandSender)) {
                        return;
                    }
                    commandSender.sendMessage("Attempting to clean flyway, check the logs.");
                    EvenMoreFish.getInstance().getPluginDataManager().getDatabaseWorker()
                        .write(() -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().cleanFlyway());
                });
    }

    public CommandAPICommand migrateToLatest() {
        return new CommandAPICommand("migrate-to-latest")
                .withShortDescription("Migrate to the latest DB version.")
                .withPermission("emf.admin.debug.database.migrate")
                .executes((commandSender, commandArguments) -> {
                    if (CommandUtils.isLogDbError(commandSender)) {
                        return;
                    }
                    EvenMoreFish.getInstance().getPluginDataManager().getDatabaseWorker()
                        .write(() -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().migrateFromDatabaseVersionToLatest());
                });
    }


}
