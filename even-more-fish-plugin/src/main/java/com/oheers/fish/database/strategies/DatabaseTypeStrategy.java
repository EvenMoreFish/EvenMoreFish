package com.oheers.fish.database.strategies;

import org.flywaydb.core.api.configuration.FluentConfiguration;

public interface DatabaseTypeStrategy {
    default FluentConfiguration configureFlyway(FluentConfiguration flywayConfig) {
        return flywayConfig;
    }

}
