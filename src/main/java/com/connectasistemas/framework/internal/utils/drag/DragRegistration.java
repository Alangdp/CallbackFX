package com.connectasistemas.framework.internal.utils.drag;

import com.connectasistemas.framework.utils.drag.DragHandlers;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * Registro interno responsável por anexar e remover handlers de drag de uma Region.
 */
public final class DragRegistration {

    private final Region region;
    private final DragHandlers handlers;
    private final EventHandler<MouseEvent> pressWrapper = this::handlePress;
    private final EventHandler<MouseEvent> dragWrapper = this::handleDrag;
    private final EventHandler<MouseEvent> releaseWrapper = this::handleRelease;
    private boolean dragging;

    public DragRegistration(Region region, DragHandlers handlers) {
        this.region = region;
        this.handlers = handlers;
    }

    public void install() {
        region.addEventFilter(MouseEvent.MOUSE_PRESSED, pressWrapper);
        region.addEventFilter(MouseEvent.MOUSE_DRAGGED, dragWrapper);
        region.addEventFilter(MouseEvent.MOUSE_RELEASED, releaseWrapper);
    }

    public void dispose() {
        region.removeEventFilter(MouseEvent.MOUSE_PRESSED, pressWrapper);
        region.removeEventFilter(MouseEvent.MOUSE_DRAGGED, dragWrapper);
        region.removeEventFilter(MouseEvent.MOUSE_RELEASED, releaseWrapper);
    }

    private void handlePress(MouseEvent event) {
        dragging = true;
        if (handlers.onPress() != null) {
            handlers.onPress().handle(event);
        }
    }

    private void handleDrag(MouseEvent event) {
        if (!dragging) {
            return;
        }
        if (handlers.onDrag() != null) {
            handlers.onDrag().handle(event);
        }
        if (handlers.onDragUp() != null) {
            handlers.onDragUp().handle(event);
        }
    }

    private void handleRelease(MouseEvent event) {
        if (!dragging) {
            return;
        }
        dragging = false;
        if (handlers.onRelease() != null) {
            handlers.onRelease().handle(event);
        }
    }
}
