package com.connectasistemas.framework.utils;

import javafx.scene.Node;

import java.util.HashMap;
import java.util.Map;

public class ScreenManagerSharedData {
    // Mapa de @Screen -> (String[Acronym] -> Node)
    private static final Map<Object, Map<String, Node>> CACHE = new HashMap<>();

    /**
     * Apaga dados de uma tela
     * @param key Objeto da tela
     */
    public static void resetScreenData(Object key) {
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
    public static Map<Object, Map<String, Node>> getCache() {
        return CACHE;
    }

    /**
     * Adiciona um elemento no Cachea
     *
     * @param screen Objeto da tela atual
     * @param key    Acronym do campo
     * @param value  Instância do elemento em tela
     */
    public static void setScreenData(Object screen, String key, Node value) {
        // Caso não exista um Map para a tela atual cria-o
        CACHE.putIfAbsent(screen, new HashMap<>());

        // Tenta obter o elemento a ser adicionado
        Map<String, Node> screenData = CACHE.get(screen);
        Node node = screenData.get(key);

        // Caso já exista retorna uma exceção
        if (node != null) {
            throw new RuntimeException(StringUtils.concat("Elemento já adicionado para a tela atual: ", key));
        }

        // Adiciona o elemento na lista
        screenData.putIfAbsent(key, value);
    }
}
