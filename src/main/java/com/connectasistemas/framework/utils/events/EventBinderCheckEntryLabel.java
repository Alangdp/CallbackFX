package com.connectasistemas.framework.utils.events;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.fxelements.CheckEntryLabel;
import com.connectasistemas.framework.interfaces.EventBinderEvents;
import com.connectasistemas.framework.utils.CallbackInvoker;
import com.connectasistemas.framework.utils.Status;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Binder de eventos para um CheckEntryLabel
 */
public class EventBinderCheckEntryLabel extends EventBinderEvents {

    String acronym;
    CheckEntryLabel entry;
    Object screenInstance;
    Object callbacksInstance;

    public EventBinderCheckEntryLabel(
            String acronym,
            CheckEntryLabel entry,
            Object screenInstance,
            Object callbacksInstance
    ) {
        this.acronym = acronym;
        this.entry = entry;
        this.screenInstance = screenInstance;
        this.callbacksInstance = callbacksInstance;
    }

    @Override
    public List<Runnable> applyEntcamSaicamEvent() {
        CheckBox checkBox = this.entry.getCheckBox();

        List<Runnable> unregisters = new ArrayList<>();

        unregisters.add(registerNavigationTracker(checkBox));

        boolean hasEntcam = CallbackInvoker.exists(callbacksInstance, "entcam", acronym);
        boolean hasSaicam = CallbackInvoker.exists(callbacksInstance, "saicam", acronym);

        ChangeListener<Boolean> focusListener = (obs, oldV, newV) -> {
            if (newV) {
                publishEvent(EventType.ENTCAM);
                if (hasEntcam) {
                    CallbackInvoker.call(callbacksInstance, screenInstance, "entcam", acronym);
                }
                return;
            }

            publishEvent(EventType.SAICAM);

            if (Status.consumeValidationSkip()) {
                Status.VALIDA = false;
                clearExitReason();
                return;
            }

            Status.VALIDA = shouldValidateOnExit();

            if (hasSaicam) {
                resetErrorTracking();
                CallbackInvoker.call(callbacksInstance, screenInstance, "saicam", acronym);
                focusIfError(this.entry);
            }

            clearExitReason();
        };

        checkBox.focusedProperty().addListener(focusListener);
        unregisters.add(() -> checkBox.focusedProperty().removeListener(focusListener));

        return unregisters;
    }

    @Override
    public List<Runnable> applyTecladEvent() {
        List<Runnable> unregisters = new ArrayList<>();
        CheckBox checkBox = this.entry.getCheckBox();

        if (CallbackInvoker.exists(callbacksInstance, "teclad", acronym)) {

            var oldHandler = checkBox.getOnKeyPressed();

            var newHandler = (EventHandler<KeyEvent>) e -> {
                publishEvent(EventType.TECLAD);
                CallbackInvoker.call(callbacksInstance, screenInstance, "teclad", acronym, e);

                if (oldHandler != null) {
                    oldHandler.handle(e);
                }
            };

            checkBox.setOnKeyPressed(newHandler);
            unregisters.add(() -> checkBox.setOnKeyPressed(oldHandler));
        }

        return unregisters;
    }

    @Override
    public List<Runnable> applyAltcamEvent() {
        List<Runnable> unregisters = new ArrayList<>();
        CheckBox checkBox = this.entry.getCheckBox();

        if (CallbackInvoker.exists(callbacksInstance, "altcam", acronym)) {

            ChangeListener<Boolean> selectedListener = (obs, oldV, newV) -> {
                publishEvent(EventType.ALTCAM);
                CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym);
            };

            checkBox.selectedProperty().addListener(selectedListener);
            unregisters.add(() -> checkBox.selectedProperty().removeListener(selectedListener));
        }

        return unregisters;
    }
}
