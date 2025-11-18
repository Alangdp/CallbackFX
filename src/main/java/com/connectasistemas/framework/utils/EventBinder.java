package com.connectasistemas.framework.utils;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.util.*;

/**
 * Responsável por "Ativar" os eventos do javaFx e interligar eles com os callbacks personalizados
 */
public class EventBinder {

    // Map com a seguinte hierarquia
    // Instância de tela -> Map de elementos -> Lista de eventos
    // Tem essa hierarquia para poder apagar eventos de maneira simples, tive alguns problemas chatos com JavaFX por...
    // ...Causa disso
    private static final Map<Object, Map<Node, List<Runnable>>> EVENT_MAP = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Vincula os eventos do javaFX com as funções de callback baseado na entrada
     *
     * @param acronym Sigla da campo
     * @param node Instância do elemento
     * @param screenInstance Instância da tela
     * @param callbacksInstance Instância do controller
     */
    public static void attach(
            String acronym,
            Node node,
            Object screenInstance,
            Object callbacksInstance
    ) {
        // Se não houver tela
        if (screenInstance == null) return;

        // Se a tela já não estiver adicionada, adiciona ao Map e já cria um novo para aquele mesmo valor
        EVENT_MAP.computeIfAbsent(screenInstance, k -> new HashMap<>());

        // Map de eventos por elemento
        Map<Node, List<Runnable>> screenEvents = EVENT_MAP.get(screenInstance);

        // Se Já tem eventos aplicados → não reaplica
        if (screenEvents.containsKey(node)) {
            return;
        }

        List<Runnable> unregisters = new ArrayList<>();

        // TextField
        if (node instanceof TextField txt) {
            // ---------- FOCUS (entcam / saicam) ----------
            // Se existir callback para entrada ou saída de foco, registra listener
            // Quando o campo ganha foco -> chama "entcam"
            // Quando o campo perde foco -> chama "saicam"
            if (CallbackInvoker.exists(callbacksInstance, "entcam", acronym)
                    || CallbackInvoker.exists(callbacksInstance, "saicam", acronym)) {
                ChangeListener<Boolean> focusListener = (obs, oldV, newV) -> {
                    if (newV) {
                        CallbackInvoker.call(callbacksInstance, screenInstance, "entcam", acronym);
                    } else {
                        CallbackInvoker.call(callbacksInstance, screenInstance, "saicam", acronym);
                    }
                };

                txt.focusedProperty().addListener(focusListener);
                unregisters.add(() -> txt.focusedProperty().removeListener(focusListener));
            }
            // ---------- ALTERAÇÃO (altcam) ----------
            // Se existir callback "altcam", dispara sempre que o texto mudar
            // oldV e newV não importam para a chamada, é só detecção de alteração
            if (CallbackInvoker.exists(callbacksInstance, "altcam", acronym)) {

                ChangeListener<String> textListener = (obs, oldV, newV) -> {
                    CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym);
                };

                txt.textProperty().addListener(textListener);
                unregisters.add(() -> txt.textProperty().removeListener(textListener));
            }
            // ---------- TECLA (teclad) ----------
            // Se existir callback "teclad", registra um novo handler de tecla
            // O callback recebe o KeyEvent também
            // Mantém o handler antigo: chama o callback e depois o original
            if (CallbackInvoker.exists(callbacksInstance, "teclad", acronym)) {

                var oldHandler = txt.getOnKeyPressed();

                var newHandler = (javafx.event.EventHandler<javafx.scene.input.KeyEvent>) e -> {
                    // Passa o evento de tecla para o callback
                    CallbackInvoker.call(callbacksInstance, screenInstance, "teclad", acronym, e);

                    // Mantém o comportamento original do componente
                    if (oldHandler != null) oldHandler.handle(e);
                };

                txt.setOnKeyPressed(newHandler);
                unregisters.add(() -> txt.setOnKeyPressed(oldHandler));
            }

        }

        // Salva no registro
        screenEvents.put(node, unregisters);
    }

    /**
     * Deleta todos os eventos vinculadas a tela de entrada
     * @param screenInstance Instância da tela
     */
    public static void deleteEvents(Object screenInstance) {
        if (screenInstance == null) return;

        Map<Node, List<Runnable>> screenEvents = EVENT_MAP.get(screenInstance);
        if (screenEvents == null) return;

        for (var entry : screenEvents.entrySet()) {
            for (Runnable r : entry.getValue()) {
                try {
                    r.run();
                } catch (Exception ignored) {}
            }
        }

        EVENT_MAP.remove(screenInstance);
    }
}
