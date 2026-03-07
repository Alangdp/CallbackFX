package com.connectasistemas.framework.utils.drag;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import com.connectasistemas.framework.utils.delimiter.Delimiter;

import javafx.scene.input.DragEvent;

/**
 * Especificação para criação de {@link Delimiter}s gerenciados pelo utilitário de drag.
 * Cada spec gera um delimiter que atualiza a seleção associada ao overlay.
 */
public final class DelimiterSpec {
    private final double width;
    private final double height;
    private final double offsetX;
    private final double offsetY;
    private final String label;
    private final String key;
    private final BiConsumer<DragEvent, Delimiter> onTrigger;

    private DelimiterSpec(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.offsetX = builder.offsetX;
        this.offsetY = builder.offsetY;
        this.label = builder.label;
        this.key = builder.key;
        this.onTrigger = builder.onTrigger;
    }

    Delimiter toDelimiter(AtomicReference<String> selectionRef) {
        BiConsumer<DragEvent, Delimiter> consumer = (event, delimiter) -> {
            if (key != null) {
                selectionRef.set(key);
            }
            if (onTrigger != null) {
                onTrigger.accept(event, delimiter);
            }
        };
        return new Delimiter(width, height, offsetX, offsetY, label, consumer);
    }
    
    /**
     * Retorna a chave identificadora deste delimiter.
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Retorna o callback onTrigger.
     */
    public BiConsumer<DragEvent, Delimiter> getOnTrigger() {
        return onTrigger;
    }
    
    /**
     * Retorna a largura em porcentagem.
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * Retorna a altura em porcentagem.
     */
    public double getHeight() {
        return height;
    }
    
    /**
     * Retorna o offset X em porcentagem.
     */
    public double getOffsetX() {
        return offsetX;
    }
    
    /**
     * Retorna o offset Y em porcentagem.
     */
    public double getOffsetY() {
        return offsetY;
    }

    public static Builder builder(String key) {
        return new Builder(key);
    }

    /**
     * Builder responsável por preencher os parâmetros opcionais do delimiter.
     */
    public static final class Builder {
        private final String key;
        private double width;
        private double height;
        private double offsetX;
        private double offsetY;
        private String label = "";
        private BiConsumer<DragEvent, Delimiter> onTrigger;

        private Builder(String key) {
            this.key = key;
        }

        public Builder size(double width, double height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder offset(double offsetX, double offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder onTrigger(BiConsumer<DragEvent, Delimiter> consumer) {
            this.onTrigger = consumer;
            return this;
        }

        public DelimiterSpec build() {
            return new DelimiterSpec(this);
        }
    }
}
