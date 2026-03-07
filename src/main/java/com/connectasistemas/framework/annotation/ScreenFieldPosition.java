package com.connectasistemas.framework.annotation;

import com.connectasistemas.framework.enums.Position;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Complementa {@link ScreenField} para refinar o alinhamento dentro do pai.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScreenFieldPosition {
    /**
     * Alinhamento relativo ao pai conforme {@link Position}.
     */
    Position alignment() default Position.CENTER;
}
