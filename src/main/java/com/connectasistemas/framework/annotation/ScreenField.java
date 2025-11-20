package com.connectasistemas.framework.annotation;

import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.interfaces.PositionElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um campo da tela que o framework ira tratar com campo
 * OBS: Isso que marca o campo com callbacks genéricos
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScreenField {
    String acronym();
    String father() default "";
    Position position() default Position.CENTER;
}