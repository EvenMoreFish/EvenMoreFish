package com.oheers.fish.permissions;

public class AdminPerms {
    private AdminPerms() {
        throw new UnsupportedOperationException();
    }

    public static final String ADMIN = "emf.admin";
    public static final String UPDATE_NOTIFY = "emf.admin.update.notify";
    public static final String MIGRATE = "emf.admin.migrate";

    public static final String DATABASE = "emf.admin.debug.database";
    public static final String DATABASE_CLEAN = "emf.admin.debug.database.clean";
    public static final String DATABASE_FLYWAY = "emf.admin.debug.database.flyway";
    public static final String DATABASE_MIGRATE = "emf.admin.debug.database.migrate";
}
