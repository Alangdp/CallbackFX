package com.connectasistemas.framework.utils.events;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.interfaces.EventBinderEvents;
import com.connectasistemas.framework.utils.CallbackInvoker;

/**
 * Binder de eventos para {@link TableColumn} editáveis.
 */
public class EventBinderTableColumn extends EventBinderEvents {

    private final String acronym;
    private final TableColumn<?, ?> tableColumn;
    private final Object screenInstance;
    private final Object callbacksInstance;

    public EventBinderTableColumn(String acronym,
                                  TableColumn<?, ?> tableColumn,
                                  Object screenInstance,
                                  Object callbacksInstance) {
        this.acronym = acronym;
        this.tableColumn = tableColumn;
        this.screenInstance = screenInstance;
        this.callbacksInstance = callbacksInstance;
    }

    @Override
    public List<Runnable> applyEntcamSaicamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        boolean hasEntcam = CallbackInvoker.exists(callbacksInstance, "entcam", acronym);
        boolean hasSaicam = CallbackInvoker.exists(callbacksInstance, "saicam", acronym);

        @SuppressWarnings("rawtypes")
        TableColumn rawColumn = (TableColumn) tableColumn;

        if (hasEntcam) {
            @SuppressWarnings("unchecked")
            EventHandler<TableColumn.CellEditEvent> oldStart = rawColumn.getOnEditStart();

            EventHandler<TableColumn.CellEditEvent> newStart = event -> {
                publishEvent(EventType.ENTCAM);
                CallbackInvoker.call(callbacksInstance, screenInstance, "entcam", acronym, event);

                if (oldStart != null) {
                    oldStart.handle(event);
                }
            };

            rawColumn.setOnEditStart(newStart);
            unregisters.add(() -> rawColumn.setOnEditStart(oldStart));
        }

        if (hasSaicam) {
            @SuppressWarnings("unchecked")
            EventHandler<TableColumn.CellEditEvent> oldCancel = rawColumn.getOnEditCancel();

            EventHandler<TableColumn.CellEditEvent> newCancel = event -> {
                publishEvent(EventType.SAICAM);
                CallbackInvoker.call(callbacksInstance, screenInstance, "saicam", acronym, event);

                if (oldCancel != null) {
                    oldCancel.handle(event);
                }
            };

            rawColumn.setOnEditCancel(newCancel);
            unregisters.add(() -> rawColumn.setOnEditCancel(oldCancel));
        }

        return unregisters;
    }

    @Override
    public List<Runnable> applyAltcamEvent() {
        List<Runnable> unregisters = new ArrayList<>();

        boolean hasAltcam = CallbackInvoker.exists(callbacksInstance, "altcam", acronym);
        boolean hasSaicam = CallbackInvoker.exists(callbacksInstance, "saicam", acronym);

        if (!hasAltcam && !hasSaicam) {
            return unregisters;
        }

        @SuppressWarnings("rawtypes")
        TableColumn rawColumn = (TableColumn) tableColumn;

        @SuppressWarnings("unchecked")
        EventHandler<TableColumn.CellEditEvent> oldCommit = rawColumn.getOnEditCommit();

        EventHandler<TableColumn.CellEditEvent> newCommit = event -> {
            if (hasAltcam) {
                publishEvent(EventType.ALTCAM);
                CallbackInvoker.call(callbacksInstance, screenInstance, "altcam", acronym, event);
            }

            if (hasSaicam) {
                publishEvent(EventType.SAICAM);
                CallbackInvoker.call(callbacksInstance, screenInstance, "saicam", acronym, event);
            }

            if (oldCommit != null) {
                oldCommit.handle(event);
            }
        };

        rawColumn.setOnEditCommit(newCommit);
        unregisters.add(() -> rawColumn.setOnEditCommit(oldCommit));
        return unregisters;
    }

    @Override
    public List<Runnable> applyTecladEvent() {
        return new ArrayList<>();
    }

    @Override
    public List<Runnable> applyCustomEvents() {
        return new ArrayList<>();
    }
}
