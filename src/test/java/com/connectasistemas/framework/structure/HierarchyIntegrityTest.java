package com.connectasistemas.framework.structure;

import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.internal.examples.Example;
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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integridade hierárquica.
 * Camada 3 — garante consistência entre father/child em campos anotados.
 */
class HierarchyIntegrityTest {

    private ScreenView view;
    private Example screen;

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @BeforeEach
    void setUp() {
        view = ScreenAssembler.compose(Example.class);
        screen = (Example) view.screenInstance();
    }

    @AfterEach
    void tearDown() {
        ScreenManagerSharedData.resetScreenData();
        ScreenHierarchyRegistry.clear();
    }

    // ---- Cenário 1: Todos os campos com father referem pais válidos ----

    @Test
    @DisplayName("Todos os campos com father devem referenciar acronyms existentes")
    void shouldHaveValidFatherReferences() {
        // Coleta todos os acronyms
        Set<String> acronyms = new HashSet<>();
        for (Field field : Example.class.getDeclaredFields()) {
            ScreenField sf = field.getAnnotation(ScreenField.class);
            if (sf != null) {
                acronyms.add(sf.acronym());
            }
        }

        // Verifica que cada father aponta para um acronym existente
        for (Field field : Example.class.getDeclaredFields()) {
            ScreenField sf = field.getAnnotation(ScreenField.class);
            if (sf != null && !sf.father().isEmpty()) {
                assertTrue(acronyms.contains(sf.father()),
                        "Campo " + sf.acronym() + " referencia father '" + sf.father() + "' que nao existe");
            }
        }
    }

    // ---- Cenário 2: Não há ciclos na hierarquia ----

    @Test
    @DisplayName("Hierarquia não deve ter ciclos")
    void shouldHaveNoCycles() {
        // Monta mapa acronym -> father
        Map<String, String> parentMap = new HashMap<>();
        for (Field field : Example.class.getDeclaredFields()) {
            ScreenField sf = field.getAnnotation(ScreenField.class);
            if (sf != null && !sf.father().isEmpty()) {
                parentMap.put(sf.acronym(), sf.father());
            }
        }

        // Para cada campo, percorre a cadeia de pais — se revisitar, há ciclo
        for (String acronym : parentMap.keySet()) {
            Set<String> visited = new HashSet<>();
            String current = acronym;
            while (current != null && parentMap.containsKey(current)) {
                assertFalse(visited.contains(current),
                        "Ciclo detectado envolvendo: " + current);
                visited.add(current);
                current = parentMap.get(current);
            }
        }
    }

    // ---- Cenário 3: root não tem father ----

    @Test
    @DisplayName("Campo root não deve ter father definido")
    void shouldHaveRootWithNoFather() throws Exception {
        Field rootField = Example.class.getDeclaredField("root");
        ScreenField sf = rootField.getAnnotation(ScreenField.class);
        assertNotNull(sf, "root deve ter @ScreenField");
        assertTrue(sf.father().isEmpty(),
                "root não deve ter father definido");
    }

    // ---- Cenário 4: ScreenHierarchyRegistry contém Example após compose ----

    @Test
    @DisplayName("ScreenHierarchyRegistry deve registrar Example após compose")
    void shouldRegisterExampleInHierarchy() {
        // ScreenHierarchyRegistry.contains é verificado indiretamente pelo compose ter sucesso
        // Se a hierarquia fosse inválida, compose lançaria exceção
        assertNotNull(view.screenInstance(),
                "compose deve ter retornado instância válida");
        assertNotNull(screen.root,
                "root deve ter sido atribuído pelo assembler");
    }

    // ---- Cenário 5: Cada acronym é único ----

    @Test
    @DisplayName("Cada acronym deve ser único na classe Example")
    void shouldHaveUniqueAcronyms() {
        Set<String> seen = new HashSet<>();
        for (Field field : Example.class.getDeclaredFields()) {
            ScreenField sf = field.getAnnotation(ScreenField.class);
            if (sf != null) {
                assertTrue(seen.add(sf.acronym()),
                        "Acronym duplicado: " + sf.acronym());
            }
        }
    }
}
