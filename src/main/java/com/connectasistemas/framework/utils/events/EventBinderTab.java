package com.connectasistemas.framework.utils.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Tab;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.interfaces.EventBinderEvents;
import com.connectasistemas.framework.utils.CallbackInvoker;

/**
 * Binder de eventos para Tab.
 */
public class EventBinderTab extends EventBinderEvents {

    private final String acronym;
    private final Tab tab;
    private final Object screenInstance;
    private final Object callbacksInstance;

    public EventBinderTab(String acronym,
                          Tab tab,
                          Object screenInstance,
                          Object callbacksInstance) {
        this.acronym = acronym;
        this.tab = tab;
        this.screenInstance = screenInstance;
        this.callbacksInstance = callbacksInstance;
    }

    @Override
    public List<Runnable> applyEntcamSaicamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        boolean hasEntcam = CallbackInvoker.exists(callbacksInstance, "entcam", acronym);
        boolean hasSaicam = CallbackInvoker.exists(callbacksInstance, "saicam", acronym);

        if (!hasEntcam && !hasSaicam) {
            return unregisters;
        }

        ChangeListener<Boolean> listener = (obs, oldSelected, newSelected) -> {
            if (Boolean.TRUE.equals(newSelected)) {
                publishEvent(EventType.ENTCAM);
                if (hasEntcam) {
                    CallbackInvoker.call(callbacksInstance, screenInstance, "entcam", acronym);
                }
                return;
            }

            if (Boolean.FALSE.equals(newSelected) && Boolean.TRUE.equals(oldSelected)) {
                publishEvent(EventType.SAICAM);
                if (hasSaicam) {
                    CallbackInvoker.call(callbacksInstance, screenInstance, "saicam", acronym);
                }
            }
        };

        tab.selectedProperty().addListener(listener);
        unregisters.add(() -> tab.selectedProperty().removeListener(listener));
        return unregisters;
    }

    @Override
    public List<Runnable> applyAltcamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (!CallbackInvoker.exists(callbacksInstance, "altcam", acronym)) {
            return unregisters;
        }

        ChangeListener<Boolean> listener = (obs, oldSelected, newSelected) -> {
            if (Boolean.TRUE.equals(newSelected)) {
                publishEvent(EventType.ALTCAM);
                CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym);
            }
        };

        tab.selectedProperty().addListener(listener);
        unregisters.add(() -> tab.selectedProperty().removeListener(listener));
        return unregisters;
    }

    @Override
    public List<Runnable> applyTecladEvent() {
        return Collections.emptyList();
    }
}
