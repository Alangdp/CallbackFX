package com.connectasistemas.framework.utils.events;

import com.connectasistemas.framework.interfaces.EventBinderEvents;
import com.connectasistemas.framework.utils.CallbackInvoker;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Binder de eventos para um Button
 */
public class EventBinderButton extends EventBinderEvents {

    String acronym;
    Button button;
    Object screenInstance;
    Object callbacksInstance;

    public EventBinderButton(
            String acronym,
            Button button,
            Object screenInstance,
            Object callbacksInstance
    ) {
        this.acronym = acronym;
        this.button = button;
        this.screenInstance = screenInstance;
        this.callbacksInstance = callbacksInstance;
    }

    /**
     * Aplica eventos de entrada e saída de campo
     * ---------- FOCUS (entcam / saicam) ----------
     * Se existir callback para entrada ou saída de foco, registra listener
     * Quando o campo ganha foco -> chama "entcam"
     * Quando o campo perde foco -> chama "saicam"
     */
    @Override
    public List<Runnable> applyEntcamSaicamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (CallbackInvoker.exists(callbacksInstance, "entcam", acronym)
                || CallbackInvoker.exists(callbacksInstance, "saicam", acronym)) {
            var focusListener = (ChangeListener<Boolean>) (obs, oldV, newV) -> {
                if (newV) {
                    CallbackInvoker.call(callbacksInstance, screenInstance, "entcam", acronym);
                } else {
                    CallbackInvoker.call(callbacksInstance, screenInstance, "saicam", acronym);
                }
            };

            button.focusedProperty().addListener(focusListener);
            unregisters.add(() -> button.focusedProperty().removeListener(focusListener));
        }

        return unregisters;
    }

    /**
     * Aplica eventos de alteração no botão
     * ---------- ALTERAÇÃO (altcam) ----------
     * Se existir callback "altcam", dispara sempre que o botão dispara o onAction
     */
    @Override
    public List<Runnable> applyAltcamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (CallbackInvoker.exists(callbacksInstance, "altcam", acronym)) {

            var oldHandler = button.getOnAction();

            var newHandler = (EventHandler<ActionEvent>) e -> {
                CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym);

                if (oldHandler != null) oldHandler.handle(e);
            };

            button.setOnAction(newHandler);
            unregisters.add(() -> button.setOnAction(oldHandler));
        }

        return unregisters;
    }

    /**
     * Aplica eventos de alteração de campo
     * ---------- TECLA (teclad) ----------
     * Se existir callback "teclad", registra um novo handler de tecla
     * O callback recebe o KeyEvent também
     * Mantém o handler antigo: chama o callback e depois o original
     */
    @Override
    public List<Runnable> applyTecladEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (CallbackInvoker.exists(callbacksInstance, "teclad", acronym)) {

            var oldHandler = button.getOnKeyPressed();

            var newHandler = (EventHandler<KeyEvent>) e -> {
                // Passa o evento de tecla para o callback
                CallbackInvoker.call(callbacksInstance, screenInstance, "teclad", acronym, e);

                // Mantém o comportamento original do componente
                if (oldHandler != null) oldHandler.handle(e);
            };

            button.setOnKeyPressed(newHandler);
            unregisters.add(() -> button.setOnKeyPressed(oldHandler));
        }

        return unregisters;
    }
}
