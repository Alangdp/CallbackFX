package com.connectasistemas.framework.annotation;

import com.connectasistemas.framework.enums.ValidationDataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declara metadados de validação para campos anotados com {@link ScreenField}.
 * Combina limites de texto, números e datas, além de regras de obrigatoriedade.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScreenValidation {
    /**
     * Comprimento máximo para o valor textual; -1 remove o limite.
     */
    int maxLength() default -1;
    /**
     * Comprimento mínimo exigido.
     */
    int minLength() default 0;
    /**
     * Valor numérico mínimo aceito.
     */
    double minValue() default Double.NEGATIVE_INFINITY;
    /**
     * Valor numérico máximo aceito.
     */
    double maxValue() default Double.POSITIVE_INFINITY;
    /**
     * Permite letras no valor inserido.
     */
    boolean allowLetters() default true;
    /**
     * Permite números no valor inserido.
     */
    boolean allowNumbers() default true;
    /**
     * Permite espaços em branco.
     */
    boolean allowWhitespace() default true;
    /**
     * Permite símbolos e pontuações.
     */
    boolean allowSymbols() default true;
    /**
     * Indica obrigatoriedade do campo.
     */
    boolean required() default false;
    /**
     * Data mínima aceita conforme {@link #datePattern()}.
     */
    String minDate() default "";
    /**
     * Data máxima aceita conforme {@link #datePattern()}.
     */
    String maxDate() default "";
    /**
     * Padrão de formatação usado nas datas mínimas e máximas.
     */
    String datePattern() default "yyyy-MM-dd";
    /**
     * Tipo de dado esperado pelo validador.
     */
    ValidationDataType dataType() default ValidationDataType.TEXT;
    /**
     * Mostra mensagens padrão de validação ao usuário.
     */
    boolean showMessage() default false;
    /**
     * Acrônimo do elemento que dispara a validação (ex.: botão salvar).
     */
    String validateOn() default "";
}
