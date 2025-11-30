package com.connectasistemas.framework.utils;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/**
 * Singletoon para iniciar a conexão com o banco
 */
public final class DatabaseManager {
    private static Jdbi jdbi;

    public static Jdbi get() {
        if (jdbi == null) {
            jdbi = Jdbi.create("jdbc:sqlite:app.db");
            jdbi.installPlugin(new SqlObjectPlugin());
        }

        return jdbi;
    }
}
