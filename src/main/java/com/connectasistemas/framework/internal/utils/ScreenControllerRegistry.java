package com.connectasistemas.framework.internal.utils;

import com.connectasistemas.framework.internal.utils.records.ScreenView;
import com.connectasistemas.framework.utils.ScreenRuntimeContext;
import com.connectasistemas.framework.utils.ScreenRuntimeContext.ScreenControllerContext;

/**
 * Centraliza o registro de instancias de telas e controllers ativos.
 * Delega para {@link ScreenRuntimeContext#getDefault()}.
 */
public class ScreenControllerRegistry {

    private static final ScreenRuntimeContext ctx = ScreenRuntimeContext.getDefault();

    private ScreenControllerRegistry() {
    }

    /**
     * Registra a instancia de tela e o respectivo controller ativos.
     */
    public static void register(ScreenView view) {
        if (view == null || view.metadata() == null) {
            return;
        }

        Object controller = view.metadata().callbackInstance();
        if (controller == null) {
            return;
        }

        ScreenControllerContext context = new ScreenControllerContext(view.screenInstance(), controller);
        ctx.getControllerContexts().put(view.screenClass(), context);
        ctx.getInstanceToClass().put(view.screenInstance(), view.screenClass());
    }

    /**
     * Remove o vinculo referente a instancia informada.
     */
    public static void unregister(Object screenInstance) {
        if (screenInstance == null) {
            return;
        }

        Class<?> screenClass = ctx.getInstanceToClass().remove(screenInstance);
        if (screenClass == null) {
            return;
        }

        ScreenControllerContext context = ctx.getControllerContexts().get(screenClass);
        if (context != null && context.screenInstance() == screenInstance) {
            ctx.getControllerContexts().remove(screenClass);
        }
    }

    /**
     * Retorna o controller registrado para a tela informada.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getControllerReference(Class<?> screenClass) {
        if (screenClass == null) {
            return null;
        }

        ScreenControllerContext context = ctx.getControllerContexts().get(screenClass);
        return context != null ? (T) context.controller() : null;
    }

    /**
     * Retorna a instancia da tela correspondente a classe.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getScreenReference(Class<T> screenClass) {
        if (screenClass == null) {
            return null;
        }

        ScreenControllerContext context = ctx.getControllerContexts().get(screenClass);
        return context != null ? (T) context.screenInstance() : null;
    }
}
