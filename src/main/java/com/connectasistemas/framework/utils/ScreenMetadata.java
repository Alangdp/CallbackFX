package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.annotation.ScreenField;
import javafx.scene.layout.Region;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Armazena informações carregadas das anotações de uma tela
 */
public class ScreenMetadata {
    // Elemento root do JavaFX
    Region root;

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
        if (fields.containsKey(acronym)) {
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

    public Region root() {
        return root;
    }

    public void setRoot(Class<?> clz) {
        this.root = RegionManager.createRegion(clz);
    }

    public void overrideRoot(Region region) {
        if (region == null) {
            throw new IllegalArgumentException("Root não pode ser nulo");
        }
        this.root = region;
    }

    // Retorna o mapa de campos anotados]
    // OBS: Retorna a ordem hierárquica conforme os pais e o order definido em @ScreenFieldPosition
    public Map<String, Field> getFields() {

        List<Map.Entry<String, Field>> roots = new ArrayList<>();
        Map<String, List<Map.Entry<String, Field>>> childrenByFather = new HashMap<>();

        fields.forEach((acronym, field) -> {
            ScreenField annotation = field.getAnnotation(ScreenField.class);
            if (annotation == null) {
                return;
            }

            String father = annotation.father();
            if (father == null || father.isEmpty()) {
                roots.add(Map.entry(acronym, field));
            } else {
                childrenByFather
                        .computeIfAbsent(father, key -> new ArrayList<>())
                        .add(Map.entry(acronym, field));
            }
        });

        LinkedHashMap<String, Field> ordered = new LinkedHashMap<>();
        Set<String> visited = new HashSet<>();

        traverseHierarchy(roots, childrenByFather, ordered, visited);

        fields.forEach((acronym, field) -> ordered.putIfAbsent(acronym, field));

        return ordered;
    }

    /**
     * Varre a hierarquia dos nós de forma recursiva
     * @param nodes Nó atual
     * @param childrenByFather Filhos por pai
     * @param ordered Nós ordenados
     * @param visited Nós visitados
     */
    private void traverseHierarchy(List<Map.Entry<String, Field>> nodes,
            Map<String, List<Map.Entry<String, Field>>> childrenByFather,
            LinkedHashMap<String, Field> ordered,
            Set<String> visited) {

        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        nodes.sort(Comparator
                .comparingInt((Map.Entry<String, Field> entry) -> normalizedOrder(entry.getValue()
                        .getAnnotation(ScreenField.class).order()))
                .thenComparing(Map.Entry::getKey));

        for (Map.Entry<String, Field> entry : nodes) {
            String acronym = entry.getKey();
            if (!visited.add(acronym)) {
                continue;
            }

            ordered.put(acronym, entry.getValue());

            List<Map.Entry<String, Field>> children = childrenByFather.get(acronym);
            if (children != null) {
                traverseHierarchy(children, childrenByFather, ordered, visited);
            }
        }
    }

    // Normaliza o order para facilitar a ordenação
    // OBS: valores <= 0 são considerados como Integer.MAX_VALUE ou seja, vão para o final
    private static int normalizedOrder(int order) {
        return order <= 0 ? Integer.MAX_VALUE : order;
    }
}
