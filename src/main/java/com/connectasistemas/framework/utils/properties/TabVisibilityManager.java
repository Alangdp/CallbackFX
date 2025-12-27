package com.connectasistemas.framework.utils.properties;

import java.util.Map;
import java.util.WeakHashMap;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Controla a visibilidade de tabs sem descartá-las do cache do framework.
 */
public final class TabVisibilityManager {

    private static final class TabState {
        private TabPane parent;
        private int lastIndex = -1;
        private boolean hidden;
    }

    private static final Map<Tab, TabState> STATES = new WeakHashMap<>();

    private TabVisibilityManager() {
    }

    public static void setVisible(Tab tab, boolean visible) {
        if (tab == null) {
            return;
        }

        if (visible) {
            show(tab);
        } else {
            hide(tab);
        }
    }

    private static void hide(Tab tab) {
        TabState state = STATES.computeIfAbsent(tab, key -> new TabState());
        if (state.hidden) {
            return;
        }

        TabPane currentParent = tab.getTabPane();
        if (currentParent == null) {
            currentParent = state.parent;
        }

        if (currentParent == null) {
            return;
        }

        int currentIndex = currentParent.getTabs().indexOf(tab);
        if (currentIndex >= 0) {
            state.lastIndex = currentIndex;
            currentParent.getTabs().remove(tab);
        }

        state.parent = currentParent;
        state.hidden = true;
    }

    private static void show(Tab tab) {
        TabState state = STATES.computeIfAbsent(tab, key -> new TabState());
        TabPane currentParent = state.parent != null ? state.parent : tab.getTabPane();
        if (currentParent == null) {
            return;
        }

        if (currentParent.getTabs().contains(tab)) {
            state.hidden = false;
            state.lastIndex = currentParent.getTabs().indexOf(tab);
            state.parent = currentParent;
            return;
        }

        int insertionIndex = state.lastIndex;
        if (insertionIndex < 0 || insertionIndex > currentParent.getTabs().size()) {
            insertionIndex = currentParent.getTabs().size();
        }

        currentParent.getTabs().add(insertionIndex, tab);
        state.hidden = false;
        state.parent = currentParent;
        state.lastIndex = currentParent.getTabs().indexOf(tab);
    }
}
