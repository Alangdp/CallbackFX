package com.connectasistemas.framework.utils.properties;

import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.enums.CursorType;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.interfaces.PropertiesBinder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

/**
 * Binder genérico para aplicar {@link ScreenProperties} em nós e Stage.
 */
public class PropertiesBinderGeneric implements PropertiesBinder {

    private static final String SPLIT_PANE_FIXED_KEY = "splitpane-fixed-applied";

    @Override
    public boolean applyAll(ScreenProperties properties, Node node) {
        if (properties == null || node == null) return false;

        // Propriedades básicas
        node.setDisable(!properties.enabled());
        node.setVisible(properties.visible());
        node.setManaged(properties.managed());
        node.setFocusTraversable(properties.focusTraversable());
        node.setMouseTransparent(properties.mouseTransparent());
        node.setPickOnBounds(properties.pickOnBounds());
        node.setCache(properties.cache());
        node.setOpacity(properties.opacity());

        // Propriedades de transformação
        node.setRotate(properties.rotate());
        node.setScaleX(properties.scaleX());
        node.setScaleY(properties.scaleY());
        node.setScaleZ(properties.scaleZ());
        node.setTranslateX(properties.translateX());
        node.setTranslateY(properties.translateY());
        node.setTranslateZ(properties.translateZ());

        // Propriedades específicas
        applyId(properties, node);
        applyStyle(properties, node);
        applyCursor(properties, node);
        applyTooltip(properties, node);
        applyEditable(properties, node);
        applyWrapText(properties, node);
        applySplitPaneResizability(properties, node);
        applyTreeViewProperties(properties, node);

        if (properties.focusOnLoad()) {
            node.requestFocus();
        }

        return true;
    }

    @Override
    public void applyToStage(ScreenProperties properties, Stage stage) {
        if (properties == null || stage == null) return;

        stage.setResizable(properties.resizable());
        stage.setFullScreen(properties.fullScreen());
        stage.setMaximized(properties.maximized());
        stage.setAlwaysOnTop(properties.alwaysOnTop());
        stage.setOpacity(properties.opacity());
    }

    public void applyToTab(ScreenProperties properties, Tab tab) {
        if (properties == null || tab == null) {
            return;
        }

        tab.setDisable(!properties.enabled());
        applyTabId(properties, tab);
        applyTabStyle(properties, tab);
        applyTabTooltip(properties, tab);
        TabVisibilityManager.setVisible(tab, properties.visible());
    }

    public void applyToTableColumn(ScreenProperties properties, TableColumn<?, ?> column) {
        if (properties == null || column == null) {
            return;
        }

        column.setEditable(properties.editable());
        column.setVisible(properties.visible());
        column.setSortable(properties.enabled());
        column.setReorderable(properties.enabled());

        applyColumnId(properties, column);
        applyColumnStyle(properties, column);
    }

    private void applyId(ScreenProperties properties, Node node) {
        String id = properties.id();
        if (id != null && !id.isBlank()) {
            node.setId(id);
        }
    }

    private void applyStyle(ScreenProperties properties, Node node) {
        if (properties.style() != null && !properties.style().isBlank()) {
            node.setStyle(properties.style());
        }

        String classes = properties.styleClass();
        if (classes != null && !classes.isBlank()) {
            String[] split = classes.trim().split("\\s+");
            node.getStyleClass().addAll(split);
        }
    }

    private void applyCursor(ScreenProperties properties, Node node) {
        CursorType cursorType = properties.cursor();

        Cursor resolved = resolveCursorByEnum(cursorType);
        if (resolved == null) {
            return;
        }

        node.setCursor(resolved);
    }

    private Cursor resolveCursorByEnum(CursorType cursorType) {
        try {
            return switch (cursorType) {
                case DEFAULT -> Cursor.DEFAULT;
                case DRAG -> Cursor.OPEN_HAND;
                case HAND -> Cursor.HAND;
                case MOVE -> Cursor.MOVE;
                case TEXT -> Cursor.TEXT;
                case WAIT -> Cursor.WAIT;
                case RESIZE -> Cursor.SE_RESIZE;
            };
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void applyTooltip(ScreenProperties properties, Node node) {
        String text = properties.tooltip();
        if (text == null || text.isBlank()) {
            return;
        }

        Tooltip tooltip = new Tooltip(text);

        if (node instanceof Control control) {
            control.setTooltip(tooltip);
            return;
        }

        Tooltip.install(node, tooltip);
    }

    private void applyTabId(ScreenProperties properties, Tab tab) {
        String id = properties.id();
        if (id != null && !id.isBlank()) {
            tab.setId(id);
        }
    }

    private void applyTabStyle(ScreenProperties properties, Tab tab) {
        if (properties.style() != null && !properties.style().isBlank()) {
            tab.setStyle(properties.style());
        }

        String classes = properties.styleClass();
        if (classes != null && !classes.isBlank()) {
            String[] split = classes.trim().split("\\s+");
            tab.getStyleClass().addAll(split);
        }
    }

    private void applyTabTooltip(ScreenProperties properties, Tab tab) {
        String text = properties.tooltip();
        if (text == null || text.isBlank()) {
            return;
        }

        tab.setTooltip(new Tooltip(text));
    }

    private void applyColumnId(ScreenProperties properties, TableColumn<?, ?> column) {
        String id = properties.id();
        if (id != null && !id.isBlank()) {
            column.setId(id);
        }
    }

    private void applyColumnStyle(ScreenProperties properties, TableColumn<?, ?> column) {
        if (properties.style() != null && !properties.style().isBlank()) {
            column.setStyle(properties.style());
        }

        String classes = properties.styleClass();
        if (classes != null && !classes.isBlank()) {
            String[] split = classes.trim().split("\\s+");
            column.getStyleClass().addAll(split);
        }
    }

    private void applyEditable(ScreenProperties properties, Node node) {
        boolean editable = properties.editable();

        if (node instanceof TextInputControl tic) {
            tic.setEditable(editable);
            return;
        }

        if (node instanceof TextEntryLabel tel) {
            tel.getTextField().setEditable(editable);
            return;
        }

        if (node instanceof TreeView<?> treeView) {
            treeView.setEditable(editable);
        }
    }

    private void applyWrapText(ScreenProperties properties, Node node) {
        boolean wrap = properties.wrapText();

        if (node instanceof Label label) {
            label.setWrapText(wrap);
            return;
        }

        if (node instanceof TextArea textArea) {
            textArea.setWrapText(wrap);
            return;
        }

        if (node instanceof Labeled labeled) {
            labeled.setWrapText(wrap);
        }
    }

    private void applySplitPaneResizability(ScreenProperties properties, Node node) {
        boolean resizableWithParent = properties.resizable();

        if (node instanceof SplitPane splitPane) {
            if (!resizableWithParent) {
                applySplitPaneFixedStyle(splitPane);
            }
        }

        if (node.getParent() instanceof SplitPane) {
            SplitPane.setResizableWithParent(node, resizableWithParent);
            return;
        }

        if (resizableWithParent) {
            return;
        }

        ChangeListener<Parent> listener = new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Parent> obs, Parent oldParent, Parent newParent) {
                if (newParent instanceof SplitPane splitPane) {
                    SplitPane.setResizableWithParent(node, false);
                    applySplitPaneFixedStyle(splitPane);
                    node.parentProperty().removeListener(this);
                }
            }
        };

        node.parentProperty().addListener(listener);
    }

    private void applySplitPaneFixedStyle(SplitPane splitPane) {
        if (!splitPane.getStyleClass().contains("splitpane-fixed")) {
            splitPane.getStyleClass().add("splitpane-fixed");
        }

        Object applied = splitPane.getProperties().get(SPLIT_PANE_FIXED_KEY);
        if (Boolean.TRUE.equals(applied)) {
            return;
        }

        Runnable decorator = () -> decorateSplitPaneDividers(splitPane);
        splitPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                Platform.runLater(decorator);
            }
        });

        Platform.runLater(decorator);
        splitPane.getProperties().put(SPLIT_PANE_FIXED_KEY, Boolean.TRUE);
    }

    private void decorateSplitPaneDividers(SplitPane splitPane) {
        splitPane.lookupAll(".split-pane-divider").forEach(divider -> {
            divider.setMouseTransparent(true);
            divider.setPickOnBounds(false);
            divider.setStyle("-fx-padding: 0; -fx-background-color: transparent; -fx-border-color: transparent;");
        });
    }

    private void applyTreeViewProperties(ScreenProperties properties, Node node) {
        if (!(node instanceof TreeView<?> treeView)) {
            return;
        }

        treeView.setShowRoot(properties.showRoot());
    }

    public void applyToTreeItem(ScreenProperties properties, TreeItem<?> treeItem) {
        if (properties == null || treeItem == null) {
            return;
        }

        treeItem.setExpanded(properties.expanded());
    }
}
