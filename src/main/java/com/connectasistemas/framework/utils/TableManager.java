package com.connectasistemas.framework.utils;


import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.internal.utils.PropertiesBinderGeneric;
import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;

import javafx.scene.control.TableColumn;

/**
 * Conjunto de utilidades para manipular {@link TableColumn} em tempo de execução.
 */
public final class TableManager {

    private static final PropertiesBinderGeneric PROPERTIES_BINDER = new PropertiesBinderGeneric();

    private TableManager() {
    }

    /**
     * Recupera a coluna registrada com o acrônimo informado.
     *
     * @param screenInstance instância anotada com {@code @Screen}
     * @param acronym identificador aplicado no {@code @ScreenField}
     * @return coluna encontrada
     */
    public static TableColumn<?, ?> getColumn(Object screenInstance, String acronym) {
        Object element = ScreenManagerSharedData.getScreenData(screenInstance, acronym);

        if (element instanceof TableColumn<?, ?> column) {
            return column;
        }

        throw new IllegalArgumentException(StringUtils.concat(
                "Elemento informado não é uma TableColumn: ", acronym));
    }

    public static void setHeader(Object screenInstance, String acronym, String text) {
        setHeader(getColumn(screenInstance, acronym), text);
    }

    public static void setHeader(TableColumn<?, ?> column, String text) {
        if (column == null) {
            return;
        }

        column.setText(text != null ? text : "");
    }

    public static void setVisible(Object screenInstance, String acronym, boolean visible) {
        setVisible(getColumn(screenInstance, acronym), visible);
    }

    public static void setVisible(TableColumn<?, ?> column, boolean visible) {
        if (column == null) {
            return;
        }

        column.setVisible(visible);
    }

    public static void setEditable(Object screenInstance, String acronym, boolean editable) {
        setEditable(getColumn(screenInstance, acronym), editable);
    }

    public static void setEditable(TableColumn<?, ?> column, boolean editable) {
        if (column == null) {
            return;
        }

        column.setEditable(editable);
    }

    public static void setSortable(Object screenInstance, String acronym, boolean sortable) {
        setSortable(getColumn(screenInstance, acronym), sortable);
    }

    public static void setSortable(TableColumn<?, ?> column, boolean sortable) {
        if (column == null) {
            return;
        }

        column.setSortable(sortable);
    }

    public static void setStyle(Object screenInstance, String acronym, String style) {
        setStyle(getColumn(screenInstance, acronym), style);
    }

    public static void setStyle(TableColumn<?, ?> column, String style) {
        if (column == null) {
            return;
        }

        column.setStyle(style);
    }

    public static void setStyleClasses(Object screenInstance, String acronym, String... classes) {
        setStyleClasses(getColumn(screenInstance, acronym), classes);
    }

    public static void setStyleClasses(TableColumn<?, ?> column, String... classes) {
        if (column == null) {
            return;
        }

        column.getStyleClass().clear();
        if (classes == null) {
            return;
        }

        for (String clazz : classes) {
            if (clazz != null && !clazz.isBlank()) {
                column.getStyleClass().add(clazz.trim());
            }
        }
    }

    public static void applyProperties(Object screenInstance, String acronym, ScreenProperties properties) {
        applyProperties(getColumn(screenInstance, acronym), properties);
    }

    public static void applyProperties(TableColumn<?, ?> column, ScreenProperties properties) {
        PROPERTIES_BINDER.applyToTableColumn(properties, column);
    }
}
