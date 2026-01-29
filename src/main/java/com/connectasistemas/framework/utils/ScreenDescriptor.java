package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.internal.utils.ScreenMetadata;

import javafx.scene.layout.Region;

/**
 * Representa um recorte imutavel dos principais dados de uma tela ativa.
 * A ideia eh expor informacoes uteis sem obrigar o consumidor a conhecer os
 * detalhes internos armazenados em {@code ScreenMetadata}.
 */
public final class ScreenDescriptor {

    private final String title;
    private final int width;
    private final int height;
    private final Region root;
    private final Object screenInstance;
    private final Class<?> screenClass;

    private ScreenDescriptor(
            String title,
            int width,
            int height,
            Region root,
            Object screenInstance,
            Class<?> screenClass) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.root = root;
        this.screenInstance = screenInstance;
        this.screenClass = screenClass;
    }

    static ScreenDescriptor from(ScreenMetadata metadata, Object instance) {
        if (metadata == null) {
            throw new IllegalArgumentException(StringUtils.concat(
                    "ScreenMetadata nao pode ser nulo"));
        }

        Class<?> resolvedClass = instance != null ? instance.getClass() : null;
        return new ScreenDescriptor(
                metadata.getTitle(),
                metadata.getWidth(),
                metadata.getHeight(),
                metadata.root(),
                instance,
                resolvedClass);
    }

    /**
     * Retorna o titulo configurado via {@code @Screen}.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Largura ideal da janela, conforme declarada na anotacao.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Altura ideal da janela, conforme declarada na anotacao.
     */
    public int getHeight() {
        return height;
    }

    /**
     * No raiz que foi montado para a tela atual.
     */
    public Region getRoot() {
        return root;
    }

    /**
     * Instancia concreta da tela anotada com {@code @Screen}, quando disponivel.
     */
    public Object getScreenInstance() {
        return screenInstance;
    }

    /**
     * Classe concreta da tela atualmente carregada.
     */
    public Class<?> getScreenClass() {
        return screenClass;
    }
}
