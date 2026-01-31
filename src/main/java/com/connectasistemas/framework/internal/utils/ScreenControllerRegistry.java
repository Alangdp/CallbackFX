package com.connectasistemas.framework.internal.utils;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.connectasistemas.framework.internal.utils.records.ScreenView;

/**
 * Centraliza o registro de instâncias de telas e controllers ativos.
 */
public class ScreenControllerRegistry {

    private static final Map<Class<?>, ScreenContext> CONTEXTS = new ConcurrentHashMap<>();
    private static final Map<Object, Class<?>> INSTANCE_TO_CLASS = Collections.synchronizedMap(new WeakHashMap<>());

    private ScreenControllerRegistry() {
    }

    /**
     * Registra a instância de tela e o respectivo controller ativos.
     */
    public static void register(ScreenView view) {
        if (view == null || view.metadata() == null) {
            return;
        }

        Object controller = view.metadata().callbackInstance();
        if (controller == null) {
            return;
        }

        ScreenContext context = new ScreenContext(view.screenInstance(), controller);
        CONTEXTS.put(view.screenClass(), context);
        INSTANCE_TO_CLASS.put(view.screenInstance(), view.screenClass());
    }

    /**
     * Remove o vínculo referente à instância informada.
     */
    public static void unregister(Object screenInstance) {
        if (screenInstance == null) {
            return;
        }

        Class<?> screenClass = INSTANCE_TO_CLASS.remove(screenInstance);
        if (screenClass == null) {
            return;
        }

        ScreenContext context = CONTEXTS.get(screenClass);
        if (context != null && context.screenInstance() == screenInstance) {
            CONTEXTS.remove(screenClass);
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * Retorna o controller registrado para a tela informada.
     */
    public static <T> T getControllerReference(Class<?> screenClass) {
        if (screenClass == null) {
            return null;
        }

        ScreenContext context = CONTEXTS.get(screenClass);
        return context != null ? (T) context.controller() : null;
    }

    @SuppressWarnings("unchecked")
    /**
     * Retorna a instância da tela correspondente à classe.
     */
    public static <T> T getScreenReference(Class<T> screenClass) {
        if (screenClass == null) {
            return null;
        }

        ScreenContext context = CONTEXTS.get(screenClass);
        return context != null ? (T) context.screenInstance() : null;
    }

    private record ScreenContext(Object screenInstance, Object controller) {
    }
}
