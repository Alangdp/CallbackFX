package com.connectasistemas.framework.utils.drag;

import com.connectasistemas.framework.internal.utils.drag.DragRegistration;
import com.connectasistemas.framework.internal.utils.drag.NodeDragRegistration;
import com.connectasistemas.framework.internal.utils.drag.OverlayRegistration;
import com.connectasistemas.framework.utils.delimiter.RegionOverlayPane;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Utilitário genérico para auxiliar operações de drag em {@link Region} do JavaFX.
 * Centraliza o registro de eventos de mouse e o gerenciamento de overlays compostos por {@link Delimiter}s.
 * 
 * Também gerencia a detecção de elementos sob o mouse durante operações de drag,
 * permitindo que elementos como ChildFactory sejam notificados quando um drag ocorre sobre eles.
 */
public final class DragUtils {

    /**
     * Registra um callback para drop (soltar) em uma Region, sem necessidade de overlay ou delimiters.
     * O callback será chamado quando um drop for realizado sobre a região.
     *
     * @param region Região alvo do drop
     * @param onDrop Callback chamado ao soltar (DragEvent)
     */
    public static void registerDropHandler(Region region, EventHandler<DragEvent> onDrop) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(onDrop, "onDrop");
        region.setOnDragDropped(onDrop);
    }

    private static final WeakHashMap<Region, DragRegistration>     DRAG_REGISTRY      = new WeakHashMap<>();
    private static final WeakHashMap<Node,   NodeDragRegistration> NODE_DRAG_REGISTRY = new WeakHashMap<>();
    private static final WeakHashMap<Region, OverlayRegistration>  OVERLAY_REGISTRY   = new WeakHashMap<>();
    
    // Armazena os DelimiterSpecs para poder disparar triggers manualmente
    private static final WeakHashMap<Region, List<DelimiterSpec>> DELIMITER_SPECS_REGISTRY = new WeakHashMap<>();
    
    // Registro de listeners de hover para detectar drag sobre elementos
    private static final WeakHashMap<Region, HoverDetectionRegistration> HOVER_REGISTRY = new WeakHashMap<>();
    
    // Flag global que indica se há um drag em andamento
    private static boolean globalDragging = false;
    
    // Posição atual do mouse durante o drag
    private static double currentDragX = 0;
    private static double currentDragY = 0;
    
    // Elemento que está sendo arrastado atualmente (para excluir da notificação de hover)
    private static Region currentDragSource = null;

    // Dado associado ao drag atual (funciona para ambos os sistemas: mouse e nativo JavaFX)
    private static Object currentDragData = null;

    // Registro de callbacks de drop customizados (sistema de mouse)
    private static final WeakHashMap<Region, Consumer<Object>> MOUSE_DROP_REGISTRY = new WeakHashMap<>();

    private DragUtils() {
    }

    // ---------------------------------------------------------------------
    // Global drag state
    // ---------------------------------------------------------------------
    
    /**
     * Indica se há um drag em andamento globalmente.
     */
    public static boolean isDragging() {
        return globalDragging;
    }
    
    /**
     * Retorna a posição X atual do mouse durante o drag.
     */
    public static double getCurrentDragX() {
        return currentDragX;
    }
    
    /**
     * Retorna a posição Y atual do mouse durante o drag.
     */
    public static double getCurrentDragY() {
        return currentDragY;
    }
    
    /**
     * Atualiza o estado global de drag.
     * Deve ser chamado pelos handlers de drag.
     * 
     * @param dragging true se o drag está ativo, false se terminou
     * @param sceneX posição X do mouse na Scene
     * @param sceneY posição Y do mouse na Scene
     */
    public static void setDragState(boolean dragging, double sceneX, double sceneY) {
        setDragState(dragging, sceneX, sceneY, null);
    }
    
    /**
     * Atualiza o estado global de drag, especificando o elemento sendo arrastado.
     * Deve ser chamado pelos handlers de drag.
     * 
     * @param dragging true se o drag está ativo, false se terminou
     * @param sceneX posição X do mouse na Scene
     * @param sceneY posição Y do mouse na Scene
     * @param dragSource o elemento que está sendo arrastado (será excluído das notificações de hover)
     */
    public static void setDragState(boolean dragging, double sceneX, double sceneY, Region dragSource) {
        globalDragging = dragging;
        currentDragX = sceneX;
        currentDragY = sceneY;
        currentDragSource = dragging ? dragSource : null;
        
        // Notifica todos os elementos registrados para hover detection
        if (dragging) {
            notifyHoverListeners(sceneX, sceneY);
        } else {
            // Quando o drag termina, dispara os mouse drop handlers nas regiões em hover
            fireMouseDropHandlers();
            // Notifica saída para todos e limpa o dado do drag
            clearAllHoverStates();
            clearDragData();
        }
    }
    
    /**
     * Retorna o elemento que está sendo arrastado atualmente.
     * @return o elemento sendo arrastado, ou null se não há drag ativo
     */
    public static Region getCurrentDragSource() {
        return currentDragSource;
    }

    // ---------------------------------------------------------------------
    // Drag data — compartilhamento de dados entre fonte e destino
    // ---------------------------------------------------------------------

    /**
     * Define o dado associado ao drag atual.
     * Deve ser chamado no {@code onPress} da fonte (sistema de mouse)
     * ou no {@code onDragDetected} (sistema nativo JavaFX).
     *
     * @param data qualquer objeto que represente o item sendo arrastado
     */
    public static void setDragData(Object data) {
        currentDragData = data;
    }

    /**
     * Retorna o dado associado ao drag atual.
     * Disponível tanto para o sistema de mouse quanto para o nativo JavaFX.
     *
     * @param <T> tipo esperado do dado
     * @param type classe do tipo esperado
     * @return o dado arrastado, ou null se não houver nenhum ou o tipo não bater
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDragData(Class<T> type) {
        if (type.isInstance(currentDragData)) {
            return (T) currentDragData;
        }
        return null;
    }

    /**
     * Limpa o dado do drag. Chamado automaticamente ao finalizar o drag
     * via {@link #setDragState(boolean, double, double)}.
     */
    public static void clearDragData() {
        currentDragData = null;
    }

    // ---------------------------------------------------------------------
    // Drop handler para sistema de mouse (sem Dragboard)
    // ---------------------------------------------------------------------

    /**
     * Registra um callback de drop para o sistema customizado de mouse.
     * O callback recebe o dado definido via {@link #setDragData(Object)}.
     * Compatível com {@link #registerDropHandler(Region, EventHandler)} — ambos podem coexistir.
     *
     * @param region  região alvo do drop
     * @param onDrop  callback chamado ao soltar, recebe o dado do drag
     */
    public static void registerMouseDropHandler(Region region, Consumer<Object> onDrop) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(onDrop, "onDrop");
        MOUSE_DROP_REGISTRY.put(region, onDrop);
    }

    /**
     * Remove o callback de drop do sistema de mouse para a região informada.
     */
    public static void unregisterMouseDropHandler(Region region) {
        if (region == null) return;
        MOUSE_DROP_REGISTRY.remove(region);
    }

    // ---------------------------------------------------------------------
    // Drag handlers
    // ---------------------------------------------------------------------

    /**
     * Registra handlers de drag para uma {@link Region}. Eventuais registros anteriores são substituídos.
     */
    public static void registerDragHandlers(Region region, DragHandlers handlers) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(handlers, "handlers");

        unregisterDragHandlers(region);
        DragRegistration registration = new DragRegistration(region, handlers);
        registration.install();
        DRAG_REGISTRY.put(region, registration);
    }

    /**
     * Remove os handlers previamente registrados para a {@link Region} informada.
     */
    public static void unregisterDragHandlers(Region region) {
        if (region == null) {
            return;
        }
        DragRegistration registration = DRAG_REGISTRY.remove(region);
        if (registration != null) {
            registration.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // Drag handlers para Node genérico (ex: TreeCell, Label, ImageView...)
    // ---------------------------------------------------------------------

    /**
     * Registra handlers de drag para qualquer {@link Node}.
     * Use quando o elemento não é uma {@link Region} (ex: {@code TreeCell}, {@code Control}).
     * Eventuais registros anteriores são substituídos.
     */
    public static void registerDragHandlers(Node node, DragHandlers handlers) {
        Objects.requireNonNull(node,     "node");
        Objects.requireNonNull(handlers, "handlers");

        unregisterDragHandlers(node);
        NodeDragRegistration registration = new NodeDragRegistration(node, handlers);
        registration.install();
        NODE_DRAG_REGISTRY.put(node, registration);
    }

    /**
     * Remove os handlers previamente registrados para o {@link Node} informado.
     */
    public static void unregisterDragHandlers(Node node) {
        if (node == null) return;
        NodeDragRegistration registration = NODE_DRAG_REGISTRY.remove(node);
        if (registration != null) {
            registration.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // Hover Detection
    // ---------------------------------------------------------------------
    
    /**
     * Registra um elemento para receber notificações de hover durante drag.
     * 
     * @param region O elemento a ser monitorado
     * @param onEnter Callback chamado quando o drag entra na área do elemento
     * @param onExit Callback chamado quando o drag sai da área do elemento
     */
    public static void registerHoverDetection(Region region, Runnable onEnter, Runnable onExit) {
        Objects.requireNonNull(region, "region");
        
        unregisterHoverDetection(region);
        HoverDetectionRegistration registration = new HoverDetectionRegistration(region, onEnter, onExit);
        HOVER_REGISTRY.put(region, registration);
    }
    
    /**
     * Remove o registro de hover detection para o elemento.
     */
    public static void unregisterHoverDetection(Region region) {
        if (region == null) return;
        
        HoverDetectionRegistration registration = HOVER_REGISTRY.remove(region);
        if (registration != null && registration.isHovering) {
            // Notifica saída se estava em hover
            if (registration.onExit != null) {
                registration.onExit.run();
            }
        }
    }
    
    /**
     * Notifica todos os listeners registrados sobre a posição atual do drag.
     * Exclui o elemento que está sendo arrastado (currentDragSource) das notificações.
     */
    private static void notifyHoverListeners(double sceneX, double sceneY) {
        for (HoverDetectionRegistration registration : HOVER_REGISTRY.values()) {
            Region region = registration.region;
            if (region == null || region.getScene() == null) continue;
            
            // Ignora o elemento que está sendo arrastado
            if (currentDragSource != null && isDescendantOrSame(region, currentDragSource)) {
                // Se o elemento sendo arrastado estava em hover, notifica saída
                if (registration.isHovering) {
                    registration.isHovering = false;
                    if (registration.onExit != null) {
                        registration.onExit.run();
                    }
                }
                continue;
            }
            
            // Verifica se o ponto está sobre o elemento
            Bounds boundsInScene = region.localToScene(region.getBoundsInLocal());
            boolean isOver = boundsInScene != null && boundsInScene.contains(sceneX, sceneY);
            
            if (isOver && !registration.isHovering) {
                // Entrou na área
                registration.isHovering = true;
                if (registration.onEnter != null) {
                    registration.onEnter.run();
                }
            } else if (!isOver && registration.isHovering) {
                // Saiu da área
                registration.isHovering = false;
                if (registration.onExit != null) {
                    registration.onExit.run();
                }
            }
        }
    }
    
    /**
     * Verifica se target é descendente de source ou é o mesmo elemento.
     */
    private static boolean isDescendantOrSame(Region target, Region source) {
        if (target == source) return true;
        
        // Verifica se source está na hierarquia de pais de target
        javafx.scene.Parent parent = target.getParent();
        while (parent != null) {
            if (parent == source) return true;
            parent = parent.getParent();
        }
        
        // Verifica se target está na hierarquia de pais de source
        parent = source.getParent();
        while (parent != null) {
            if (parent == target) return true;
            parent = parent.getParent();
        }
        
        return false;
    }
    
    /**
     * Limpa o estado de hover de todos os elementos registrados.
     */
    private static void clearAllHoverStates() {
        for (HoverDetectionRegistration registration : HOVER_REGISTRY.values()) {
            if (registration.isHovering) {
                registration.isHovering = false;
                if (registration.onExit != null) {
                    registration.onExit.run();
                }
            }
        }
    }

    /**
     * Dispara os callbacks de drop do sistema de mouse para todas as regiões
     * que estão em hover no momento em que o drag é finalizado.
     */
    private static void fireMouseDropHandlers() {
        for (HoverDetectionRegistration hoverReg : HOVER_REGISTRY.values()) {
            if (!hoverReg.isHovering) continue;
            Consumer<Object> handler = MOUSE_DROP_REGISTRY.get(hoverReg.region);
            if (handler != null) {
                handler.accept(currentDragData);
            }
        }
    }
    
    /**
     * Registro interno para hover detection.
     */
    private static class HoverDetectionRegistration {
        final Region region;
        final Runnable onEnter;
        final Runnable onExit;
        boolean isHovering = false;
        
        HoverDetectionRegistration(Region region, Runnable onEnter, Runnable onExit) {
            this.region = region;
            this.onEnter = onEnter;
            this.onExit = onExit;
        }
    }

    // ---------------------------------------------------------------------
    // Overlay / Delimiter helpers
    // ---------------------------------------------------------------------

    /**
     * Configura (ou retorna o já configurado) {@link RegionOverlayPane} para a {@link Region} informada.
     * Os {@link Delimiter}s são construídos a partir dos {@link DelimiterSpec}s fornecidos e ficam gerenciados pelo utilitário.
     */
    public static RegionOverlayPane setupOverlay(Region region, List<DelimiterSpec> specifications) {
        Objects.requireNonNull(region, "region");
        if (specifications == null || specifications.isEmpty()) {
            throw new IllegalArgumentException("É necessário informar ao menos um DelimiterSpec.");
        }

        OverlayRegistration existing = OVERLAY_REGISTRY.get(region);
        if (existing != null) {
            return existing.overlay();
        }

        RegionOverlayPane overlay = new RegionOverlayPane(region);
        overlay.setOverlayEnabled(false);

        AtomicReference<String> selection = new AtomicReference<>();
        for (DelimiterSpec spec : specifications) {
            overlay.addDelimiter(spec.toDelimiter(selection));
        }

        OVERLAY_REGISTRY.put(region, new OverlayRegistration(overlay, selection));
        
        // Armazena os specs para poder disparar triggers manualmente
        DELIMITER_SPECS_REGISTRY.put(region, new ArrayList<>(specifications));
        
        return overlay;
    }

    /**
     * Exibe o overlay previamente configurado (caso exista).
     */
    public static void showOverlay(Region region) {
        OverlayRegistration registration = OVERLAY_REGISTRY.get(region);
        if (registration != null) {
            registration.overlay().setOverlayEnabled(true);
        }
    }

    /**
     * Oculta o overlay previamente configurado (caso exista).
     */
    public static void hideOverlay(Region region) {
        OverlayRegistration registration = OVERLAY_REGISTRY.get(region);
        if (registration != null) {
            registration.overlay().setOverlayEnabled(false);
        }
    }

    /**
     * Remove o overlay gerenciado e libera os recursos associados.
     */
    public static void disposeOverlay(Region region) {
        OverlayRegistration registration = OVERLAY_REGISTRY.remove(region);
        if (registration != null) {
            registration.overlay().setOverlayEnabled(false);
        }
    }

    /**
     * @return a chave do último {@link Delimiter} acionado ou {@code null} caso não exista seleção.
     */
    public static String getSelectedDelimiterKey(Region region) {
        OverlayRegistration registration = OVERLAY_REGISTRY.get(region);
        if (registration == null) {
            return null;
        }
        return registration.selection().get();
    }

    /**
     * Limpa a seleção atual associada ao overlay da {@link Region} informada.
     */
    public static void clearSelectedDelimiter(Region region) {
        OverlayRegistration registration = OVERLAY_REGISTRY.get(region);
        if (registration != null) {
            registration.selection().set(null);
        }
    }
    
    /**
     * Dispara o trigger do delimiter que está sob a posição especificada.
     * Deve ser chamado quando o mouse é solto durante um drag.
     * 
     * @param sceneX posição X do mouse na Scene
     * @param sceneY posição Y do mouse na Scene
     * @return true se um trigger foi disparado, false caso contrário
     */
    public static boolean triggerDelimiterAtPosition(double sceneX, double sceneY) {
        // Procura em todos os elementos com hover ativo
        for (HoverDetectionRegistration hoverReg : HOVER_REGISTRY.values()) {
            if (!hoverReg.isHovering) continue;
            
            Region region = hoverReg.region;
            if (region == null || region.getScene() == null) continue;
            
            // Verifica se tem specs registrados
            List<DelimiterSpec> specs = DELIMITER_SPECS_REGISTRY.get(region);
            if (specs == null || specs.isEmpty()) continue;
            
            // Converte posição da scene para local do region
            Bounds regionBounds = region.localToScene(region.getBoundsInLocal());
            if (regionBounds == null || !regionBounds.contains(sceneX, sceneY)) continue;
            
            // Calcula posição relativa dentro do region (em porcentagem)
            double localX = sceneX - regionBounds.getMinX();
            double localY = sceneY - regionBounds.getMinY();
            double percentX = (localX / regionBounds.getWidth()) * 100;
            double percentY = (localY / regionBounds.getHeight()) * 100;
            
            // Procura qual delimiter contém a posição
            for (DelimiterSpec spec : specs) {
                double specMinX = spec.getOffsetX();
                double specMaxX = spec.getOffsetX() + spec.getWidth();
                double specMinY = spec.getOffsetY();
                double specMaxY = spec.getOffsetY() + spec.getHeight();
                
                if (percentX >= specMinX && percentX <= specMaxX &&
                    percentY >= specMinY && percentY <= specMaxY) {
                    // Encontrou o delimiter - dispara o trigger
                    if (spec.getOnTrigger() != null) {
                        spec.getOnTrigger().accept(null, null);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Método utilitário para normalizar listas imutáveis de {@link DelimiterSpec}.
     */
    public static List<DelimiterSpec> immutableSpecs(DelimiterSpec... specs) {
        List<DelimiterSpec> list = new ArrayList<>();
        Collections.addAll(list, specs);
        return Collections.unmodifiableList(list);
    }
}