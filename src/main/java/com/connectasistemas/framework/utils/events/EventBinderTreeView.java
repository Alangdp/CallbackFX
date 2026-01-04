package com.connectasistemas.framework.utils.events;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.interfaces.EventBinderEvents;
import com.connectasistemas.framework.utils.CallbackInvoker;
import com.connectasistemas.framework.utils.Status;

/**
 * Binder de eventos para {@link TreeView}.
 */
public class EventBinderTreeView extends EventBinderEvents {

    private final String acronym;
    private final TreeView<?> treeView;
    private final Object screenInstance;
    private final Object callbacksInstance;

    public EventBinderTreeView(String acronym,
                               TreeView<?> treeView,
                               Object screenInstance,
                               Object callbacksInstance) {
        this.acronym = acronym;
        this.treeView = treeView;
        this.screenInstance = screenInstance;
        this.callbacksInstance = callbacksInstance;
    }

    @Override
    public List<Runnable> applyEntcamSaicamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        unregisters.add(registerNavigationTracker(treeView));

        boolean hasEntcam = CallbackInvoker.exists(callbacksInstance, "entcam", acronym);
        boolean hasSaicam = CallbackInvoker.exists(callbacksInstance, "saicam", acronym);

        ChangeListener<Boolean> focusListener = (obs, oldV, newV) -> {
            if (Boolean.TRUE.equals(newV)) {
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
                focusIfError(treeView);
            }

            clearExitReason();
        };

        treeView.focusedProperty().addListener(focusListener);
        unregisters.add(() -> treeView.focusedProperty().removeListener(focusListener));

        return unregisters;
    }

    @Override
    public List<Runnable> applyAltcamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (!CallbackInvoker.exists(callbacksInstance, "altcam", acronym)) {
            return unregisters;
        }

        if (treeView.getSelectionModel() == null) {
            return unregisters;
        }

        ChangeListener<TreeItem<?>> listener = (obs, oldItem, newItem) -> {
            if (newItem == null) {
                return;
            }

            publishEvent(EventType.ALTCAM);
            CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym);
        };

        treeView.getSelectionModel().selectedItemProperty().addListener(listener);
        unregisters.add(() -> treeView.getSelectionModel().selectedItemProperty().removeListener(listener));
        return unregisters;
    }

    @Override
    public List<Runnable> applyTecladEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (!CallbackInvoker.exists(callbacksInstance, "teclad", acronym)) {
            return unregisters;
        }

        @SuppressWarnings("unchecked")
        EventHandler<KeyEvent> oldHandler = (EventHandler<KeyEvent>) treeView.getOnKeyPressed();

        EventHandler<KeyEvent> newHandler = e -> {
            publishEvent(EventType.TECLAD);
            CallbackInvoker.call(callbacksInstance, screenInstance, "teclad", acronym, e);

            if (oldHandler != null) {
                oldHandler.handle(e);
            }
        };

        treeView.setOnKeyPressed(newHandler);
        unregisters.add(() -> treeView.setOnKeyPressed(oldHandler));
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

        @SuppressWarnings("unchecked")
        EventHandler<MouseEvent> oldHandler = (EventHandler<MouseEvent>) treeView.getOnMouseClicked();

        EventHandler<MouseEvent> newHandler = e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                TreeItem<?> selected = getSelectedItem();

                if (e.getClickCount() >= 2 && hasDoubleClick) {
                    publishEvent(EventType.DOUBLE_CLICK);
                    CallbackInvoker.call(callbacksInstance, screenInstance, "doubleClick", acronym, e, selected);
                } else if (e.getClickCount() == 1 && hasClick) {
                    publishEvent(EventType.CLICK);
                    CallbackInvoker.call(callbacksInstance, screenInstance, "click", acronym, e, selected);
                }
            }

            if (oldHandler != null) {
                oldHandler.handle(e);
            }
        };

        treeView.setOnMouseClicked(newHandler);
        unregisters.add(() -> treeView.setOnMouseClicked(oldHandler));
        return unregisters;
    }

    private TreeItem<?> getSelectedItem() {
        return treeView.getSelectionModel() != null
                ? treeView.getSelectionModel().getSelectedItem()
                : null;
    }
}
