package com.connectasistemas.framework.internal.utils;

import com.connectasistemas.framework.fxelements.CheckEntryLabel;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.internal.events.*;
import com.connectasistemas.framework.internal.interfaces.EventBinderEvents;
import com.connectasistemas.framework.utils.ScreenRuntimeContext;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeView;

import java.util.*;

/**
 * Responsavel por "Ativar" os eventos do javaFx e interligar eles com os
 * callbacks personalizados. O mapa de eventos agora reside em
 * {@link ScreenRuntimeContext}.
 */
public class EventBinder {

    private static final ScreenRuntimeContext ctx = ScreenRuntimeContext.getDefault();

    /**
     * Vincula os eventos do javaFX com as funcoes de callback baseado na entrada.
     *
     * @param acronym           sigla do campo
     * @param element           instancia do elemento
     * @param screenInstance    instancia da tela
     * @param callbacksInstance instancia do controller
     */
    public static void attach(
            String acronym,
            Object element,
            Object screenInstance,
            Object callbacksInstance
    ) {
        EventBinderEvents binder = null;

        if (element instanceof TextInputControl input) {
            binder = new EventBinderTextInputControl(acronym, input, screenInstance, callbacksInstance);
        } else if (element instanceof CheckBox checkBox) {
            binder = new EventBinderCheckBox(acronym, checkBox, screenInstance, callbacksInstance);
        } else if (element instanceof ComboBox<?> comboBox) {
            binder = new EventBinderComboBox(acronym, comboBox, screenInstance, callbacksInstance);
        } else if (element instanceof Button button) {
            binder = new EventBinderButton(acronym, button, screenInstance, callbacksInstance);
        } else if (element instanceof TextEntryLabel txt) {
            binder = new EventBinderTextEntryLabel(acronym, txt, screenInstance, callbacksInstance);
        } else if (element instanceof CheckEntryLabel checkEntry) {
            binder = new EventBinderCheckEntryLabel(acronym, checkEntry, screenInstance, callbacksInstance);
        } else if (element instanceof ListView<?> listView) {
            binder = new EventBinderListView(acronym, listView, screenInstance, callbacksInstance);
        } else if (element instanceof TreeView<?> treeView) {
            binder = new EventBinderTreeView(acronym, treeView, screenInstance, callbacksInstance);
        } else if (element instanceof TableView<?> tableView) {
            binder = new EventBinderTableView(acronym, tableView, screenInstance, callbacksInstance);
        } else if (element instanceof TableColumn<?, ?> tableColumn) {
            binder = new EventBinderTableColumn(acronym, tableColumn, screenInstance, callbacksInstance);
        } else if (element instanceof Tab tab) {
            binder = new EventBinderTab(acronym, tab, screenInstance, callbacksInstance);
        }

        attachElement(acronym, element, screenInstance, callbacksInstance, binder);
    }

    /**
     * Registra o elemento no mapa interno e executa a configuracao especifica de
     * eventos.
     */
    private static void attachElement(
            String acronym,
            Object element,
            Object screenInstance,
            Object callbacksInstance,
            EventBinderEvents binder
    ) {
        if (screenInstance == null || element == null) {
            return;
        }

        Map<Object, Map<Object, List<Runnable>>> eventMap = ctx.getEventMap();
        eventMap.computeIfAbsent(screenInstance, k -> new HashMap<>());
        Map<Object, List<Runnable>> screenEvents = eventMap.get(screenInstance);

        if (screenEvents.containsKey(element)) {
            return;
        }

        // Permite que o controller configure o elemento antes dos eventos padrao
        CallbackInvoker.call(callbacksInstance, screenInstance, "config", acronym);

        List<Runnable> unregisters = new ArrayList<>();

        if (binder != null) {
            unregisters.addAll(binder.applyEntcamSaicamEvent());
            unregisters.addAll(binder.applyAltcamEvent());
            unregisters.addAll(binder.applyTecladEvent());
            unregisters.addAll(binder.applyCustomEvents());
        }

        screenEvents.put(element, unregisters);
    }

    /**
     * Deleta todos os eventos vinculados a tela de entrada.
     *
     * @param screenInstance instancia da tela
     */
    public static void deleteEvents(Object screenInstance) {
        if (screenInstance == null) return;

        Map<Object, Map<Object, List<Runnable>>> eventMap = ctx.getEventMap();
        Map<Object, List<Runnable>> screenEvents = eventMap.get(screenInstance);
        if (screenEvents == null) return;

        for (var entry : screenEvents.entrySet()) {
            for (Runnable r : entry.getValue()) {
                try {
                    r.run();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }

        eventMap.remove(screenInstance);
    }
}
