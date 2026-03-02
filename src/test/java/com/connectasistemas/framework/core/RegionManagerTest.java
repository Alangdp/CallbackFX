package com.connectasistemas.framework.core;

import com.connectasistemas.framework.internal.utils.RegionManager;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link RegionManager}.
 * Camada 1 — motor do framework.
 */
class RegionManagerTest {

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    // ---- Cenário 1: createRegion(BorderPane.class) ----

    @Test
    @DisplayName("createRegion(BorderPane.class) deve retornar BorderPane")
    void shouldCreateBorderPane() {
        Region region = RegionManager.createRegion(BorderPane.class);
        assertInstanceOf(BorderPane.class, region, "Deve retornar BorderPane");
    }

    // ---- Cenário 2: createRegion(VBox.class) ----

    @Test
    @DisplayName("createRegion(VBox.class) deve retornar VBox")
    void shouldCreateVBox() {
        Region region = RegionManager.createRegion(VBox.class);
        assertInstanceOf(VBox.class, region, "Deve retornar VBox");
    }

    // ---- Cenário 3: createRegion(HBox.class) ----

    @Test
    @DisplayName("createRegion(HBox.class) deve retornar HBox")
    void shouldCreateHBox() {
        Region region = RegionManager.createRegion(HBox.class);
        assertInstanceOf(HBox.class, region, "Deve retornar HBox");
    }

    // ---- Cenário 4: tipo não registrado lança exceção ----

    @Test
    @DisplayName("createRegion com tipo não registrado deve lançar RuntimeException")
    void shouldThrowForUnregisteredType() {
        assertThrows(RuntimeException.class,
                () -> RegionManager.createRegion(javafx.scene.canvas.Canvas.class),
                "Tipo não registrado deve lançar exceção");
    }
}
