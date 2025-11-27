package com.connectasistemas.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Usado em conjunto com @ScreenField para indicar o tamanho de um elemento
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScreenFieldSize {
    // Define o tamanho do elemento
    double width() default 0;
    double height() default 0;

    // Define se o tamanho máximo será aplicado
    // OBS: se aplicado ignora o width e height
    boolean maxWidth() default false;
    boolean maxHeight() default false;

    // Define o tamanho em labels
    double labelWidth () default 0;
    double labelHeight() default 0;

    // Padding: 1=top, 2=right, 3=bottom, 4=left
    // EXE: @ScreenFieldSize(padding = {10, 20, 10, 20})
    int[] padding() default {};

    // Margin: 1=top, 2=right, 3=bottom, 4=left
    // EXE: @ScreenFieldSize(margin = {10, 20, 10, 20})
    int[] margin() default {};

    // Define o tipo de crescimento vertical
    boolean vgrow() default false;

    // Define o tipo de crescimento horizontal
    boolean hgrow() default false;

    // Define o espaçamento
    double spacing() default 0;
}