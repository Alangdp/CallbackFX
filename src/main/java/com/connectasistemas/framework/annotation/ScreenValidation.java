package com.connectasistemas.framework.annotation;

import com.connectasistemas.framework.enums.ValidationDataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define regras de validação para campos declarados em uma tela anotada com {@code @Screen}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScreenValidation {
    int maxLength() default -1;
    int minLength() default 0;
    double minValue() default Double.NEGATIVE_INFINITY;
    double maxValue() default Double.POSITIVE_INFINITY;
    boolean allowLetters() default true;
    boolean allowNumbers() default true;
    boolean allowWhitespace() default true;
    boolean allowSymbols() default true;
    boolean required() default false;
    String minDate() default "";
    String maxDate() default "";
    String datePattern() default "yyyy-MM-dd";
    ValidationDataType dataType() default ValidationDataType.TEXT;
}
