package com.connectasistemas.framework.annotation;

import com.connectasistemas.framework.enums.Position;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declara um campo gerenciado pelo assembler. O acrônimo vira o identificador
 * interno utilizado na composição, eventos e validações.
 * <p>
 * Exemplo:
 * </p>
 * <pre>
 * {@code
 * @ScreenField(acronym = "btnSave", father = "topPane", position = Position.LEFT, order = 1)
 * private Button btnSave;
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScreenField {
    /**
     * Identificador único do elemento dentro da tela.
     */
    String acronym();

    /**
     * Acrônimo do elemento pai. Quando vazio o campo é adicionado na raiz.
     */
    String father() default "";

    /**
     * Posição relativa ao pai; varia conforme o tipo de layout.
     */
    Position position() default Position.CENTER;

    /**
     * Texto associado ao elemento (rótulo, título, placeholder, etc.).
     */
    String literal() default "";

    /**
     * Ordem de processamento; valores menores são avaliados primeiro.
     */
    int order() default 0;

    /**
     * Indica se o campo instancia um {@link com.connectasistemas.framework.interfaces.CustomElement}.
     */
    boolean custom() default false;
}