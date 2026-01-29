package com.connectasistemas.framework.internal.events;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.internal.interfaces.EventBinderEvents;
import com.connectasistemas.framework.internal.utils.CallbackInvoker;
import com.connectasistemas.framework.utils.Status;

/**
 * Binder de eventos para {@link TableView}.
 */
public class EventBinderTableView extends EventBinderEvents {

    private final String acronym;
    private final TableView<?> tableView;
    private final Object screenInstance;
    private final Object callbacksInstance;

    public EventBinderTableView(String acronym,
                                TableView<?> tableView,
                                Object screenInstance,
                                Object callbacksInstance) {
        this.acronym = acronym;
        this.tableView = tableView;
        this.screenInstance = screenInstance;
        this.callbacksInstance = callbacksInstance;
    }

    @Override
    public List<Runnable> applyEntcamSaicamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        unregisters.add(registerNavigationTracker(tableView));

        boolean hasEntcam = CallbackInvoker.exists(callbacksInstance, "entcam", acronym);
        boolean hasSaicam = CallbackInvoker.exists(callbacksInstance, "saicam", acronym);

        ChangeListener<Boolean> focusListener = (obs, oldFocused, newFocused) -> {
            if (Boolean.TRUE.equals(newFocused)) {
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
                focusIfError(tableView);
            }

            clearExitReason();
        };

        tableView.focusedProperty().addListener(focusListener);
        unregisters.add(() -> tableView.focusedProperty().removeListener(focusListener));

        return unregisters;
    }

    @Override
    public List<Runnable> applyAltcamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (!CallbackInvoker.exists(callbacksInstance, "altcam", acronym)) {
            return unregisters;
        }

        if (tableView.getSelectionModel() == null) {
            return unregisters;
        }

        ChangeListener<Object> selectionListener = (obs, oldValue, newValue) -> {
            publishEvent(EventType.ALTCAM);
            CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym);
        };

        tableView.getSelectionModel().selectedItemProperty().addListener(selectionListener);
        unregisters.add(() -> tableView.getSelectionModel().selectedItemProperty().removeListener(selectionListener));

        return unregisters;
    }

    @Override
    public List<Runnable> applyTecladEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        if (!CallbackInvoker.exists(callbacksInstance, "teclad", acronym)) {
            return unregisters;
        }

        EventHandler<? super KeyEvent> oldHandler = tableView.getOnKeyPressed();

        EventHandler<KeyEvent> newHandler = event -> {
            publishEvent(EventType.TECLAD);
            CallbackInvoker.call(callbacksInstance, screenInstance, "teclad", acronym, event);

            if (oldHandler != null) {
                oldHandler.handle(event);
            }
        };

        tableView.setOnKeyPressed(newHandler);
        unregisters.add(() -> tableView.setOnKeyPressed(oldHandler));
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

        EventHandler<? super MouseEvent> oldHandler = tableView.getOnMouseClicked();

        EventHandler<MouseEvent> newHandler = event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Object selectedRow = getSelectedRow();
                TablePosition<?, ?> selectedCell = getSelectedCell();

                if (event.getClickCount() >= 2 && hasDoubleClick) {
                    publishEvent(EventType.DOUBLE_CLICK);
                    CallbackInvoker.call(callbacksInstance, screenInstance, "doubleClick", acronym, event, selectedRow, selectedCell);
                } else if (event.getClickCount() == 1 && hasClick) {
                    publishEvent(EventType.CLICK);
                    CallbackInvoker.call(callbacksInstance, screenInstance, "click", acronym, event, selectedRow, selectedCell);
                }
            }

            if (oldHandler != null) {
                oldHandler.handle(event);
            }
        };

        tableView.setOnMouseClicked(newHandler);
        unregisters.add(() -> tableView.setOnMouseClicked(oldHandler));
        return unregisters;
    }

    private Object getSelectedRow() {
        return tableView.getSelectionModel() != null
                ? tableView.getSelectionModel().getSelectedItem()
                : null;
    }

    private TablePosition<?, ?> getSelectedCell() {
        if (tableView.getSelectionModel() == null) {
            return null;
        }

        var cells = tableView.getSelectionModel().getSelectedCells();
        if (cells == null || cells.isEmpty()) {
            return null;
        }

        return cells.get(0);
    }
}
