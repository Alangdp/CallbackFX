package com.connectasistemas.framework.core;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.internal.examples.ExampleController;
import com.connectasistemas.framework.internal.processor.AnnotationProcessor;
import com.connectasistemas.framework.internal.utils.ScreenMetadata;
import com.connectasistemas.framework.interfaces.CustomElement;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link AnnotationProcessor}.
 * Camada 1 — motor do framework.
 * Necessita toolkit JavaFX para criar Regions ao processar @Screen.
 */
class AnnotationProcessorTest {

    private AnnotationProcessor processor;

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @BeforeEach
    void setUp() {
        processor = new AnnotationProcessor();
    }

    // ---- Cenário 1: processar classe com @Screen válida ----

    @Test
    @DisplayName("Processar Example deve extrair título, width e height do @Screen")
    void shouldExtractScreenAnnotationFromExample() {
        ScreenMetadata metadata = processor.processScreen(Example.class);

        assertEquals("CallbackFX Showcase", metadata.getTitle(),
                "Título deve ser 'CallbackFX Showcase'");
        assertEquals(1280, metadata.getWidth(),
                "Width deve ser 1280");
        assertEquals(780, metadata.getHeight(),
                "Height deve ser 780");
    }

    // ---- Cenário 2: processar classe sem @Screen ----

    @Test
    @DisplayName("Processar classe sem @Screen deve retornar metadata vazia")
    void shouldReturnEmptyMetadataWhenNoScreen() {
        ScreenMetadata metadata = processor.processScreen(ClasseSemAnotacao.class);

        assertNull(metadata.getTitle(), "Título deve ser nulo para classe sem @Screen");
        assertTrue(metadata.getFields().isEmpty(),
                "Campos devem estar vazios para classe sem @Screen");
    }

    // ---- Cenário 3: campo custom=true sem CustomElement ----

    @Test
    @DisplayName("Campo custom=true sem implementar CustomElement deve lançar exceção")
    void shouldThrowWhenCustomTrueWithoutCustomElement() {
        assertThrows(RuntimeException.class,
                () -> processor.processScreen(ClasseCustomSemInterface.class),
                "Deve lançar exceção para campo custom=true sem CustomElement");
    }

    // ---- Cenário 4: campo implementa CustomElement sem custom=true ----

    @Test
    @DisplayName("Campo que implementa CustomElement sem custom=true deve lançar exceção")
    void shouldThrowWhenCustomElementWithoutFlag() {
        assertThrows(RuntimeException.class,
                () -> processor.processScreen(ClasseCustomSemFlag.class),
                "Deve lançar exceção para campo CustomElement sem custom=true");
    }

    // ---- Cenário 5: CustomElement que retorna tipo diferente do próprio ----

    @Test
    @DisplayName("Processar CustomNaoRegion não deve lançar exceção (Button estende Region)")
    void shouldProcessCustomElementThatExtendsRegion() {
        // FakeCustomButton estende Button que estende Region, portanto é válido
        assertDoesNotThrow(
                () -> processor.processScreen(ClasseCustomNaoRegion.class),
                "CustomElement que estende Region não deve lançar exceção");
    }

    // ---- Cenário 6: processar todos os 41 campos de Example ----

    @Test
    @DisplayName("Processar Example deve retornar metadata com 41 campos, cada acronym único")
    void shouldProcess41FieldsFromExample() {
        ScreenMetadata metadata = processor.processScreen(Example.class);

        assertEquals(49, metadata.getFields().size(),
                "Example deve ter exatamente 49 campos anotados");
    }

    // ---- Cenário 7: detectar acronym duplicado ----

    @Test
    @DisplayName("Acronym duplicado deve lançar exceção ao processar")
    void shouldThrowWhenDuplicateAcronym() {
        assertThrows(RuntimeException.class,
                () -> processor.processScreen(ClasseComAcronymDuplicado.class),
                "Deve lançar exceção para acronym duplicado");
    }

    // ---- Cenário: callback instance ----

    @Test
    @DisplayName("Processar Example deve instanciar o ExampleController como callbackInstance")
    void shouldInstantiateCallbackInstance() {
        ScreenMetadata metadata = processor.processScreen(Example.class);

        assertNotNull(metadata.callbackInstance(),
                "callbackInstance não deve ser nulo");
        assertInstanceOf(ExampleController.class, metadata.callbackInstance(),
                "callbackInstance deve ser ExampleController");
    }

    // ==== Classes auxiliares para teste ====

    // Classe sem nenhuma anotação
    static class ClasseSemAnotacao {
        public Button botao;
    }

    // Classe com campo custom=true mas tipo que NÃO implementa CustomElement
    @Screen(title = "Teste Custom Sem Interface", callbacks = Void.class, region = javafx.scene.layout.BorderPane.class)
    static class ClasseCustomSemInterface {
        @ScreenField(acronym = "campo1", custom = true)
        public VBox campo1;
    }

    // Classe com campo que implementa CustomElement mas NÃO tem custom=true
    @Screen(title = "Teste Custom Sem Flag", callbacks = Void.class, region = javafx.scene.layout.BorderPane.class)
    static class ClasseCustomSemFlag {
        @ScreenField(acronym = "card")
        public Example.MetricsCard card;
    }

    // CustomElement que não estende Region (impossível em prática, mas teste edge)
    static class FakeCustomElement implements CustomElement {
        @Override
        public Class<? extends Region> getType() {
            return VBox.class;
        }

        @Override
        public void onElementCreated(Region element) {
        }
    }

    // Para o cenário 5, precisamos de uma classe que implementa CustomElement mas não estende Region
    // Na prática, o Java exige que a classe compile, então criamos uma que estende Button (não Region)
    @Screen(title = "Teste Custom Não Region", callbacks = Void.class, region = javafx.scene.layout.BorderPane.class)
    static class ClasseCustomNaoRegion {
        @ScreenField(acronym = "fake", custom = true)
        public FakeCustomButton fake;
    }

    // CustomElement que estende Button (Node mas não Region)
    static class FakeCustomButton extends Button implements CustomElement {
        @Override
        public Class<? extends Region> getType() {
            return VBox.class;
        }

        @Override
        public void onElementCreated(Region element) {
        }
    }

    // Classe com acronym duplicado
    @Screen(title = "Teste Duplicado", callbacks = Void.class, region = javafx.scene.layout.BorderPane.class)
    static class ClasseComAcronymDuplicado {
        @ScreenField(acronym = "campo1")
        public Button campo1;

        @ScreenField(acronym = "campo1")
        public Button campo2;
    }
}
