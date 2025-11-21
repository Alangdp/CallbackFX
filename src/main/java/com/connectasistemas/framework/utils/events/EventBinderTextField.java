package com.connectasistemas.framework.utils.events;

import com.connectasistemas.framework.interfaces.EventBinderEvents;
import com.connectasistemas.framework.utils.CallbackInvoker;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Binder o de eventos para um textfield
 */
public class EventBinderTextField extends EventBinderEvents {

    String acronym;
    TextField txt;
    Object screenInstance;
    Object callbacksInstance;

    public EventBinderTextField(
            String acronym,
            TextField txt,
            Object screenInstance,
            Object callbacksInstance
    ) {
        this.acronym = acronym;
        this.txt = txt;
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

            txt.focusedProperty().addListener(focusListener);
            unregisters.add(() -> txt.focusedProperty().removeListener(focusListener));
        }

        return unregisters;
    }

    /**
     * Aplica eventos de teclar no campo
     * ---------- ALTERAÇÃO (altcam) ----------
     * Se existir callback "altcam", dispara sempre que o texto mudar
     * oldV e newV não importam para a chamada, é só detecção de alteração
     */
    @Override
    public List<Runnable> applyAltcamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (CallbackInvoker.exists(callbacksInstance, "altcam", acronym)) {

            ChangeListener<String> textListener = (obs, oldV, newV) -> {
                CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym);
            };

            txt.textProperty().addListener(textListener);
            unregisters.add(() -> txt.textProperty().removeListener(textListener));
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

            var oldHandler = txt.getOnKeyPressed();

            var newHandler = (EventHandler<KeyEvent>) e -> {
                // Passa o evento de tecla para o callback
                CallbackInvoker.call(callbacksInstance, screenInstance, "teclad", acronym, e);

                // Mantém o comportamento original do componente
                if (oldHandler != null) oldHandler.handle(e);
            };

            txt.setOnKeyPressed(newHandler);
            unregisters.add(() -> txt.setOnKeyPressed(oldHandler));
        }

        return unregisters;
    }
}
