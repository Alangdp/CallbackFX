package com.connectasistemas.framework.utils.migrations;

import com.connectasistemas.framework.interfaces.Migration;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

/**
 * Executa as migrações disponíveis
 */
public class MigrationRunner {
    public static void run(Jdbi jdbi) {
        List<Migration> list = List.of(
                new GenericMigration()
        );
        list.forEach(m -> m.up(jdbi));
    }
}