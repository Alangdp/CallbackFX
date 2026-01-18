package com.connectasistemas.framework.views;

import com.connectasistemas.framework.interfaces.CustomElement;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Region;

public class TopMenu extends MenuBar implements CustomElement{

    public TopMenu() {
        super();
        Menu configurationMenu = createConfigurationMenu();
        Menu pagesMenu = createPagesMenu();
        Menu toolsMenu = createToolsMenu();
        addMenusToBar(configurationMenu, pagesMenu, toolsMenu);
    }

    private Menu createConfigurationMenu() {
        Menu configurationMenu = new Menu("Configurations");

        // Botão de grid
        CheckMenuItem showGridOnCanva = new CheckMenuItem("Grid");

        // Botão de devMode
        CheckMenuItem devMode = new CheckMenuItem("Dev Mode");

        configurationMenu.getItems().addAll(showGridOnCanva, devMode);
        return configurationMenu;
    }

    private Menu createPagesMenu() {
        Menu pagesMenu = new Menu("Pages");

        return pagesMenu;
    }

    private Menu createToolsMenu() {
        Menu toolsMenu = new Menu("Tools");

        return toolsMenu;   
    }

    // Adiciona todos os menus recebidos ao MenuBar
    private void addMenusToBar(Menu... menus) {
        this.getMenus().addAll(menus);
    }

    @Override
    public Class<? extends Region> getType() {
        return MenuBar.class;
    }
}