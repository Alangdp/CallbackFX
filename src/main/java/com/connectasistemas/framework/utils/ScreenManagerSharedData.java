package com.connectasistemas.framework.utils;

import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Gerencia dados compartilhados entre telas
 */
public class ScreenManagerSharedData {
    // Mapa de @Screen -> (Acronym -> elemento instanciado)
    private static final Map<Object, Map<String, Object>> CACHE = new WeakHashMap<>();

    /**
     * Apaga dados de uma tela
     * @param key Objeto da tela
     */
    public static void resetScreenData(Object key) {
        if (key == null) {
            return;
        }
        CACHE.remove(key);
    }

    /**
     * Apaga dados de todas as telas salvas
     */
    public static void resetScreenData() {
        CACHE.clear();
    }

    /**
     * Retorna o Map usado como cache
     * OBS: é usado para manipulação mais fácil de elementos
     * @return Cache por janela por acronym
     */
    public static Map<Object, Map<String, Object>> getCache() {
        return CACHE;
    }

    /**
     * Adiciona um elemento no Cache
     *
     * @param screen Objeto da tela atual
     * @param key    Acronym do campo
     * @param value  Instância do elemento em tela
     */
    public static void setScreenData(Object screen, String key, Object value) {
        // Caso não exista um Map para a tela atual cria-o
        if (screen == null) {
            throw new IllegalArgumentException(StringUtils.concat(
                    "Tela não pode ser nula ao registrar dados"));
        }

        if (value == null) {
            throw new IllegalArgumentException(StringUtils.concat(
                    "Elemento não pode ser nulo ao registrar dados"));
        }

        CACHE.putIfAbsent(screen, new HashMap<>());

        // Tenta obter o elemento a ser adicionado
        Map<String, Object> screenData = CACHE.get(screen);
        Object node = screenData.get(key);

        // Caso já exista retorna uma exceção
        if (node != null) {
            throw new RuntimeException(StringUtils.concat(
                    "Elemento já adicionado para a tela atual: ", key));
        }

        // Adiciona o elemento na lista
        screenData.putIfAbsent(key, value);
    }

    /**
     * Procura um elemento no Cache
     *
     * @param screen Objeto da tela atual
     * @param key    Acronym do campo
     * @return Node retornado
     */
    public static Object getScreenData(Object screen, String key) {
        // Caso não exista um Map para a tela atual cria-o
        if (screen == null) {
            throw new IllegalArgumentException(StringUtils.concat(
                    "Tela não pode ser nula ao consultar dados"));
        }

        CACHE.putIfAbsent(screen, new HashMap<>());

        // Tenta obter o elemento a ser adicionado
        Map<String, Object> screenData = CACHE.get(screen);
        Object node = screenData.get(key);

        if (node == null) {
            throw new RuntimeException(StringUtils.concat(
                    "Elemento procurado não existe: ", key));
        }

        return node;
    }

    /**
     * Procura um elemento no Cache como Node
     *
     * @param screen Objeto da tela atual
     * @param key    Acronym do campo
     * @return Node retornado
     */
    public static Node getScreenDataAsNode(Object screen, String key) {
        Object element = getScreenData(screen, key);

        if (element instanceof Node node) {
            return node;
        }

        throw new RuntimeException(StringUtils.concat(
                "Elemento procurado não é um Node: ", key));
    }

    /**
     * Procura um elemento no Cache como Region
     *
     * @param screen Objeto da tela atual
     * @param key    Acronym do campo
     * @return Node retornado
     */
    public static Region getScreenDataAsRegion(Object screen, String key) {
        // Tenta obter o elemento a ser adicionado
        Map<String, Object> screenData = CACHE.get(screen);
        if (screenData == null) {
            throw new RuntimeException(StringUtils.concat("Tela não registrada ao procurar o elemento: ", key));
        }

        Object node = screenData.get(key);
        if (node == null) {
            throw new RuntimeException(StringUtils.concat("Elemento procurado não existe: ", key));
        }

        if (node instanceof Region region) {
            return region;
        }

        throw new RuntimeException(StringUtils.concat("Elemento procurado não é um Region: ", key));
    }
}
