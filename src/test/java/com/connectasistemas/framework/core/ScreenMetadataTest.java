package com.connectasistemas.framework.core;

import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.internal.processor.AnnotationProcessor;
import com.connectasistemas.framework.internal.utils.ScreenMetadata;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link ScreenMetadata}.
 * Camada 1 — motor do framework.
 */
class ScreenMetadataTest {

    private ScreenMetadata metadata;

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @BeforeEach
    void setUp() {
        metadata = new ScreenMetadata();
    }

    // ---- Cenário 1: getFields() retorna mapa ordenado por hierarquia pai-filho ----

    @Test
    @DisplayName("getFields deve retornar campos na ordem hierárquica (pais antes de filhos)")
    void shouldReturnFieldsInHierarchicalOrder() {
        AnnotationProcessor processor = new AnnotationProcessor();
        ScreenMetadata exampleMetadata = processor.processScreen(Example.class);

        Map<String, Field> fields = exampleMetadata.getFields();
        List<String> keys = new ArrayList<>(fields.keySet());

        // featureRoot é filho de featureTree, deve vir depois
        int featureTreeIndex = keys.indexOf("featureTree");
        int featureRootIndex = keys.indexOf("featureRoot");
        assertTrue(featureTreeIndex < featureRootIndex,
                "featureTree deve aparecer antes de featureRoot");

        // layoutNode é filho de featureRoot, deve vir depois
        int layoutNodeIndex = keys.indexOf("layoutNode");
        assertTrue(featureRootIndex < layoutNodeIndex,
                "featureRoot deve aparecer antes de layoutNode");
    }

    // ---- Cenário 2: getFields() respeita order entre irmãos ----

    @Test
    @DisplayName("getFields deve respeitar o atributo order entre irmãos")
    void shouldRespectOrderAmongSiblings() {
        AnnotationProcessor processor = new AnnotationProcessor();
        ScreenMetadata exampleMetadata = processor.processScreen(Example.class);

        Map<String, Field> fields = exampleMetadata.getFields();
        List<String> keys = new ArrayList<>(fields.keySet());

        // Os filhos de featureRoot devem seguir a ordem: layoutNode(1), eventsNode(2), ...
        int layoutIdx = keys.indexOf("layoutNode");
        int eventsIdx = keys.indexOf("eventsNode");
        int validationIdx = keys.indexOf("validationNode");
        int tablesIdx = keys.indexOf("tablesNode");
        int treesIdx = keys.indexOf("treesNode");
        int navIdx = keys.indexOf("navigationNode");
        int utilsIdx = keys.indexOf("utilsNode");

        assertTrue(layoutIdx < eventsIdx, "layoutNode(order=1) deve vir antes de eventsNode(order=2)");
        assertTrue(eventsIdx < validationIdx, "eventsNode(order=2) deve vir antes de validationNode(order=3)");
        assertTrue(validationIdx < tablesIdx, "validationNode(order=3) deve vir antes de tablesNode(order=4)");
        assertTrue(tablesIdx < treesIdx, "tablesNode(order=4) deve vir antes de treesNode(order=5)");
        assertTrue(treesIdx < navIdx, "treesNode(order=5) deve vir antes de navigationNode(order=6)");
        assertTrue(navIdx < utilsIdx, "navigationNode(order=6) deve vir antes de utilsNode(order=7)");
    }

    // ---- Cenário 3: overrideRoot substitui a raiz ----

    @Test
    @DisplayName("overrideRoot deve substituir a raiz e retornar a mesma instância")
    void shouldOverrideRoot() {
        VBox novaRaiz = new VBox();
        metadata.overrideRoot(novaRaiz);

        assertSame(novaRaiz, metadata.root(),
                "overrideRoot deve retornar a raiz injetada");
    }

    @Test
    @DisplayName("overrideRoot com null deve lançar exceção")
    void shouldThrowWhenOverrideRootNull() {
        assertThrows(IllegalArgumentException.class,
                () -> metadata.overrideRoot(null),
                "overrideRoot(null) deve lançar exceção");
    }

    // ---- Cenário 4: setTitle / getTitle roundtrip ----

    @Test
    @DisplayName("setTitle e getTitle devem preservar o valor")
    void shouldPreserveTitleRoundtrip() {
        metadata.setTitle("Tela de Teste");
        assertEquals("Tela de Teste", metadata.getTitle(),
                "getTitle deve retornar o valor definido por setTitle");
    }

    // ---- Cenário 5: addField com acronym duplicado lança exceção ----

    @Test
    @DisplayName("addField com acronym duplicado deve lançar RuntimeException")
    void shouldThrowWhenDuplicateAcronym() throws NoSuchFieldException {
        // Usa um campo qualquer como dummy
        Field dummyField = DummyClass.class.getDeclaredField("campo1");

        metadata.addField("duplicado", dummyField);

        assertThrows(RuntimeException.class,
                () -> metadata.addField("duplicado", dummyField),
                "addField com acronym duplicado deve lançar exceção");
    }

    // ---- Cenário: setRoot cria Region a partir do tipo ----

    @Test
    @DisplayName("setRoot com BorderPane.class deve criar uma raiz do tipo BorderPane")
    void shouldCreateRootFromType() {
        metadata.setRoot(BorderPane.class);

        assertNotNull(metadata.root(), "Root não deve ser nulo após setRoot");
        assertInstanceOf(BorderPane.class, metadata.root(),
                "Root deve ser do tipo BorderPane");
    }

    // ---- Cenário: width/height roundtrip ----

    @Test
    @DisplayName("setWidth e setHeight devem preservar os valores")
    void shouldPreserveWidthAndHeight() {
        metadata.setWidth(1024);
        metadata.setHeight(768);

        assertEquals(1024, metadata.getWidth(), "Width deve ser 1024");
        assertEquals(768, metadata.getHeight(), "Height deve ser 768");
    }

    // ---- Cenário: callbackInstance roundtrip ----

    @Test
    @DisplayName("setCallbackInstance e callbackInstance devem preservar a referência")
    void shouldPreserveCallbackInstance() {
        Object controller = new Object();
        metadata.setCallbackInstance(controller);

        assertSame(controller, metadata.callbackInstance(),
                "callbackInstance deve retornar a mesma referência definida");
    }

    // Classe auxiliar para testes de addField
    static class DummyClass {
        @ScreenField(acronym = "campo1")
        public javafx.scene.control.Button campo1;
    }
}
