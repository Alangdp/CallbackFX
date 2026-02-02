package com.connectasistemas.framework.utils.delimiter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import com.connectasistemas.framework.utils.StringUtils;

/**
 * Gerencia delimitadores percentuais posicionados sobre uma {@link Region},
 * mantendo sempre uma camada de overlay acima do conteúdo do componente alvo.
 */
public class RegionOverlayPane {

    /** Ocupação máxima permitida por linha (100%). */
    private static final double MAX_OCCUPATION_PERCENT = 100d;
    /** Tolerância numérica para comparações de ponto flutuante. */
    private static final double EPSILON = 0.0001d;

    /** Região à qual o overlay está acoplado. */
    private final Region targetRegion;
    /** Pane que desenha efetivamente os delimitadores acima da região alvo. */
    private final Pane overlayLayer;
    /** Coleção dos delimitadores ativos e seus respectivos nós visuais. */
    private final List<DelimiterHandle> handles = new ArrayList<>();
    /** Listener que acompanha mudanças no pai da região para reposicionar o overlay. */
    private final ChangeListener<Parent> parentListener = this::handleParentChange;
    /** Listener que reage a mudanças de tamanho da região para recalcular o layout. */
    private final InvalidationListener sizeListener = observable -> layoutDelimiters();

    /** Indica se o overlay deve mostrar estilos de depuração. */
    private boolean debugVisible;

    /**
     * Cria o gerenciador de delimitadores para a {@link Region} informada.
     *
     * @param targetRegion componente que receberá os overlays
     */
    public RegionOverlayPane(Region targetRegion) {

        // OBS: Protege contra instância nula da região e evita overlay órfão.
        this.targetRegion = Objects.requireNonNull(targetRegion, "Region alvo obrigatória");

        // OBS: Cria o Pane que servirá como camada superior aos componentes originais.
        this.overlayLayer = createOverlayLayer();

        // OBS: Sincroniza posicionamento, tradução e tamanho com a região alvo.
        bindOverlayToTarget();

        // OBS: Garante que o overlay esteja inserido no mesmo pai logo na construção.
        attachToParent(targetRegion.getParent());

        // OBS: Observa futuras mudanças de pai para reposicionar automaticamente.
        targetRegion.parentProperty().addListener(parentListener);
    }

    /**
     * Ativa ou desativa a visualização dos delimitadores no overlay.
     *
     * @param debugVisible true para exibir bordas/background, false para invisível
     */
    public void setDebugVisible(boolean debugVisible) {
        this.debugVisible = debugVisible;

        // OBS: Reaplica o estilo em todos os delimitadores para refletir o estado atual.
        handles.forEach(handle -> applyDebugStyle(handle.node()));
    }

    public boolean isDebugVisible() {
        return debugVisible;
    }

    /**
     * Adiciona um novo delimitador, garantindo que o total de largura não exceda 100%.
     *
     * @param delimiter delimitador a ser registrado
     */
    public void addDelimiter(Delimiter delimiter) {
        Objects.requireNonNull(delimiter, "Delimiter obrigatório");

        // OBS: Confere se offset e percentuais estão dentro dos limites.
        validatePercentages(delimiter);

        // OBS: Confere se o novo delimitador não estoura a soma da linha.
        validateGlobalOccupation(delimiter);

        // OBS: Cria o nó gráfico correspondente ao delimitador.
        Region node = buildDelimiterNode(delimiter);

        // OBS: Armazena o handle para operações futuras.
        handles.add(new DelimiterHandle(delimiter, node));

        // OBS: Adiciona o nó ao overlay.
        overlayLayer.getChildren().add(node);

        // OBS: Mantém a camada sempre acima dos demais nós irmãos.
        overlayLayer.toFront();

        // OBS: Força a atualização das dimensões e posições.
        layoutDelimiters();
    }

    /**
     * Remove o delimitador informado, recalculando a ocupação total.
     *
     * @param delimiter delimitador previamente registrado
     * @return true se foi removido, senão false
     */
    public boolean removeDelimiter(Delimiter delimiter) {
        Objects.requireNonNull(delimiter, "Delimiter obrigatório");
        Iterator<DelimiterHandle> iterator = handles.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            DelimiterHandle handle = iterator.next();
            if (handle.delimiter().equals(delimiter)) {
                
                // OBS: Remove o nó visual do overlay.
                overlayLayer.getChildren().remove(handle.node());
                iterator.remove();
                removed = true;
            }
        }
        
        // OBS: Recalcula o layout apenas se houve remoção efetiva.
        if (removed) layoutDelimiters();
        return removed;
    }

    /**
     * Remove todos os delimitadores cadastrados.
     */
    public void clearDelimiters() {
        handles.clear();
        overlayLayer.getChildren().clear();
    }

    /**
     * Retorna uma visão imutável dos delimitadores ativos.
     *
     * @return lista somente leitura de delimitadores
     */
    public List<Delimiter> getDelimiters() {
        if (handles.isEmpty()) return Collections.emptyList();
        List<Delimiter> snapshot = new ArrayList<>(handles.size());
        for (DelimiterHandle handle : handles) {
            snapshot.add(handle.delimiter());
        }
        return Collections.unmodifiableList(snapshot);
    }

    private Pane createOverlayLayer() {
        Pane layer = new Pane();

        // OBS: Overlay não participa do layout do pai para não deslocar outros elementos.
        layer.setManaged(false);

        // OBS: Ignora eventos nas áreas vazias para não bloquear o componente base.
        layer.setPickOnBounds(false);

        // OBS: Deve capturar eventos, portanto mouseTransparent fica false.
        layer.setMouseTransparent(false);
        layer.getStyleClass().add("region-overlay-layer");
        return layer;
    }

    private void bindOverlayToTarget() {
        
        // OBS: Replica deslocamentos e traduções para manter o overlay alinhado.
        overlayLayer.layoutXProperty().bind(targetRegion.layoutXProperty());
        overlayLayer.layoutYProperty().bind(targetRegion.layoutYProperty());
        overlayLayer.translateXProperty().bind(targetRegion.translateXProperty());
        overlayLayer.translateYProperty().bind(targetRegion.translateYProperty());

        // OBS: Mantém dimensões idênticas à região de origem.
        overlayLayer.prefWidthProperty().bind(targetRegion.widthProperty());
        overlayLayer.prefHeightProperty().bind(targetRegion.heightProperty());
        overlayLayer.minWidthProperty().bind(targetRegion.widthProperty());
        overlayLayer.minHeightProperty().bind(targetRegion.heightProperty());
        overlayLayer.maxWidthProperty().bind(targetRegion.widthProperty());
        overlayLayer.maxHeightProperty().bind(targetRegion.heightProperty());

        // OBS: Sempre que a região redimensionar, reposiciona os delimitadores.
        targetRegion.widthProperty().addListener(sizeListener);
        targetRegion.heightProperty().addListener(sizeListener);
    }

    private void handleParentChange(ObservableValue<? extends Parent> observable, Parent oldParent, Parent newParent) {
        detachFromParent(oldParent);
        attachToParent(newParent);
    }

    private void attachToParent(Parent parent) {
        if (parent == null) return;
        if (!(parent instanceof Pane pane)) {
            throw new IllegalStateException(StringUtils.concat(
                "O pai da Region deve herdar Pane para suportar o overlay: ",
                parent.getClass().getName()
            ));
        }
        if (!pane.getChildren().contains(overlayLayer)) {
            
            // OBS: Insere o overlay apenas uma vez no pai compatível.
            pane.getChildren().add(overlayLayer);
        }

        // OBS: Mantém o overlay acima das demais crianças para preservar a interação.
        overlayLayer.toFront();
    }

    private void detachFromParent(Parent parent) {
        if (parent instanceof Pane pane) {
            pane.getChildren().remove(overlayLayer);
        }
    }

    private void layoutDelimiters() {
        double regionWidth = targetRegion.getWidth();
        double regionHeight = targetRegion.getHeight();
        if (regionWidth <= 0 || regionHeight <= 0) return;

        for (DelimiterHandle handle : handles) {
            Region node = handle.node();

            // OBS: Calcula dimensões e posições reais multiplicando percentuais pelos tamanhos atuais da Region.
            double width = regionWidth * (handle.delimiter().getWidthPercent() / 100d);
            double height = regionHeight * (handle.delimiter().getHeightPercent() / 100d);
            double positionX = regionWidth * (handle.delimiter().getStartXPercent() / 100d);
            double positionY = regionHeight * (handle.delimiter().getStartYPercent() / 100d);

            // OBS: Reaplica tamanho com base nos percentuais atuais para acompanhar redimensionamentos.
            node.resize(width, height);

            // OBS: Reposiciona o nó considerando os offsets de início para refletir os percentuais de origem.
            node.relocate(positionX, positionY);
        }
    }

    private Region buildDelimiterNode(Delimiter delimiter) {
        StackPane node = new StackPane();
        node.setManaged(false);
        node.setAlignment(Pos.CENTER);
        node.setPickOnBounds(true);
        node.setMouseTransparent(false);

        // OBS: Insere o rótulo centralizado do delimitador para facilitar identificação visual.
        node.getChildren().add(buildLabel(delimiter));
        node.getStyleClass().add("region-overlay-delimiter");
        applyDebugStyle(node);

        // OBS: Mantém o delimitador pronto para novos drops enquanto o cursor permanece sobre ele.
        node.setOnDragOver(event -> {

            // OBS: Aceita todos os modos de transferência para permitir múltiplos tipos de conteúdo.
            event.acceptTransferModes(TransferMode.ANY);

            // OBS: Consome o evento para impedir propagação ao componente base.
            event.consume();
        });

        // OBS: Trata o momento em que o usuário solta o item arrastado dentro do delimitador.
        node.setOnDragDropped(event -> {

            // OBS: Executa a rotina registrada no delimitador para tratar o drop.
            delimiter.executeDrop(event);

            // OBS: Marca o drop como concluído para informar ao sistema DnD.
            event.setDropCompleted(true);

            // OBS: Consome o evento para evitar duplicidade de processamento.
            event.consume();
        });

        // OBS: Consome eventos ao entrar na área para não gerar feedback extra no componente base.
        node.setOnDragEntered(event -> {

            // OBS: Consome imediatamente para manter o overlay como manipulador exclusivo.
            event.consume();
        });

        // OBS: Repete a estratégia ao sair da área garantindo que o ciclo de drag finalize no overlay.
        node.setOnDragExited(event -> {

            // OBS: Consome o evento para evitar que outros nós interpretem a saída dupla.
            event.consume();
        });
        return node;
    }

    private Label buildLabel(Delimiter delimiter) {
        String labelText = delimiter.getLabel();
        Label label = new Label(StringUtils.isBlank(labelText) ? "" : labelText);
        label.setMouseTransparent(true);
        return label;
    }

    private void applyDebugStyle(Region node) {
        if (debugVisible) {
            node.setStyle("-fx-border-color: #ff6f00; -fx-border-width: 1; -fx-background-color: rgba(255,111,0,0.25);");
        } else {
            node.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");
        }
    }

    private void validatePercentages(Delimiter delimiter) {
        if (delimiter.getWidthPercent() <= 0 || delimiter.getWidthPercent() > 100) {
            throw new IllegalArgumentException(StringUtils.concat(
                "widthPercent inválido: ", delimiter.getWidthPercent()
            ));
        }
        if (delimiter.getHeightPercent() <= 0 || delimiter.getHeightPercent() > 100) {
            throw new IllegalArgumentException(StringUtils.concat(
                "heightPercent inválido: ", delimiter.getHeightPercent()
            ));
        }
        if (delimiter.getStartXPercent() < 0 || delimiter.getStartXPercent() >= 100) {
            throw new IllegalArgumentException(StringUtils.concat(
                "startXPercent inválido: ", delimiter.getStartXPercent()
            ));
        }
        if (delimiter.getStartYPercent() < 0 || delimiter.getStartYPercent() >= 100) {
            throw new IllegalArgumentException(StringUtils.concat(
                "startYPercent inválido: ", delimiter.getStartYPercent()
            ));
        }
    }

    private void validateGlobalOccupation(Delimiter delimiter) {
        double currentTotal = getRowWidthOccupation(delimiter.getStartYPercent());
        double newTotal = currentTotal + delimiter.getWidthPercent();
        if (newTotal - MAX_OCCUPATION_PERCENT > EPSILON) {
            throw new IllegalStateException(StringUtils.concat(
                "Não é possível adicionar o delimitador: ocupação total excede 100% da Region alvo (",
                newTotal,
                "%)."
            ));
        }
    }

    private double getRowWidthOccupation(double rowStart) {
        double total = 0d;
        for (DelimiterHandle handle : handles) {
            if (Math.abs(handle.delimiter().getStartYPercent() - rowStart) < EPSILON) {
                total += handle.delimiter().getWidthPercent();
            }
        }
        return total;
    }

    private record DelimiterHandle(Delimiter delimiter, Region node) { }
}
