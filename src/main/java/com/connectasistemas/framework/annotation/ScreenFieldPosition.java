package com.connectasistemas.framework.annotation;

import com.connectasistemas.framework.enums.Position;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Usado em conjunto com @ScreenField para a posição de um elemento
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScreenFieldPosition {
    // Alinhamento do elemento
    Position alignment() default Position.CENTER;
}
