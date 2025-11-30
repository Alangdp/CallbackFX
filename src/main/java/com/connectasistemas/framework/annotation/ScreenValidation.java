package com.connectasistemas.framework.annotation;

import com.connectasistemas.framework.enums.ValidationDataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para declarar regras de validação de campos em telas.
 * Pode ser aplicada a campos com {@link ScreenField},
 *
 * Cada elemento desta anotação representa uma restrição ou metadado usado pelo validador:
 *
 * @return maxLength - comprimento máximo permitido para texto. Valor padrão -1 indica que não há limite.
 * @return minLength - comprimento mínimo permitido para texto. Valor padrão 0.
 * @return minValue - valor numérico mínimo permitido. Valor padrão Double.NEGATIVE_INFINITY indica sem limite inferior.
 * @return maxValue - valor numérico máximo permitido. Valor padrão Double.POSITIVE_INFINITY indica sem limite superior.
 * @return allowLetters - permite caracteres alfabéticos (letras). true por padrão.
 * @return allowNumbers - permite caracteres numéricos (dígitos). true por padrão.
 * @return allowWhitespace - permite espaços em branco (espaços, tabs). true por padrão.
 * @return allowSymbols - permite símbolos/pontuação. true por padrão.
 * @return required - indica que o campo é obrigatório (não pode ser nulo/vazio). false por padrão.
 * @return minDate - data mínima permitida, expressa como string no formato definido por datePattern.
 *                      String vazia ("") indica que não há limite inferior de data.
 * @return maxDate - data máxima permitida, expressa como string no formato definido por datePattern.
 *                      String vazia ("") indica que não há limite superior de data.
 * @return datePattern - padrão de formatação/parse de datas usado para minDate e maxDate.
 *                       Padrão "yyyy-MM-dd". Deve ser compatível com java.time.format.DateTimeFormatter.
 * @return dataType - tipo de dado esperado para validação, conforme enum ValidationDataType.
 *                     Usado para orientar validações específicas (por exemplo, TEXT, NUMBER, DATE). Padrão ValidationDataType.TEXT.
 * @return showMessage - indica se mensagens de erro devem ser exibidas ao usuário em caso de falha na validação. true por padrão.
 *                     Nesse caso, o framework vai mostrar warning de validação automaticamente.
 *
 * Observações:
 * - Combinações de restrições devem ser interpretadas em conjunto (por exemplo, maxLength e
 *   allowNumbers/allowLetters para validação de texto).
 * - Valores numéricos e de data que estejam fora dos limites configurados devem ser considerados inválidos.
 * - Para validação de datas, recomenda-se fornecer minDate/maxDate no mesmo padrão especificado por datePattern.
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
    boolean showMessage() default false;
}
