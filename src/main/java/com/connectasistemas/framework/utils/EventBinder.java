package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.interfaces.EventBinderEvents;
import com.connectasistemas.framework.utils.events.EventBinderButton;
import com.connectasistemas.framework.utils.events.EventBinderCheckBox;
import com.connectasistemas.framework.utils.events.EventBinderComboBox;
import com.connectasistemas.framework.utils.events.EventBinderTextField;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
        EventBinderEvents binder = null;

        if (node instanceof TextField txt) {
            binder = new EventBinderTextField(acronym, txt, screenInstance, callbacksInstance);
        } else if (node instanceof CheckBox checkBox) {
            binder = new EventBinderCheckBox(acronym, checkBox, screenInstance, callbacksInstance);
        } else if (node instanceof ComboBox<?> comboBox) {
            binder = new EventBinderComboBox(acronym, comboBox, screenInstance, callbacksInstance);
        } else if (node instanceof Button button) {
            binder = new EventBinderButton(acronym, button, screenInstance, callbacksInstance);
        }

        if (binder != null) {
            unregisters.addAll(binder.applyEntcamSaicamEvent());
            unregisters.addAll(binder.applyAltcamEvent());
            unregisters.addAll(binder.applyTecladEvent());
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
