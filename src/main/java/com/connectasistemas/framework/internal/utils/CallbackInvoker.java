package com.connectasistemas.framework.internal.utils;

import com.connectasistemas.framework.utils.EventContext;

import java.lang.reflect.Method;

/**
 * Responsavel pela montagem de nomes e chamada de funcoes de callbacks.
 * Tenta primeiro a assinatura com {@link EventContext} como segundo argumento;
 * faz fallback para a assinatura original caso nao exista.
 */
public class CallbackInvoker {

    /**
     * Monta o nome no formato padrao de callbacks.
     *
     * OBS: os metodos aqui seguem a seguinte tabela de nomenclatura para os callbacks:
     * Prefixo do evento  |  Callback gerado
     * entcam             | callbackEntcamNome
     * saicam             | callbackSaicamNome
     * teclad             | callbackTecladNome
     * altcam             | callbackAltcamNome
     * valida             | callbackValidaNome
     * prebrw             | callbackPrebrwNome
     * posbrw             | callbackPosbrwNome
     * outace             | callbackOutaceNome
     * ceplgr             | callbackCeplgrNome
     *
     * Os paragrafos geram o nome:
     * callback + Prefixo do evento + Nome da variavel
     *
     * @param event   nome do evento exemplo: "entcam"
     * @param acronym sigla do callback definida no @ScreenElement
     * @return nome do paragrafo de callback final
     */
    private static String buildName(String event, String acronym) {
        String ac = acronym.substring(0,1).toUpperCase() + acronym.substring(1);
        return "callback" + event.substring(0,1).toUpperCase() + event.substring(1) + ac;
    }

    /**
     * Chama o metodo caso seja valido, por padrao sempre passa a instancia de tela
     * atual para o callback. Tenta primeiro a assinatura que aceita
     * {@link EventContext} como segundo argumento; caso nao exista, faz fallback
     * para a assinatura sem contexto e usa internamente o EventContext padrao.
     *
     * @param callbacksInstance instancia do Controller
     * @param screenInstance    instancia da tela atual
     * @param event             nome do evento
     * @param acronym           sigla do campo
     * @param extraArgs         argumentos extras repassados diretamente ao callback
     */
    public static void call(Object callbacksInstance,
                            Object screenInstance,
                            String event,
                            String acronym,
                            Object... extraArgs) {

        // Se nao houver controller
        if (callbacksInstance == null) return;

        // Nome do callback montado
        String expectedPrefix = buildName(event, acronym);

        // Referencia ao metodo encontrado
        Method targetMethod = null;
        boolean hasEventContext = false;

        // Varre os metodos declarados no controller
        for (var m : callbacksInstance.getClass().getDeclaredMethods()) {
            if (!m.getName().equals(expectedPrefix)) {
                continue;
            }

            // Verifica se possui parametro EventContext
            Class<?>[] paramTypes = m.getParameterTypes();
            boolean acceptsContext = paramTypes.length >= 2
                    && EventContext.class.isAssignableFrom(paramTypes[1]);

            if (acceptsContext) {
                // Prioridade para assinatura com EventContext
                targetMethod = m;
                hasEventContext = true;
                break;
            }

            // Guarda como fallback caso nenhuma assinatura com contexto seja encontrada
            if (targetMethod == null) {
                targetMethod = m;
            }
        }

        if (targetMethod == null) {
            return;
        }

        // Converte o atributo para "public" mesmo que seja privado
        targetMethod.setAccessible(true);

        try {
            if (hasEventContext) {
                // Chama a assinatura que aceita EventContext
                invokeWithContext(targetMethod, callbacksInstance, screenInstance, extraArgs);
            } else {
                // Fallback para assinatura original
                invokeWithoutContext(targetMethod, callbacksInstance, screenInstance, extraArgs);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Invoca o callback com EventContext como segundo argumento.
     */
    private static void invokeWithContext(Method method,
                                          Object callbacksInstance,
                                          Object screenInstance,
                                          Object[] extraArgs) throws Exception {
        EventContext ctx = EventContext.getDefault();
        if (extraArgs != null && extraArgs.length > 0) {
            Object[] args = new Object[extraArgs.length + 2];
            args[0] = screenInstance;
            args[1] = ctx;
            System.arraycopy(extraArgs, 0, args, 2, extraArgs.length);
            method.invoke(callbacksInstance, args);
        } else {
            method.invoke(callbacksInstance, screenInstance, ctx);
        }
    }

    /**
     * Invoca o callback sem EventContext (assinatura original).
     */
    private static void invokeWithoutContext(Method method,
                                             Object callbacksInstance,
                                             Object screenInstance,
                                             Object[] extraArgs) throws Exception {
        if (extraArgs != null && extraArgs.length > 0) {
            Object[] args = new Object[extraArgs.length + 1];
            args[0] = screenInstance;
            System.arraycopy(extraArgs, 0, args, 1, extraArgs.length);
            method.invoke(callbacksInstance, args);
        } else {
            method.invoke(callbacksInstance, screenInstance);
        }
    }

    /**
     * Busca um metodo com prefixo e sigla.
     *
     * @param callbacksInstance instancia do Controller
     * @param event             nome do evento
     * @param acronym           sigla do campo
     * @return true se o callback existe
     */
    public static boolean exists(Object callbacksInstance, String event, String acronym) {
        // Se nao houver controller
        if (callbacksInstance == null) return false;

        // Nome do callback montado
        String expectedPrefix = buildName(event, acronym);

        // Varre os callbacks buscando o com o prefixo recebido
        for (var m : callbacksInstance.getClass().getDeclaredMethods()) {
            if (m.getName().equals(expectedPrefix)) {
                return true;
            }
        }
        return false;
    }
}
