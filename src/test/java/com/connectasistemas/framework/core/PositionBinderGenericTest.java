package com.connectasistemas.framework.core;

import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.internal.position.PositionBinderGeneric;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link PositionBinderGeneric}.
 * Camada 1 — motor do framework.
 */
class PositionBinderGenericTest {

    private PositionBinderGeneric binder;

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @BeforeEach
    void setUp() {
        binder = new PositionBinderGeneric();
    }

    // ---- Cenário 1: alignment = LEFT em VBox → Pos.CENTER_LEFT ----

    @Test
    @DisplayName("alignment LEFT em VBox deve resultar em Pos.CENTER_LEFT")
    void shouldApplyCenterLeftToVBox() {
        VBox vbox = new VBox();
        ScreenFieldPosition position = createPosition(Position.LEFT);

        binder.applyAll(position, vbox);

        assertEquals(Pos.CENTER_LEFT, vbox.getAlignment(),
                "VBox.getAlignment() deve ser CENTER_LEFT");
    }

    // ---- Cenário 2: alignment = CENTER em StackPane ----

    @Test
    @DisplayName("alignment CENTER em StackPane deve resultar em Pos.CENTER")
    void shouldApplyCenterToStackPane() {
        StackPane stackPane = new StackPane();
        ScreenFieldPosition position = createPosition(Position.CENTER);

        binder.applyAll(position, stackPane);

        assertEquals(Pos.CENTER, stackPane.getAlignment(),
                "StackPane.getAlignment() deve ser CENTER");
    }

    // ---- Cenário extra: alignment RIGHT em HBox ----

    @Test
    @DisplayName("alignment RIGHT em HBox deve resultar em Pos.CENTER_RIGHT")
    void shouldApplyCenterRightToHBox() {
        HBox hbox = new HBox();
        ScreenFieldPosition position = createPosition(Position.RIGHT);

        binder.applyAll(position, hbox);

        assertEquals(Pos.CENTER_RIGHT, hbox.getAlignment(),
                "HBox.getAlignment() deve ser CENTER_RIGHT");
    }

    // ---- Cenário: null não quebra ----

    @Test
    @DisplayName("applyAll com null não deve lançar exceção")
    void shouldHandleNull() {
        assertDoesNotThrow(() -> binder.applyAll(null, new VBox()),
                "applyAll com position null não deve lançar exceção");
        assertDoesNotThrow(() -> binder.applyAll(createPosition(Position.CENTER), null),
                "applyAll com node null não deve lançar exceção");
    }

    /**
     * Cria uma instância anônima de ScreenFieldPosition com o alignment informado.
     */
    private ScreenFieldPosition createPosition(Position alignment) {
        return new ScreenFieldPosition() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ScreenFieldPosition.class;
            }

            @Override
            public Position alignment() {
                return alignment;
            }
        };
    }
}
