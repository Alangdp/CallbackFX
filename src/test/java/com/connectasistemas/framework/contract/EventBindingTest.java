package com.connectasistemas.framework.contract;

import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.internal.utils.EventBinder;
import com.connectasistemas.framework.internal.utils.ScreenAssembler;
import com.connectasistemas.framework.internal.utils.ScreenHierarchyRegistry;
import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;
import com.connectasistemas.framework.internal.utils.records.ScreenView;
import com.connectasistemas.framework.utils.ScreenRuntimeContext;
import com.connectasistemas.framework.core.JavaFXTestHelper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para vinculação de eventos via {@link EventBinder}.
 * Camada 4 — garante que o EVENT_MAP é preenchido e os desregistros funcionam.
 */
class EventBindingTest {

    private ScreenView view;

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @BeforeEach
    void setUp() {
        view = ScreenAssembler.compose(Example.class);
    }

    @AfterEach
    void tearDown() {
        // Limpa eventos registrados antes de limpar dados compartilhados
        EventBinder.deleteEvents(view.screenInstance());
        ScreenManagerSharedData.resetScreenData();
        ScreenHierarchyRegistry.clear();
    }

    // ---- Cenário 1: EVENT_MAP registra listeners após compose ----

    @Test
    @DisplayName("Apos compose, eventMap deve conter a instancia da tela")
    @SuppressWarnings("unchecked")
    void shouldRegisterEventsAfterCompose() {
        // Acessa o eventMap via ScreenRuntimeContext
        Map<Object, Map<Object, List<Runnable>>> eventMap =
                ScreenRuntimeContext.getDefault().getEventMap();

        assertTrue(eventMap.containsKey(view.screenInstance()),
                "eventMap deve conter a instancia da tela apos compose");

        Map<Object, List<Runnable>> screenEvents = eventMap.get(view.screenInstance());
        assertFalse(screenEvents.isEmpty(),
                "Mapa de eventos da tela nao deve estar vazio");
    }

    // ---- Cenário 2: deleteEvents remove todos os eventos ----

    @Test
    @DisplayName("deleteEvents deve remover todos os eventos da tela")
    @SuppressWarnings("unchecked")
    void shouldRemoveAllEventsOnDelete() {
        Map<Object, Map<Object, List<Runnable>>> eventMap =
                ScreenRuntimeContext.getDefault().getEventMap();

        // Confirma que existe antes de deletar
        assertTrue(eventMap.containsKey(view.screenInstance()),
                "eventMap deve conter a tela antes de deleteEvents");

        // Deleta os eventos
        EventBinder.deleteEvents(view.screenInstance());

        assertFalse(eventMap.containsKey(view.screenInstance()),
                "eventMap nao deve conter a tela apos deleteEvents");
    }

    // ---- Cenário 3: Binders retornam runnables de desregistro ----

    @Test
    @DisplayName("Eventos registrados devem ter ao menos um runnable de desregistro")
    @SuppressWarnings("unchecked")
    void shouldHaveUnregisterRunnables() {
        Map<Object, Map<Object, List<Runnable>>> eventMap =
                ScreenRuntimeContext.getDefault().getEventMap();

        Map<Object, List<Runnable>> screenEvents = eventMap.get(view.screenInstance());
        assertNotNull(screenEvents, "Deve haver mapa de eventos para a tela");

        // Verifica que pelo menos um elemento tem runnables não vazios
        boolean hasRunnables = screenEvents.values().stream()
                .anyMatch(list -> !list.isEmpty());
        assertTrue(hasRunnables,
                "Ao menos um elemento deve ter runnables de desregistro");
    }

    // ---- Cenário extra: deleteEvents com instância nula não lança exceção ----

    @Test
    @DisplayName("deleteEvents com instância nula não deve lançar exceção")
    void shouldNotThrowOnNullDelete() {
        assertDoesNotThrow(() -> EventBinder.deleteEvents(null),
                "deleteEvents com nulo deve ser silencioso");
    }
}
