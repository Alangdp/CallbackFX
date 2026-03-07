package com.connectasistemas.framework.internal.events;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.internal.interfaces.EventBinderEvents;
import com.connectasistemas.framework.internal.utils.CallbackInvoker;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Binder de eventos para um ComboBox
 */
public class EventBinderComboBox extends EventBinderEvents {

    String acronym;
    ComboBox<?> comboBox;
    Object screenInstance;
    Object callbacksInstance;

    public EventBinderComboBox(
            String acronym,
            ComboBox<?> comboBox,
            Object screenInstance,
            Object callbacksInstance
    ) {
        this.acronym = acronym;
        this.comboBox = comboBox;
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

        unregisters.add(registerNavigationTracker(comboBox));

        boolean hasEntcam = CallbackInvoker.exists(callbacksInstance, "entcam", acronym);
        boolean hasSaicam = CallbackInvoker.exists(callbacksInstance, "saicam", acronym);

        if (hasEntcam || hasSaicam) {
            ChangeListener<Boolean> focusListener = (obs, oldV, newV) -> {
                if (newV) {
                    publishEvent(EventType.ENTCAM);
                    if (hasEntcam) {
                        CallbackInvoker.call(callbacksInstance, screenInstance, "entcam", acronym);
                    }
                } else {
                    publishEvent(EventType.SAICAM);

                    if (consumeValidationSkip()) {
                        changeValida(false);
                        clearExitReason();
                        return;
                    }

                    changeValida(shouldValidateOnExit());
                    if (hasSaicam) {
                        resetErrorTracking();
                        CallbackInvoker.call(callbacksInstance, screenInstance, "saicam", acronym);
                        focusIfError(comboBox);
                    }
                    clearExitReason();
                }
            };

            comboBox.focusedProperty().addListener(focusListener);
            unregisters.add(() -> comboBox.focusedProperty().removeListener(focusListener));
        }

        return unregisters;
    }

    /**
     * Aplica eventos de alteração do valor selecionado
     * ---------- ALTERAÇÃO (altcam) ----------
     * Se existir callback "altcam", dispara sempre que o valor selecionado mudar
     * oldV e newV não importam para a chamada, é só detecção de alteração
     */
    @Override
    public List<Runnable> applyAltcamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (CallbackInvoker.exists(callbacksInstance, "altcam", acronym)) {

            ChangeListener<Object> valueListener = (obs, oldV, newV) -> {
                publishEvent(EventType.ALTCAM);
                CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym);
            };

            comboBox.valueProperty().addListener(valueListener);
            unregisters.add(() -> comboBox.valueProperty().removeListener(valueListener));
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

            var oldHandler = comboBox.getOnKeyPressed();

            var newHandler = (EventHandler<KeyEvent>) e -> {
                publishEvent(EventType.TECLAD);
                // Passa o evento de tecla para o callback
                CallbackInvoker.call(callbacksInstance, screenInstance, "teclad", acronym, e);

                // Mantém o comportamento original do componente
                if (oldHandler != null) oldHandler.handle(e);
            };

            comboBox.setOnKeyPressed(newHandler);
            unregisters.add(() -> comboBox.setOnKeyPressed(oldHandler));
        }

        return unregisters;
    }
}
