package com.connectasistemas.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Especifica tamanhos, espaçamentos e comportamento de crescimento para campos anotados com {@link ScreenField}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScreenFieldSize {
    /**
     * Largura preferencial do elemento.
     */
    double width() default 0;
    /**
     * Altura preferencial do elemento.
     */
    double height() default 0;

    /**
     * Largura mínima aceita (px ou percentual 0-1).
     */
    double minWidth() default 0;
    /**
     * Largura máxima aceita (px ou percentual 0-1).
     */
    double maxWidth() default 0;
    /**
     * Altura mínima aceita (px ou percentual 0-1).
     */
    double minHeight() default 0;
    /**
     * Altura máxima aceita (px ou percentual 0-1).
     */
    double maxHeight() default 0;

    /**
     * Remove limites horizontais aplicados pelo layout.
     */
    boolean unlimitedWidth() default false;
    /**
     * Remove limites verticais aplicados pelo layout.
     */
    boolean unlimitedHeight() default false;

    /**
     * Largura padrão para labels associados (EntryLabel etc.).
     */
    double labelWidth () default 0;
    /**
     * Altura padrão para labels associados.
     */
    double labelHeight() default 0;

    /**
     * Padding em ordem top/right/bottom/left.
     */
    int[] padding() default {};

    /**
     * Margem em ordem top/right/bottom/left.
     */
    int[] margin() default {};

    /**
     * Expande verticalmente quando houver espaço extra.
     */
    boolean vgrow() default false;

    /**
     * Expande horizontalmente quando houver espaço extra.
     */
    boolean hgrow() default false;

    /**
     * Espaçamento entre sub-elementos internos.
     */
    double spacing() default 0;
}