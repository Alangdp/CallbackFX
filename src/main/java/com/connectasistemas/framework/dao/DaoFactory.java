package com.connectasistemas.framework.dao;

import com.connectasistemas.framework.utils.DatabaseManager;

/**
 * Classe para pré-carregar o DAO
 * OBS: Desnecessária mais simplifica o processo
 */
public class DaoFactory {
    public static <T> T dao(Class<T> clazz) {
        return DatabaseManager.get().onDemand(clazz);
    }
}