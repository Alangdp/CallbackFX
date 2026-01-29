package com.connectasistemas.framework.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.connectasistemas.framework.internal.position.BorderPanePosition;

/**
 * Marca a classe como uma tela
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Screen {
    // Define o título da tela
    String title();

    // Define a largura da tela
    int width() default 800;

    // Define a altura da tela
    int height() default 600;

    // Classe que contém os callbacks
    Class<?> callbacks() default Void.class;
    
    // Define qual será o root layout da tela
    // Ex: BorderPane, AnchorPane, etc
    Class<?> region() default BorderPanePosition.class;
}