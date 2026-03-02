package com.connectasistemas.framework.internal.utils;

import java.util.ArrayList;
import java.util.List;

import com.connectasistemas.framework.utils.ScreenRuntimeContext;

/**
 * Mantem o relacionamento entre telas pai e telas embutidas para permitir o
 * descarte recursivo dos recursos registrados (eventos, cache de nos, etc.).
 * Delega para {@link ScreenRuntimeContext#getDefault()}.
 */
public final class ScreenHierarchyRegistry {

    private static final ScreenRuntimeContext ctx = ScreenRuntimeContext.getDefault();

    private ScreenHierarchyRegistry() {
    }

    /**
     * Registra o relacionamento pai-filho para permitir descarte em cascata.
     */
    public static void registerChild(Object parent, Object child) {
        if (parent == null || child == null) {
            return;
        }

        ctx.getChildrenByParent().computeIfAbsent(parent, key -> new ArrayList<>()).add(child);
    }

    /**
     * Remove todos os filhos registrados de um pai especifico, retornando uma
     * copia para iteracao segura.
     */
    public static List<Object> detachChildren(Object parent) {
        if (parent == null) {
            return List.of();
        }

        List<Object> children = ctx.getChildrenByParent().remove(parent);
        if (children == null || children.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(children);
    }

    /**
     * Limpa completamente os vinculos registrados (usado em testes ou resets).
     */
    public static void clear() {
        ctx.getChildrenByParent().clear();
    }
}
