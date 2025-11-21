package com.connectasistemas.framework.utils.events;

import com.connectasistemas.framework.interfaces.EventBinderEvents;
import com.connectasistemas.framework.utils.CallbackInvoker;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Binder de eventos para um CheckBox
 */
public class EventBinderCheckBox extends EventBinderEvents {

    String acronym;
    CheckBox checkBox;
    Object screenInstance;
    Object callbacksInstance;

    public EventBinderCheckBox(
            String acronym,
            CheckBox checkBox,
            Object screenInstance,
            Object callbacksInstance
    ) {
        this.acronym = acronym;
        this.checkBox = checkBox;
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
            ChangeListener<Boolean> focusListener = (obs, oldV, newV) -> {
                if (newV) {
                    CallbackInvoker.call(callbacksInstance, screenInstance, "entcam", acronym);
                } else {
                    CallbackInvoker.call(callbacksInstance, screenInstance, "saicam", acronym);
                }
            };

            checkBox.focusedProperty().addListener(focusListener);
            unregisters.add(() -> checkBox.focusedProperty().removeListener(focusListener));
        }

        return unregisters;
    }

    /**
     * Aplica eventos de teclar no campo
     * ---------- ALTERAÇÃO (altcam) ----------
     * Se existir callback "altcam", dispara sempre que o estado marcado mudar
     * oldV e newV não importam para a chamada, é só detecção de alteração
     */
    @Override
    public List<Runnable> applyAltcamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (CallbackInvoker.exists(callbacksInstance, "altcam", acronym)) {

            ChangeListener<Boolean> selectedListener = (obs, oldV, newV) -> {
                CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym);
            };

            checkBox.selectedProperty().addListener(selectedListener);
            unregisters.add(() -> checkBox.selectedProperty().removeListener(selectedListener));
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

            var oldHandler = checkBox.getOnKeyPressed();

            var newHandler = (EventHandler<KeyEvent>) e -> {
                // Passa o evento de tecla para o callback
                CallbackInvoker.call(callbacksInstance, screenInstance, "teclad", acronym, e);

                // Mantém o comportamento original do componente
                if (oldHandler != null) oldHandler.handle(e);
            };

            checkBox.setOnKeyPressed(newHandler);
            unregisters.add(() -> checkBox.setOnKeyPressed(oldHandler));
        }

        return unregisters;
    }
}
