package com.connectasistemas.framework.internal.utils;

import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.Map;

import com.connectasistemas.framework.utils.ScreenRuntimeContext;
import com.connectasistemas.framework.utils.StringUtils;

/**
 * Gerencia dados compartilhados entre telas.
 * Delega para {@link ScreenRuntimeContext#getDefault()}.
 */
public class ScreenManagerSharedData {

    private static final ScreenRuntimeContext ctx = ScreenRuntimeContext.getDefault();

    /**
     * Apaga dados de uma tela.
     *
     * @param key objeto da tela
     */
    public static void resetScreenData(Object key) {
        if (key == null) {
            return;
        }
        ctx.getSharedDataCache().remove(key);
    }

    /**
     * Apaga dados de todas as telas salvas.
     */
    public static void resetScreenData() {
        ctx.getSharedDataCache().clear();
    }

    /**
     * Retorna o Map usado como cache.
     *
     * @return cache por janela por acronym
     */
    public static Map<Object, Map<String, Object>> getCache() {
        return ctx.getSharedDataCache();
    }

    /**
     * Adiciona um elemento no cache.
     *
     * @param screen objeto da tela atual
     * @param key    acronym do campo
     * @param value  instancia do elemento em tela
     */
    public static void setScreenData(Object screen, String key, Object value) {
        if (screen == null) {
            throw new IllegalArgumentException(StringUtils.concat(
                    "Tela nao pode ser nula ao registrar dados"));
        }

        if (value == null) {
            throw new IllegalArgumentException(StringUtils.concat(
                    "Elemento nao pode ser nulo ao registrar dados"));
        }

        Map<Object, Map<String, Object>> cache = ctx.getSharedDataCache();
        cache.putIfAbsent(screen, new HashMap<>());

        Map<String, Object> screenData = cache.get(screen);
        Object node = screenData.get(key);

        if (node != null) {
            throw new RuntimeException(StringUtils.concat(
                    "Elemento ja adicionado para a tela atual: ", key));
        }

        screenData.putIfAbsent(key, value);
    }

    /**
     * Procura um elemento no cache.
     *
     * @param screen objeto da tela atual
     * @param key    acronym do campo
     * @return elemento encontrado
     */
    public static Object getScreenData(Object screen, String key) {
        if (screen == null) {
            throw new IllegalArgumentException(StringUtils.concat(
                    "Tela nao pode ser nula ao consultar dados"));
        }

        Map<Object, Map<String, Object>> cache = ctx.getSharedDataCache();
        cache.putIfAbsent(screen, new HashMap<>());

        Map<String, Object> screenData = cache.get(screen);
        Object node = screenData.get(key);

        if (node == null) {
            throw new RuntimeException(StringUtils.concat(
                    "Elemento procurado nao existe: ", key));
        }

        return node;
    }

    /**
     * Procura um elemento no cache como Node.
     *
     * @param screen objeto da tela atual
     * @param key    acronym do campo
     * @return Node retornado
     */
    public static Node getScreenDataAsNode(Object screen, String key) {
        Object element = getScreenData(screen, key);

        if (element instanceof Node node) {
            return node;
        }

        throw new RuntimeException(StringUtils.concat(
                "Elemento procurado nao e um Node: ", key));
    }

    /**
     * Procura um elemento no cache como Region.
     *
     * @param screen objeto da tela atual
     * @param key    acronym do campo
     * @return Region retornado
     */
    public static Region getScreenDataAsRegion(Object screen, String key) {
        Map<String, Object> screenData = ctx.getSharedDataCache().get(screen);
        if (screenData == null) {
            throw new RuntimeException(StringUtils.concat("Tela nao registrada ao procurar o elemento: ", key));
        }

        Object node = screenData.get(key);
        if (node == null) {
            throw new RuntimeException(StringUtils.concat("Elemento procurado nao existe: ", key));
        }

        if (node instanceof Region region) {
            return region;
        }

        throw new RuntimeException(StringUtils.concat("Elemento procurado nao e um Region: ", key));
    }
}
