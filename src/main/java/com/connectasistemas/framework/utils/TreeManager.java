package com.connectasistemas.framework.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Utilidades para inspeção de {@link TreeItem} associados a telas anotadas com {@code @Screen}.
 */
public final class TreeManager {

    private TreeManager() {
    }

    /**
     * Recupera o texto exibido pelo {@link TreeItem} informado.
     *
     * @param item instância alvo
     * @return texto apresentado ou vazio quando não houver valor
     */
    public static String getLabel(TreeItem<?> item) {
        Object value = item != null ? item.getValue() : null;
        return value != null ? value.toString() : "";
    }

    /**
     * Monta o caminho textual do nó até a raiz usando '/' como separador.
     *
     * @param item nó alvo
     * @return caminho montado ou vazio quando não for possível determinar
     */
    public static String getPath(TreeItem<?> item) {
        return getPath(item, "/");
    }

    /**
     * Monta o caminho textual do nó até a raiz, permitindo personalizar o separador.
     *
     * @param item      nó alvo
     * @param separator separador entre cada segmento
     * @return caminho montado ou vazio quando não for possível determinar
     */
    public static String getPath(TreeItem<?> item, String separator) {
        if (item == null) {
            return "";
        }

        String effectiveSeparator = StringUtils.isBlank(separator) ? "/" : separator;
        List<String> segments = new ArrayList<>();
        TreeItem<?> current = item;

        while (current != null) {
            String label = getLabel(current);
            if (!StringUtils.isBlank(label)) {
                segments.add(label);
            }
            current = current.getParent();
        }

        if (segments.isEmpty()) {
            return "";
        }

        Collections.reverse(segments);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            if (i > 0) {
                builder.append(effectiveSeparator);
            }
            builder.append(segments.get(i));
        }
        return builder.toString();
    }

    /**
     * Obtém o acrônimo definido pelo campo anotado que originou o {@link TreeItem}.
     *
     * @param screenInstance instância da tela atual
     * @param item           nó criado pelo assembler
     * @return acrônimo encontrado ou vazio quando não associado
     */
    public static String getAcronym(Object screenInstance, TreeItem<?> item) {
        if (screenInstance == null || item == null) {
            return "";
        }

        Map<String, Object> screenElements = ScreenManagerSharedData.getCache().get(screenInstance);
        if (screenElements == null) {
            return "";
        }

        for (Map.Entry<String, Object> entry : screenElements.entrySet()) {
            if (entry.getValue() == item) {
                return entry.getKey();
            }
        }
        return "";
    }

    /**
     * Retorna o {@link TreeItem} associado ao acrônimo informado, quando disponível.
     *
     * @param screenInstance instância da tela atual
     * @param acronym        identificador do campo anotado
     * @return nó correspondente ou {@code null} caso não exista
     */
    public static TreeItem<?> getTreeItem(Object screenInstance, String acronym) {
        if (screenInstance == null || StringUtils.isBlank(acronym)) {
            return null;
        }

        Map<String, Object> screenElements = ScreenManagerSharedData.getCache().get(screenInstance);
        if (screenElements == null) {
            return null;
        }

        Object candidate = screenElements.get(acronym);
        if (candidate instanceof TreeItem<?>) {
            return (TreeItem<?>) candidate;
        }

        return null;
    }

    /**
     * Obtém o item atualmente selecionado em uma {@link TreeView} registrada pelo acrônimo informado.
     *
     * @param screenInstance instância da tela atual
     * @param treeAcronym    identificador do campo que representa a TreeView
     * @return item selecionado ou {@code null} quando não houver seleção
     */
    public static TreeItem<?> getSelectedItem(Object screenInstance, String treeAcronym) {
        TreeView<?> treeView = getTreeView(screenInstance, treeAcronym);
        return getSelectedItem(treeView);
    }

    /**
     * Obtém o item atualmente selecionado diretamente a partir da {@link TreeView} informada.
     *
     * @param treeView instância alvo
     * @return item selecionado ou {@code null}
     */
    public static TreeItem<?> getSelectedItem(TreeView<?> treeView) {
        if (treeView == null || treeView.getSelectionModel() == null) {
            return null;
        }

        return treeView.getSelectionModel().getSelectedItem();
    }

    /**
     * Alias semântico para {@link #getSelectedItem(Object, String)}, usado em callbacks.
     *
     * @param screenInstance instância da tela atual
     * @param treeAcronym    identificador do campo que representa a TreeView
     * @return item selecionado ou {@code null}
     */
    public static TreeItem<?> getActualItem(Object screenInstance, String treeAcronym) {
        return getSelectedItem(screenInstance, treeAcronym);
    }

    /**
     * Alias semântico para {@link #getSelectedItem(TreeView)}.
     *
     * @param treeView instância alvo
     * @return item selecionado ou {@code null}
     */
    public static TreeItem<?> getActualItem(TreeView<?> treeView) {
        return getSelectedItem(treeView);
    }

    /**
     * Retorna o valor associado ao item selecionado da TreeView.
     *
     * @param screenInstance instância da tela atual
     * @param treeAcronym    identificador do campo que representa a TreeView
     * @return valor do item selecionado ou {@code null}
     */
    public static Object getSelectedValue(Object screenInstance, String treeAcronym) {
        TreeItem<?> selected = getSelectedItem(screenInstance, treeAcronym);
        return selected != null ? selected.getValue() : null;
    }

    /**
     * Retorna o valor associado ao item selecionado da TreeView.
     *
     * @param treeView instância alvo
     * @return valor do item selecionado ou {@code null}
     */
    public static Object getSelectedValue(TreeView<?> treeView) {
        TreeItem<?> selected = getSelectedItem(treeView);
        return selected != null ? selected.getValue() : null;
    }

    /**
     * Define explicitamente o item selecionado da TreeView associada ao acrônimo informado.
     *
     * @param screenInstance instância da tela atual
     * @param treeAcronym    identificador do campo que representa a TreeView
     * @param item           nó que deve ser selecionado
     */
    public static void selectItem(Object screenInstance, String treeAcronym, TreeItem<?> item) {
        TreeView<?> treeView = getTreeView(screenInstance, treeAcronym);
        selectItem(treeView, item);
    }

    /**
     * Define explicitamente o item selecionado da {@link TreeView} informada.
     *
     * @param treeView instância alvo
     * @param item     nó que deve ser selecionado
     */
    public static void selectItem(TreeView<?> treeView, TreeItem<?> item) {
        if (treeView == null || treeView.getSelectionModel() == null || item == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        TreeView<Object> castTree = (TreeView<Object>) treeView;
        @SuppressWarnings("unchecked")
        TreeItem<Object> castItem = (TreeItem<Object>) item;
        castTree.getSelectionModel().select(castItem);
    }

    /**
     * Retorna uma chave amigável para uso em instruções {@code switch}, preferindo o acrônimo do item e
     * caindo para o rótulo exibido quando necessário.
     *
     * @param screenInstance instância da tela atual
     * @param item           nó alvo
     * @return chave para seleção condicional ou vazio
     */
    public static String getCaseKey(Object screenInstance, TreeItem<?> item) {
        if (item == null) {
            return "";
        }

        String acronym = getAcronym(screenInstance, item);
        if (!StringUtils.isBlank(acronym)) {
            return acronym;
        }

        return getLabel(item);
    }

    /**
     * Retorna a chave amigável do item atualmente selecionado, facilitando {@code switch} diretos.
     *
     * @param screenInstance instância da tela atual
     * @param treeAcronym    identificador do campo que representa a TreeView
     * @return chave da seleção atual ou vazio
     */
    public static String getSelectedCaseKey(Object screenInstance, String treeAcronym) {
        TreeItem<?> selected = getSelectedItem(screenInstance, treeAcronym);
        return getCaseKey(screenInstance, selected);
    }

    /**
     * Retorna a chave amigável do item selecionado diretamente pela {@link TreeView}.
     *
     * @param screenInstance instância da tela atual
     * @param treeView       instância alvo
     * @return chave da seleção atual ou vazio
     */
    public static String getSelectedCaseKey(Object screenInstance, TreeView<?> treeView) {
        TreeItem<?> selected = getSelectedItem(treeView);
        return getCaseKey(screenInstance, selected);
    }

    /**
     * Indica se o {@link TreeItem} atual foi declarado pelo acrônimo informado.
     *
     * @param screenInstance instância da tela atual
     * @param item           nó atual
     * @param acronym        identificador esperado
     * @return {@code true} quando o acrônimo corresponde ao nó
     */
    public static boolean matchesAcronym(Object screenInstance, TreeItem<?> item, String acronym) {
        if (item == null || StringUtils.isBlank(acronym)) {
            return false;
        }
        String currentAcronym = getAcronym(screenInstance, item);
        return acronym.equals(currentAcronym);
    }

    /**
     * Gera uma descrição curta contendo rótulo e acrônimo do nó.
     *
     * @param screenInstance instância da tela atual
     * @param item           nó alvo
     * @return descrição amigável para logs e mensagens
     */
    public static String describe(Object screenInstance, TreeItem<?> item) {
        String label = getLabel(item);
        String acronym = getAcronym(screenInstance, item);

        if (StringUtils.isBlank(label)) {
            return acronym;
        }

        if (StringUtils.isBlank(acronym)) {
            return label;
        }

        return StringUtils.concat(label, " (", acronym, ")");
    }

    private static TreeView<?> getTreeView(Object screenInstance, String acronym) {
        if (screenInstance == null || StringUtils.isBlank(acronym)) {
            return null;
        }

        Map<String, Object> screenElements = ScreenManagerSharedData.getCache().get(screenInstance);
        if (screenElements == null) {
            return null;
        }

        Object candidate = screenElements.get(acronym);
        if (candidate instanceof TreeView<?>) {
            return (TreeView<?>) candidate;
        }

        return null;
    }
}
