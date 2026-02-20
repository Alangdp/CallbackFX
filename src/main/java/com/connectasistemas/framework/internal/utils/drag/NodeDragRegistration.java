package com.connectasistemas.framework.internal.utils.drag;

import com.connectasistemas.framework.utils.drag.DragHandlers;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

/**
 * Registro interno responsável por anexar e remover handlers de drag de um {@link Node} genérico.
 *
 * PRESS é capturado no próprio node.
 * DRAG e RELEASE são capturados na Scene para garantir recebimento mesmo quando
 * o mouse sai da área do node — problema comum em TreeCell e outros controls.
 *
 * O registro na Scene é feito de forma lazy: aguarda a célula entrar na Scene,
 * pois no momento do setCellFactory o node ainda não está no grafo de cena.
 */
public final class NodeDragRegistration {

    private final Node node;
    private final DragHandlers handlers;
    private final EventHandler<MouseEvent> pressWrapper   = this::handlePress;
    private final EventHandler<MouseEvent> dragWrapper    = this::handleDrag;
    private final EventHandler<MouseEvent> releaseWrapper = this::handleRelease;
    private boolean dragging;

    // Guardamos a referência para poder remover depois
    private final ChangeListener<Scene> sceneListener;

    public NodeDragRegistration(Node node, DragHandlers handlers) {
        this.node     = node;
        this.handlers = handlers;

        this.sceneListener = (obs, oldScene, newScene) -> {
            unregisterFromScene(oldScene);
            registerInScene(newScene);
        };
    }

    public void install() {
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, pressWrapper);

        // Listener garante que quando a célula entrar na Scene (lazy),
        // os handlers de drag/release sejam registrados corretamente
        node.sceneProperty().addListener(sceneListener);

        // Se por acaso já estiver na Scene, registra imediatamente
        if (node.getScene() != null) {
            registerInScene(node.getScene());
        }
    }

    public void dispose() {
        node.removeEventFilter(MouseEvent.MOUSE_PRESSED, pressWrapper);
        node.sceneProperty().removeListener(sceneListener);
        unregisterFromScene(node.getScene());
    }

    // ------------------------------------------------------------------

    private void registerInScene(Scene scene) {
        if (scene == null) return;
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED,  dragWrapper);
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, releaseWrapper);
    }

    private void unregisterFromScene(Scene scene) {
        if (scene == null) return;
        scene.removeEventFilter(MouseEvent.MOUSE_DRAGGED,  dragWrapper);
        scene.removeEventFilter(MouseEvent.MOUSE_RELEASED, releaseWrapper);
    }

    // ------------------------------------------------------------------

    private void handlePress(MouseEvent event) {
        dragging = true;
        if (handlers.onPress() != null) {
            handlers.onPress().handle(event);
        }
    }

    private void handleDrag(MouseEvent event) {
        if (!dragging) return;
        if (handlers.onDrag() != null) {
            handlers.onDrag().handle(event);
        }
        if (handlers.onDragUp() != null) {
            handlers.onDragUp().handle(event);
        }
    }

    private void handleRelease(MouseEvent event) {
        if (!dragging) return;
        dragging = false;
        if (handlers.onRelease() != null) {
            handlers.onRelease().handle(event);
        }
    }
}