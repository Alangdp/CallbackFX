package com.connectasistemas.framework.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.connectasistemas.framework.internal.position.BorderPanePosition;

/**
 * Marca uma classe como tela reconhecida pelo framework. Os valores definidos
 * nesta anotação alimentam o {@link com.connectasistemas.framework.internal.utils.ScreenMetadata}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Screen {
    /**
     * Título padrão exibido no {@link javafx.stage.Stage} principal.
     */
    String title();

    /**
     * Largura inicial sugerida para a janela.
     */
    int width() default 800;

    /**
     * Altura inicial sugerida para a janela.
     */
    int height() default 600;

    /**
     * Classe que concentra os callbacks usados pelo {@code EventBinder}.
     */
    Class<?> callbacks() default Void.class;

    /**
     * Root layout utilizado durante a montagem (ex.: {@link javafx.scene.layout.BorderPane}).
     */
    Class<?> region() default BorderPanePosition.class;
}