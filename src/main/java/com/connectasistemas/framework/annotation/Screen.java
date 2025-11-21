package com.connectasistemas.framework.annotation;

import com.connectasistemas.framework.utils.position.BorderPanePosition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca a classe como uma tela
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Screen {
    String title();
    int width() default 800;
    int height() default 600;
    // Classe que contém os callbacks
    Class<?> callbacks() default Void.class;
    Class<?> region() default BorderPanePosition.class;
}