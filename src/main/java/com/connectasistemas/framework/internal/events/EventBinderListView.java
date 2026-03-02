package com.connectasistemas.framework.internal.events;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.internal.interfaces.EventBinderEvents;
import com.connectasistemas.framework.internal.utils.CallbackInvoker;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Binder de eventos para um ListView
 */
public class EventBinderListView extends EventBinderEvents {

    String acronym;
    ListView<?> listView;
    Object screenInstance;
    Object callbacksInstance;

    public EventBinderListView(
            String acronym,
            ListView<?> listView,
            Object screenInstance,
            Object callbacksInstance
    ) {
        this.acronym = acronym;
        this.listView = listView;
        this.screenInstance = screenInstance;
        this.callbacksInstance = callbacksInstance;
    }

    @Override
    public List<Runnable> applyEntcamSaicamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        unregisters.add(registerNavigationTracker(listView));

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

            if (consumeValidationSkip()) {
                changeValida(false);
                clearExitReason();
                return;
            }

            changeValida(shouldValidateOnExit());

            if (hasSaicam) {
                resetErrorTracking();
                CallbackInvoker.call(callbacksInstance, screenInstance, "saicam", acronym);
                focusIfError(listView);
            }

            clearExitReason();
        };

        listView.focusedProperty().addListener(focusListener);
        unregisters.add(() -> listView.focusedProperty().removeListener(focusListener));

        return unregisters;
    }

    @Override
    public List<Runnable> applyAltcamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (!CallbackInvoker.exists(callbacksInstance, "altcam", acronym)) {
            return unregisters;
        }

        ChangeListener<Object> selectionListener = (obs, oldV, newV) -> {
            publishEvent(EventType.ALTCAM);
            CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym);
        };

        listView.getSelectionModel().selectedItemProperty().addListener(selectionListener);
        unregisters.add(() -> listView.getSelectionModel().selectedItemProperty().removeListener(selectionListener));

        return unregisters;
    }

    @Override
    public List<Runnable> applyTecladEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (CallbackInvoker.exists(callbacksInstance, "teclad", acronym)) {

            var oldHandler = listView.getOnKeyPressed();

            var newHandler = (EventHandler<KeyEvent>) e -> {
                publishEvent(EventType.TECLAD);
                CallbackInvoker.call(callbacksInstance, screenInstance, "teclad", acronym, e);

                if (oldHandler != null) {
                    oldHandler.handle(e);
                }
            };

            listView.setOnKeyPressed(newHandler);
            unregisters.add(() -> listView.setOnKeyPressed(oldHandler));
        }

        return unregisters;
    }

    @Override
    public List<Runnable> applyCustomEvents() {
        List<Runnable> unregisters = new ArrayList<>();

        boolean hasClick = CallbackInvoker.exists(callbacksInstance, "click", acronym);
        boolean hasDoubleClick = CallbackInvoker.exists(callbacksInstance, "doubleClick", acronym);

        if (!hasClick && !hasDoubleClick) {
            return unregisters;
        }

        var oldHandler = listView.getOnMouseClicked();

        var newHandler = (EventHandler<MouseEvent>) e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() >= 2 && hasDoubleClick) {
                    publishEvent(EventType.DOUBLE_CLICK);
                    CallbackInvoker.call(callbacksInstance, screenInstance, "doubleClick", acronym, e, getSelectedItem());
                } else if (e.getClickCount() == 1 && hasClick) {
                    publishEvent(EventType.CLICK);
                    CallbackInvoker.call(callbacksInstance, screenInstance, "click", acronym, e, getSelectedItem());
                }
            }

            if (oldHandler != null) {
                oldHandler.handle(e);
            }
        };

        listView.setOnMouseClicked(newHandler);
        unregisters.add(() -> listView.setOnMouseClicked(oldHandler));

        return unregisters;
    }

    private Object getSelectedItem() {
        return listView.getSelectionModel() != null
                ? listView.getSelectionModel().getSelectedItem()
                : null;
    }
}
