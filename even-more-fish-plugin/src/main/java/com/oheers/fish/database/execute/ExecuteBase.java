package com.oheers.fish.database.execute;

import com.oheers.fish.database.connection.ConnectionFactory;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class ExecuteBase {
    private final ConnectionFactory connectionFactory;
    private final Settings settings;

    protected ExecuteBase(final ConnectionFactory connectionFactory, final Settings settings) {
        this.connectionFactory = connectionFactory;
        this.settings = settings;
    }

    protected @NotNull DSLContext getContext(Connection connection) {
        if (settings == null) {
            return DSL.using(connection, connectionFactory.getSQLDialect(connection));
        }
        return DSL.using(connection, connectionFactory.getSQLDialect(connection), settings);
    }

    protected Connection getConnection() throws SQLException {
        return connectionFactory.getConnection();
    }

    protected boolean supportsTransactions() {
        return this.connectionFactory.supportsTransactions();
    }
}

