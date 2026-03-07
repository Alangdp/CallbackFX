package com.connectasistemas.framework.contract;

import com.connectasistemas.framework.internal.examples.ExampleController;
import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.internal.utils.CallbackInvoker;
import com.connectasistemas.framework.internal.utils.ScreenAssembler;
import com.connectasistemas.framework.internal.utils.ScreenHierarchyRegistry;
import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;
import com.connectasistemas.framework.internal.utils.records.ScreenView;
import com.connectasistemas.framework.core.JavaFXTestHelper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link CallbackInvoker}.
 * Camada 4 — Contrato de nomenclatura e invocação reflexiva.
 */
class CallbackInvokerTest {

    private ExampleController controller;
    private ScreenView view;

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @BeforeEach
    void setUp() {
        controller = new ExampleController();
        view = ScreenAssembler.compose(Example.class);
    }

    @AfterEach
    void tearDown() {
        ScreenManagerSharedData.resetScreenData();
        ScreenHierarchyRegistry.clear();
    }

    // ---- buildName via reflexão ----

    @Test
    @DisplayName("buildName('config', 'Example') deve retornar 'callbackConfigExample'")
    void shouldBuildNameForConfigExample() throws Exception {
        Method buildName = CallbackInvoker.class.getDeclaredMethod("buildName", String.class, String.class);
        buildName.setAccessible(true);
        String result = (String) buildName.invoke(null, "config", "Example");
        assertEquals("callbackConfigExample", result,
                "Convenção de nome deve gerar callbackConfigExample");
    }

    @Test
    @DisplayName("buildName('altcam', 'saveButton') deve retornar 'callbackAltcamSaveButton'")
    void shouldBuildNameForAltcamSaveButton() throws Exception {
        Method buildName = CallbackInvoker.class.getDeclaredMethod("buildName", String.class, String.class);
        buildName.setAccessible(true);
        String result = (String) buildName.invoke(null, "altcam", "saveButton");
        assertEquals("callbackAltcamSaveButton", result,
                "Convenção de nome deve gerar callbackAltcamSaveButton");
    }

    @Test
    @DisplayName("buildName('entcam', 'filterInput') deve retornar 'callbackEntcamFilterInput'")
    void shouldBuildNameForEntcamFilterInput() throws Exception {
        Method buildName = CallbackInvoker.class.getDeclaredMethod("buildName", String.class, String.class);
        buildName.setAccessible(true);
        String result = (String) buildName.invoke(null, "entcam", "filterInput");
        assertEquals("callbackEntcamFilterInput", result,
                "Convenção de nome deve gerar callbackEntcamFilterInput");
    }

    // ---- exists ----

    @Test
    @DisplayName("exists deve retornar true para callback existente no controller")
    void shouldReturnTrueForExistingCallback() {
        assertTrue(CallbackInvoker.exists(controller, "config", "Example"),
                "callbackConfigExample deve existir no ExampleController");
    }

    @Test
    @DisplayName("exists deve retornar true para altcam + saveButton")
    void shouldReturnTrueForAltcamSaveButton() {
        assertTrue(CallbackInvoker.exists(controller, "altcam", "saveButton"),
                "callbackAltcamSaveButton deve existir no ExampleController");
    }

    @Test
    @DisplayName("exists deve retornar false para callback inexistente")
    void shouldReturnFalseForNonExistentCallback() {
        assertFalse(CallbackInvoker.exists(controller, "click", "campoInexistente"),
                "Callback inexistente deve retornar false");
    }

    @Test
    @DisplayName("exists deve retornar false para controller nulo")
    void shouldReturnFalseForNullController() {
        assertFalse(CallbackInvoker.exists(null, "config", "Example"),
                "Controller nulo deve retornar false");
    }

    // ---- call ----

    @Test
    @DisplayName("call('config', 'Example') deve executar sem lançar exceção")
    void shouldCallConfigExampleWithoutError() {
        assertDoesNotThrow(
                () -> CallbackInvoker.call(controller, view.screenInstance(), "config", "Example"),
                "callbackConfigExample deve executar sem erro");
    }

    @Test
    @DisplayName("call com controller nulo não deve lançar exceção")
    void shouldNotThrowWhenControllerIsNull() {
        assertDoesNotThrow(
                () -> CallbackInvoker.call(null, view.screenInstance(), "config", "Example"),
                "Chamada com controller nulo deve ser silenciosa");
    }
}
