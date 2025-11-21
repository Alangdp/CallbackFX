package com.connectasistemas.framework.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Armazena informações carregadas das anotações de uma tela
 */
public class ScreenMetadata {

    // Título da tela (do @Screen)
    private String title;

    // Tamanho da tela (do @Screen)
    private int width;
    private int height;

    // Callbacks (Instância)
    private Object callbackInstance;

    // Mapa dos campos anotados: acrônimo → Field
    private final Map<String, Field> fields = new HashMap<>();

    // Define o título
    public void setTitle(String title) {
        this.title = title;
    }

    // Adiciona um campo anotado
    public void addField(String acronym, Field field) {
        // Caso já exista retorna uma exceção
        if(fields.containsKey(acronym)) {
            throw new RuntimeException(StringUtils.concat("Elemento já adicionado para a tela atual: ", acronym));
        }

        fields.put(acronym, field);
    }

    // Retorna o título
    public String getTitle() {
        return title;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String title() {
        return title;
    }

    public Object callbackInstance() {
        return callbackInstance;
    }

    public void setCallbackInstance(Object callbackInstance) {
        this.callbackInstance = callbackInstance;
    }

    // Retorna o mapa de campos anotados
    public Map<String, Field> getFields() {
        return fields;
    }
}
