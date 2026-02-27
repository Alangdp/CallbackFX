package com.connectasistemas.framework.utils.drag;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Container com os handlers usados durante o ciclo de drag.
 * "onDragUp" é executado continuamente enquanto o drag estiver ativo.
 */
public class DragHandlers {
    private final EventHandler<MouseEvent> onPress;
    private final EventHandler<MouseEvent> onDrag;
    private final EventHandler<MouseEvent> onRelease;
    private final EventHandler<MouseEvent> onDragUp;
    private final EventHandler<MouseEvent> onDragDetected;

    private DragHandlers(Builder builder) {
        this.onPress = builder.onPress;
        this.onDrag = builder.onDrag;
        this.onRelease = builder.onRelease;
        this.onDragUp = builder.onDragUp;
        this.onDragDetected = builder.onDragDetected;
    }
    /**
     * Handler para o evento DRAG_DETECTED do JavaFX.
     */
    public EventHandler<MouseEvent> onDragDetected() {
        return onDragDetected;
    }

    public EventHandler<MouseEvent> onPress() {
        return onPress;
    }

    public EventHandler<MouseEvent> onDrag() {
        return onDrag;
    }

    public EventHandler<MouseEvent> onRelease() {
        return onRelease;
    }

    public EventHandler<MouseEvent> onDragUp() {
        return onDragUp;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder para facilitar o registro dos handlers opcionais.
     */
    public static final class Builder {
        private EventHandler<MouseEvent> onPress;
        private EventHandler<MouseEvent> onDrag;
        private EventHandler<MouseEvent> onRelease;
        private EventHandler<MouseEvent> onDragUp;
        private EventHandler<MouseEvent> onDragDetected;
        /**
         * Define o handler para DRAG_DETECTED.
         */
        public Builder onDragDetected(EventHandler<MouseEvent> handler) {
            this.onDragDetected = handler;
            return this;
        }

        private Builder() {
        }

        public Builder onPress(EventHandler<MouseEvent> handler) {
            this.onPress = handler;
            return this;
        }

        public Builder onDrag(EventHandler<MouseEvent> handler) {
            this.onDrag = handler;
            return this;
        }

        public Builder onRelease(EventHandler<MouseEvent> handler) {
            this.onRelease = handler;
            return this;
        }

        public Builder onDragUp(EventHandler<MouseEvent> handler) {
            this.onDragUp = handler;
            return this;
        }

        public DragHandlers build() {
            return new DragHandlers(this);
        }
    }
}
