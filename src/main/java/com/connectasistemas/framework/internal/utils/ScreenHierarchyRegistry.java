package com.connectasistemas.framework.internal.utils;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mantém o relacionamento entre telas pai e telas embutidas para permitir o
 * descarte recursivo dos recursos registrados (eventos, cache de nós, etc.).
 */
public final class ScreenHierarchyRegistry {

    private static final Map<Object, List<Object>> CHILDREN_BY_PARENT = new IdentityHashMap<>();

    private ScreenHierarchyRegistry() {
    }

    /**
     * Registra o relacionamento pai-filho para permitir descarte em cascata.
     */
    public static void registerChild(Object parent, Object child) {
        if (parent == null || child == null) {
            return;
        }

        CHILDREN_BY_PARENT.computeIfAbsent(parent, key -> new ArrayList<>()).add(child);
    }

    /**
     * Remove todos os filhos registrados de um pai específico, retornando uma
     * cópia para iteração segura.
     */
    public static List<Object> detachChildren(Object parent) {
        if (parent == null) {
            return List.of();
        }

        List<Object> children = CHILDREN_BY_PARENT.remove(parent);
        if (children == null || children.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(children);
    }

    /**
     * Limpa completamente os vínculos registrados (usado em testes ou resets).
     */
    public static void clear() {
        CHILDREN_BY_PARENT.clear();
    }
}
