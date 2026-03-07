package com.connectasistemas.framework.utils;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.text.Text;
import javafx.util.Callback;

// Adaptado de https://stackoverflow.com/a/23682926 para TreeTableView

/**
 * Utilidades para manipular colunas de {@link TreeTableView}.
 */
public class TreeTableManager {

    // Padding padrao para celulas
    private static final double DEFAULT_PADDING = 20.0;

    // Largura minima padrao para colunas com controles graficos
    private static final double DEFAULT_GRAPHIC_MIN_WIDTH = 150.0;

    private TreeTableManager() {
    }

    // ========================================================================
    // ALINHAMENTO DE CELULAS
    // ========================================================================

    /**
     * Define o alinhamento de todas as celulas de uma coluna.
     * 
     * @param column    A coluna a ser configurada
     * @param alignment Posicao do conteudo (ex: Pos.CENTER, Pos.CENTER_LEFT)
     */
    @SuppressWarnings("unchecked")
    public static <S, T> void setColumnAlignment(TreeTableColumn<S, T> column, Pos alignment) {
        if (column == null) {
            return;
        }

        // Guarda a cellFactory original se existir
        Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> originalFactory = column.getCellFactory();

        column.setCellFactory(col -> {
            TreeTableCell<S, T> cell;

            // Usa a factory original se existir, senao cria celula padrao
            if (originalFactory != null) {
                cell = originalFactory.call(col);
            } else {
                cell = new TreeTableCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.toString());
                        }
                    }
                };
            }

            cell.setAlignment(alignment);
            return cell;
        });
    }

    /**
     * Centraliza todas as celulas de uma coluna.
     * 
     * @param column A coluna a ser centralizada
     */
    public static <S, T> void centerColumn(TreeTableColumn<S, T> column) {
        setColumnAlignment(column, Pos.CENTER);
    }

    /**
     * Alinha todas as celulas de uma coluna a esquerda.
     * 
     * @param column A coluna a ser alinhada
     */
    public static <S, T> void alignColumnLeft(TreeTableColumn<S, T> column) {
        setColumnAlignment(column, Pos.CENTER_LEFT);
    }

    /**
     * Alinha todas as celulas de uma coluna a direita.
     * 
     * @param column A coluna a ser alinhada
     */
    public static <S, T> void alignColumnRight(TreeTableColumn<S, T> column) {
        setColumnAlignment(column, Pos.CENTER_RIGHT);
    }

    /**
     * Define o alinhamento de todas as colunas de um TreeTableView.
     * 
     * @param treeTableView O TreeTableView
     * @param alignment     Posicao do conteudo
     */
    public static void setAllColumnsAlignment(TreeTableView<?> treeTableView, Pos alignment) {
        if (treeTableView == null) {
            return;
        }

        for (TreeTableColumn<?, ?> column : treeTableView.getColumns()) {
            setColumnAlignmentRaw(column, alignment);
        }
    }

    /**
     * Centraliza todas as colunas de um TreeTableView.
     * 
     * @param treeTableView O TreeTableView
     */
    public static void centerAllColumns(TreeTableView<?> treeTableView) {
        setAllColumnsAlignment(treeTableView, Pos.CENTER);
    }

    /**
     * Metodo auxiliar para aplicar alinhamento sem generics.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setColumnAlignmentRaw(TreeTableColumn column, Pos alignment) {
        setColumnAlignment(column, alignment);
    }

    // ========================================================================
    // AUTO-DIMENSIONAMENTO DE COLUNAS
    // ========================================================================

    /**
     * Redimensiona automaticamente todas as colunas do TreeTableView para caber no conteudo.
     * 
     * @param treeTableView
     *                      O TreeTableView cujas colunas serao redimensionadas.
     */
    public static void autoSizeColumns(final TreeTableView<?> treeTableView) {
        autoSizeColumns(treeTableView, -1, -1);
    }

    /**
     * Redimensiona automaticamente todas as colunas do TreeTableView para caber no conteudo.
     * 
     * @param treeTableView
     *                      O TreeTableView cujas colunas serao redimensionadas.
     * @param minWidth
     *                      Largura minima desejada para todas as colunas. Use -1 para sem minimo.
     * @param maxWidth
     *                      Largura maxima desejada para todas as colunas. Use -1 para sem maximo.
     */
    public static void autoSizeColumns(final TreeTableView<?> treeTableView, double minWidth, double maxWidth) {
        if (treeTableView == null) {
            return;
        }

        for (TreeTableColumn<?, ?> column : treeTableView.getColumns()) {
            autoSizeColumn(column, minWidth, maxWidth, -1);
        }
    }

    /**
     * Redimensiona automaticamente uma coluna do TreeTableView para caber no conteudo.
     * 
     * @param column
     *                 A coluna a ser redimensionada.
     * @param minWidth
     *                 Largura minima desejada para esta coluna. Use -1 para sem minimo.
     * @param maxWidth
     *                 Largura maxima desejada para esta coluna. Use -1 para sem maximo.
     * @param maxRows
     *                 Numero maximo de linhas a examinar. Use -1 para todas.
     */
    public static void autoSizeColumn(TreeTableColumn<?, ?> column, double minWidth, double maxWidth, int maxRows) {
        if (column == null) {
            return;
        }

        TreeTableView<?> treeTableView = column.getTreeTableView();
        if (treeTableView == null) {
            return;
        }

        TreeItem<?> root = treeTableView.getRoot();
        if (root == null) {
            return;
        }

        // Coleta todos os itens da arvore
        List<TreeItem<?>> allItems = new ArrayList<>();
        collectAllTreeItems(root, allItems, treeTableView.isShowRoot());

        if (allItems.isEmpty()) {
            return;
        }

        // Calcula a largura do header
        double desiredWidth = calculateHeaderWidth(column);

        // Limita o numero de linhas a examinar
        int rows = maxRows == -1 ? allItems.size() : Math.min(allItems.size(), maxRows);

        // Flag para detectar se ha controles graficos
        boolean hasGraphicContent = false;

        // Verifica a largura de cada celula
        for (int row = 0; row < rows; row++) {
            CellMeasurement measurement = measureCell(treeTableView, column, row);
            desiredWidth = Math.max(desiredWidth, measurement.width);
            if (measurement.hasGraphic) {
                hasGraphicContent = true;
            }
        }

        // Se ha conteudo grafico e nenhuma largura foi calculada, usa minimo padrao
        if (hasGraphicContent && desiredWidth < DEFAULT_GRAPHIC_MIN_WIDTH) {
            desiredWidth = DEFAULT_GRAPHIC_MIN_WIDTH;
        }

        // Adiciona padding
        desiredWidth += DEFAULT_PADDING;

        // Aplica limites
        if (minWidth > 0) {
            desiredWidth = Math.max(minWidth, desiredWidth);
            column.setMinWidth(minWidth);
        }
        if (maxWidth > 0) {
            desiredWidth = Math.min(maxWidth, desiredWidth);
            column.setMaxWidth(maxWidth);
        }

        column.setPrefWidth(desiredWidth);
    }

    /**
     * Calcula a largura necessaria para o texto do header da coluna.
     * 
     * @param column A coluna
     * @return Largura calculada do header
     */
    private static double calculateHeaderWidth(TreeTableColumn<?, ?> column) {
        String headerText = column.getText();
        if (headerText == null || headerText.isEmpty()) {
            return 0;
        }

        Text text = new Text(headerText);
        return text.getLayoutBounds().getWidth();
    }

    /**
     * Mede a largura de uma celula, considerando texto e graficos.
     * 
     * @param treeTableView A TreeTableView
     * @param column A coluna
     * @param rowIndex Indice da linha
     * @return Medicao da celula
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static CellMeasurement measureCell(TreeTableView<?> treeTableView, TreeTableColumn column, int rowIndex) {
        CellMeasurement result = new CellMeasurement();

        // Tenta obter a celula renderizada
        TreeTableCell cell = findRenderedCell(treeTableView, column, rowIndex);
        if (cell != null) {
            // Verifica se ha grafico na celula
            Node graphic = cell.getGraphic();
            if (graphic != null) {
                result.hasGraphic = true;
                // Mede o grafico se ja tiver bounds calculados
                double graphicWidth = graphic.getBoundsInLocal().getWidth();
                if (graphicWidth > 0) {
                    result.width = graphicWidth;
                    return result;
                }
                // Tenta prefWidth se bounds nao estiver disponivel
                if (graphic.prefWidth(-1) > 0) {
                    result.width = graphic.prefWidth(-1);
                    return result;
                }
            }

            // Mede o texto da celula
            String cellText = cell.getText();
            if (cellText != null && !cellText.isEmpty()) {
                Text text = new Text(cellText);
                result.width = text.getLayoutBounds().getWidth();
                return result;
            }
        }

        // Fallback: usa cellData do CellValueFactory
        if (column.getCellValueFactory() != null) {
            Object cellData = column.getCellData(rowIndex);
            if (cellData != null) {
                String cellText = cellData.toString();
                if (cellText != null && !cellText.isEmpty()) {
                    Text text = new Text(cellText);
                    result.width = text.getLayoutBounds().getWidth();
                }
            }
        }

        return result;
    }

    /**
     * Tenta encontrar uma celula renderizada na TreeTableView.
     * 
     * @param treeTableView A TreeTableView
     * @param column A coluna
     * @param rowIndex Indice da linha
     * @return A celula encontrada ou null
     */
    @SuppressWarnings("rawtypes")
    private static TreeTableCell findRenderedCell(TreeTableView<?> treeTableView, TreeTableColumn column, int rowIndex) {
        // Procura nas linhas visiveis
        for (Node node : treeTableView.lookupAll(".tree-table-row-cell")) {
            if (node instanceof TreeTableRow) {
                TreeTableRow row = (TreeTableRow) node;
                if (row.getIndex() == rowIndex) {
                    // Procura a celula da coluna especifica
                    for (Node cellNode : row.lookupAll(".tree-table-cell")) {
                        if (cellNode instanceof TreeTableCell) {
                            TreeTableCell cell = (TreeTableCell) cellNode;
                            if (cell.getTableColumn() == column) {
                                return cell;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Coleta recursivamente todos os TreeItems da arvore em uma lista.
     * 
     * @param item         O item atual a ser processado
     * @param result       A lista onde os itens serao adicionados
     * @param includeFirst Se deve incluir o primeiro item (raiz)
     */
    private static void collectAllTreeItems(TreeItem<?> item, List<TreeItem<?>> result, boolean includeFirst) {
        if (item == null) {
            return;
        }

        // Adiciona o item atual se nao for a raiz ou se showRoot estiver ativo
        if (includeFirst) {
            result.add(item);
        }

        // Processa os filhos recursivamente
        for (TreeItem<?> child : item.getChildren()) {
            result.add(child);
            collectAllTreeItems(child, result, false);
        }
    }

    /**
     * Classe auxiliar para resultado de medicao de celula.
     */
    private static class CellMeasurement {
        double width = 0;
        boolean hasGraphic = false;
    }

}