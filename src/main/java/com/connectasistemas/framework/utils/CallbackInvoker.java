package com.connectasistemas.framework.utils;

import java.lang.reflect.Method;

/**
 * Responsável pela montagem de nomes e chamada de funções de callbacks
 */
public class CallbackInvoker {

    // Comentário acima da linha

    /**
     * Monta o nome no formato padrão de callbacks
     *
     * OBS: os métodos aqui seguem a seguinte tabela de nomenclatura para os callbacks:
     * Prefixo do evento  |	Callback gerado
     * entcam	          | callbackEntcamNome
     * saicam	          | callbackSaicamNome
     * teclad	          | callbackTecladNome
     * altcam	          | callbackAltcamNome
     * valida	          | callbackValidaNome
     * prebrw	          | callbackPrebrwNome
     * posbrw	          | callbackPosbrwNome
     * outace	          | callbackOutaceNome
     * ceplgr	          | callbackCeplgrNome
     *
     * Os paragrafos geram o nome:
     *
     * callback<Prefixo do evento><Nome da variável>
     *
     * @param event Nome do evento exemplo: "entcam"
     * @param acronym Sigla do callback definida no @ScreenElement
     * @return Nome do paragrafo de callback final
     */
    private static String buildName(String event, String acronym) {
        String ac = acronym.substring(0,1).toUpperCase() + acronym.substring(1);
        return "callback" + event.substring(0,1).toUpperCase() + event.substring(1) + ac;
    }

    // Comentário acima da linha

    /**
     * Chama o método caso seja válido, por padrão sempre passa a instância de tela atual para o callback
     * @param callbacksInstance Instância do Controller
     * @param screenInstance Instância da tela atual
     * @param event Instância nome do evento
     * @param acronym Sigla do campo
     * @param extraArgs Parâmetro extra (É passado direto para o callback)
     */
    public static void call(Object callbacksInstance,
                            Object screenInstance,
                            String event,
                            String acronym,
                            Object... extraArgs) {

        // Se não houver controller
        if (callbacksInstance == null) return;

        // Nome do callback montado
        String expectedPrefix = buildName(event, acronym);

        // Varre os métodos declarados no controller
        for (var m : callbacksInstance.getClass().getDeclaredMethods()) {
            // Se o nome do callback bater com o nome da função atual
            if (m.getName().equals(expectedPrefix)) {
                // Converte o atributo para "public" mesmo que seja privado
                m.setAccessible(true);

                try {
                    // Se houver argumentos extras monta um novo array com [Instância da tela, argumentos]
                    if (extraArgs != null && extraArgs.length > 0) {
                        Object[] args = new Object[extraArgs.length + 1];
                        args[0] = screenInstance;
                        System.arraycopy(extraArgs, 0, args, 1, extraArgs.length);
                        m.invoke(callbacksInstance, args);
                    // Caso contrário chama a função e passa a instância atual da janela
                    } else {
                        m.invoke(callbacksInstance, screenInstance);
                    }
                } catch (Exception ignored) {}
                return;
            }
        }
    }

    /**
     * Busca um método com prefixo e sigla
     *
     * @param callbacksInstance Instância do Controller
     * @param event Instância nome do evento
     * @param acronym Sigla do campo
     */
    public static boolean exists(Object callbacksInstance, String event, String acronym) {
        // Se não houver controller
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
