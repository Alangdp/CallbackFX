package com.connectasistemas.framework.interfaces;

import org.jdbi.v3.core.Jdbi;

/**
 * Interface padrão para migração
 */
public interface Migration {
    void up(Jdbi jdbi);
}