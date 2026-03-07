package com.connectasistemas.framework.utils;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.connectasistemas.framework.internal.utils.ScreenMetadata;

import javafx.stage.Stage;

/**
 * Contexto completo de navegacao e estado da aplicacao.
 * Centraliza os campos que antes viviam como {@code static} no
 * {@link ScreenManager} e nas classes de registro, permitindo multiplos
 * contextos no mesmo processo (ex.: testes paralelos, multi-window).
 */
public class ScreenRuntimeContext {

    // ---- Estado de navegacao ------------------------------------------------

    /** Stage principal gerenciado por esta instancia. */
    private Stage mainStage;

    /** Instancia da tela ativa (objeto anotado com {@code @Screen}). */
    private Object screenInstance;

    /** Metadados da tela ativa. */
    private ScreenMetadata currentMetadata;

    /** Flag de controle de reentrancia durante trocas de tela. */
    private boolean changeInProgress;

    /** Classe adiada para processamento apos a troca em andamento. */
    private Class<?> deferredScreenClass;

    /** Historico de navegacao (pilha de classes visitadas). */
    private final Stack<Class<?>> screenHistory = new Stack<>();

    /** Sub-janelas abertas, indexadas pela instancia da tela filha. */
    private final Map<Object, Stage> childStages = new WeakHashMap<>();

    /** Pilha thread-local de metadados de composicao (telas aninhadas). */
    private final ThreadLocal<Deque<ScreenCompositionMetadata>> composingMetadata =
            ThreadLocal.withInitial(ArrayDeque::new);

    // ---- Registries (antes em classes separadas com static) -----------------

    /** Controller contexts indexados por classe da tela. */
    private final Map<Class<?>, ScreenControllerContext> controllerContexts = new ConcurrentHashMap<>();

    /** Mapeamento reverso instancia -> classe. */
    private final Map<Object, Class<?>> instanceToClass = Collections.synchronizedMap(new WeakHashMap<>());

    /** Cache de elementos por tela/acronym. */
    private final Map<Object, Map<String, Object>> sharedDataCache = Collections.synchronizedMap(new WeakHashMap<>());

    /** Hierarquia pai-filho entre telas. */
    private final Map<Object, List<Object>> childrenByParent = new IdentityHashMap<>();

    /** Mapa de eventos por tela/elemento. */
    private final Map<Object, Map<Object, List<Runnable>>> eventMap = Collections.synchronizedMap(new WeakHashMap<>());

    /** Estado de validacao. */
    private final ValidationState validationState = new ValidationState();

    // ---- Instancia padrao (singleton de transicao) --------------------------

    private static final ScreenRuntimeContext DEFAULT = new ScreenRuntimeContext();

    /**
     * Retorna a instancia padrao compartilhada, usada durante o periodo de
     * transicao enquanto o codigo antigo ainda opera via metodos estaticos.
     *
     * @return instancia padrao
     */
    public static ScreenRuntimeContext getDefault() {
        return DEFAULT;
    }

    // ---- Getters e Setters (navegacao) --------------------------------------

    public Stage getMainStage() {
        return mainStage;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public Object getScreenInstance() {
        return screenInstance;
    }

    public void setScreenInstance(Object screenInstance) {
        this.screenInstance = screenInstance;
    }

    public ScreenMetadata getCurrentMetadata() {
        return currentMetadata;
    }

    public void setCurrentMetadata(ScreenMetadata currentMetadata) {
        this.currentMetadata = currentMetadata;
    }

    public boolean isChangeInProgress() {
        return changeInProgress;
    }

    public void setChangeInProgress(boolean changeInProgress) {
        this.changeInProgress = changeInProgress;
    }

    public Class<?> getDeferredScreenClass() {
        return deferredScreenClass;
    }

    public void setDeferredScreenClass(Class<?> deferredScreenClass) {
        this.deferredScreenClass = deferredScreenClass;
    }

    public Stack<Class<?>> getScreenHistory() {
        return screenHistory;
    }

    public Map<Object, Stage> getChildStages() {
        return childStages;
    }

    public Deque<ScreenCompositionMetadata> getComposingMetadataStack() {
        return composingMetadata.get();
    }

    /**
     * Remove o ThreadLocal de composicao, evitando vazamento de memoria.
     */
    public void clearComposingMetadata() {
        Deque<ScreenCompositionMetadata> stack = composingMetadata.get();
        if (stack.isEmpty()) {
            composingMetadata.remove();
        }
    }

    // ---- Accessors para registries ------------------------------------------

    public Map<Class<?>, ScreenControllerContext> getControllerContexts() {
        return controllerContexts;
    }

    public Map<Object, Class<?>> getInstanceToClass() {
        return instanceToClass;
    }

    public Map<Object, Map<String, Object>> getSharedDataCache() {
        return sharedDataCache;
    }

    public Map<Object, List<Object>> getChildrenByParent() {
        return childrenByParent;
    }

    public Map<Object, Map<Object, List<Runnable>>> getEventMap() {
        return eventMap;
    }

    public ValidationState getValidationState() {
        return validationState;
    }

    // ---- Record auxiliar para controller contexts ----------------------------

    /**
     * Par imutavel que associa uma instancia de tela ao seu controller.
     */
    public record ScreenControllerContext(Object screenInstance, Object controller) {
    }

    // ---- Estado de validacao ------------------------------------------------

    /**
     * Encapsula mapas de validacao que antes viviam como campos static em
     * {@code ValidationBinderGeneric}.
     */
    public static final class ValidationState {

        /** Map de botoes para acoes de trigger. */
        private final Map<Object, List<Object>> buttonTriggers = new WeakHashMap<>();

        /** Controles com supressao de foco ativa. */
        private final Set<Object> focusSuppressed =
                Collections.newSetFromMap(new IdentityHashMap<>());

        /**
         * Retorna o mapa de triggers por botao.
         *
         * @return mapa mutavel de triggers
         */
        @SuppressWarnings("unchecked")
        public <T> Map<Object, List<T>> getButtonTriggers() {
            return (Map<Object, List<T>>) (Map<?, ?>) buttonTriggers;
        }

        /**
         * Retorna o set de controles com foco suprimido.
         *
         * @return set mutavel de controles
         */
        @SuppressWarnings("unchecked")
        public <T> Set<T> getFocusSuppressed() {
            return (Set<T>) focusSuppressed;
        }
    }
}
