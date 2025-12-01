package com.connectasistemas.framework.utils.validation;

import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.interfaces.ValidationBinder;
import com.connectasistemas.framework.utils.CallbackInvoker;
import com.connectasistemas.framework.utils.MessageUtil;
import com.connectasistemas.framework.utils.Status;
import com.connectasistemas.framework.utils.StringUtils;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Binder genérico responsável por aplicar validações declaradas via
 * {@link ScreenValidation}.
 */
public class ValidationBinderGeneric implements ValidationBinder {

    /**
     * Aplica todas as validações declaradas em ScreenValidation ao Node fornecido.
     */
    @Override
    public boolean applyAll(ScreenValidation validation,
            Node node,
            String acronym,
            Object screenInstance,
            Object callbacksInstance) {

        if (validation == null || node == null) {
            return false;
        }

        TextInputControl control = resolveTextInputControl(node);
        if (control == null) {
            return false;
        }

        ValidationContext context = buildContext(validation);
        ValidationHandler handler = new ValidationHandler(screenInstance,
                callbacksInstance,
                acronym,
                validation,
                callbacksInstance != null && CallbackInvoker.exists(callbacksInstance, "valida", acronym));

        UnaryOperator<TextFormatter.Change> changeOperator = change -> {
            String newText = change.getControlNewText();

            ValidationResult lengthResult = enforceMaxLength(newText, validation);
            if (!lengthResult.valid()) {
                handler.publish(newText, lengthResult, false);
                return null;
            }

            ValidationResult policyResult = validateCharacterPolicy(newText, validation);
            if (!policyResult.valid()) {
                handler.publish(newText, policyResult, false);
                return null;
            }

            ValidationResult evaluation = evaluateText(newText, context);
            handler.publish(newText, evaluation, false);
            return change;
        };

        control.setTextFormatter(new TextFormatter<>(changeOperator));

        control.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                String text = control.getText();
                ValidationResult result = evaluateText(text, context);
                handler.publishOnBlur(text, result);
            }
        });

        control.textProperty().addListener((obs, oldV, newV) -> {
            ValidationResult result = evaluateText(newV, context);
            handler.publish(newV, result, false);
        });

        handler.publish(control.getText(), evaluateText(control.getText(), context), false);
        return true;
    }

    /**
     * Resolve o TextInputControl a partir do Node fornecido
     * 
     * @param node o Node a ser verificado
     * @return o TextInputControl correspondente, ou null se não for aplicável
     *         OBS: TextInputControl pode ser usado para definir validações em
     *         TextField, TextArea, etc.
     */
    private TextInputControl resolveTextInputControl(Node node) {
        if (node instanceof TextInputControl control) {
            return control;
        }

        if (node instanceof TextEntryLabel label) {
            return label.getTextField();
        }

        return null;
    }

    /**
     * Constrói o contexto de validação a partir da anotação ScreenValidation
     * 
     * @param validation
     * @return
     */
    private ValidationContext buildContext(ScreenValidation validation) {
        DateTimeFormatter formatter;
        try {
            formatter = DateTimeFormatter.ofPattern(validation.datePattern());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(StringUtils.concat("Padrão de data inválido em ScreenValidation: ",
                    validation.datePattern()), ex);
        }

        LocalDate minDate = parseDateLiteral(validation.minDate(), formatter);
        LocalDate maxDate = parseDateLiteral(validation.maxDate(), formatter);

        return new ValidationContext(validation, formatter, minDate, maxDate);
    }

    /**
     * Converte uma string em {@link LocalDate} usando o formatter informado.
     * Retorna null se a string for nula ou vazia.
     * Lança {@link RuntimeException} se o formato da data for inválido.
     *
     * Exemplo de uso:
     * DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
     * LocalDate d = parseDateLiteral("25/12/2024", f);
     *
     * @param literal   texto contendo a data a ser convertida
     * @param formatter formatter usado para interpretar o literal
     * @return LocalDate correspondente ou null se vazio
     */
    private LocalDate parseDateLiteral(String literal, DateTimeFormatter formatter) {
        if (literal == null || literal.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(literal, formatter);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException(StringUtils.concat("Data inválida em ScreenValidation: ", literal), ex);
        }
    }

    /**
     * Verifica se o texto está dentro do comprimento máximo permitido
     * 
     * @param text       o texto a ser verificado
     * @param validation a anotação ScreenValidation contendo as regras
     * @return true se estiver dentro do limite, false caso contrário
     */
    private ValidationResult enforceMaxLength(String text, ScreenValidation validation) {
        if (validation.maxLength() < 0) {
            return ValidationResult.VALID;
        }

        boolean withinLimit = text == null || text.length() <= validation.maxLength();
        if (!withinLimit) {
            return ValidationResult.invalid(StringUtils.concat(
                    "Valor deve ter no máximo ", validation.maxLength(), " caracteres."));
        }
        return ValidationResult.VALID;
    }

    /**
     * Verifica se o texto corresponde à política de caracteres definida
     * 
     * @param text       o texto a ser verificado
     * @param validation a anotação ScreenValidation contendo as regras
     * @return true se corresponder à política, false caso contrário
     */
    private ValidationResult validateCharacterPolicy(String text, ScreenValidation validation) {
        if (text == null || text.isEmpty()) {
            return ValidationResult.VALID;
        }

        return switch (validation.dataType()) {
            case INTEGER -> text.matches("-?\\d*")
                    ? ValidationResult.VALID
                    : ValidationResult.invalid("Informe apenas números inteiros.");
            case DECIMAL -> text.matches("-?\\d*(?:[\\.,]\\d*)?")
                    ? ValidationResult.VALID
                    : ValidationResult.invalid("Informe um número decimal válido.");
            case DATE -> text.chars().allMatch(ch -> Character.isDigit(ch)
                    || ch == '/' || ch == '-' || ch == '.' || Character.isWhitespace(ch))
                    ? ValidationResult.VALID
                    : ValidationResult.invalid("Informe a data usando apenas dígitos e separadores (/, -, .).");
            case TEXT -> checkAllowances(text, validation);
        };
    }

    /**
     * Verifica se o texto contém apenas os tipos de caracteres permitidos
     * 
     * @param text       o texto a ser verificado
     * @param validation a anotação ScreenValidation contendo as regras
     * @return true se o texto estiver em conformidade, false caso contrário
     */
    private ValidationResult checkAllowances(String text, ScreenValidation validation) {
        for (char ch : text.toCharArray()) {

            // Valida se é um caractere E deve validar ser permitido
            if (Character.isLetter(ch) && !validation.allowLetters()) {
                return ValidationResult.invalid("Letras não são permitidas para este campo.");
            }

            // Valida se é um dígito E deve validar ser permitido
            if (Character.isDigit(ch) && !validation.allowNumbers()) {
                return ValidationResult.invalid("Números não são permitidos para este campo.");
            }

            // Valida se é um espaço em branco E deve validar ser permitido
            if (Character.isWhitespace(ch) && !validation.allowWhitespace()) {
                return ValidationResult.invalid("Espaços em branco não são permitidos para este campo.");
            }

            // Valida se é um símbolo E deve validar ser permitido
            if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch) && !validation.allowSymbols()) {
                return ValidationResult.invalid("Símbolos não são permitidos para este campo.");
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Avalia o texto com base nas regras de validação fornecidas no contexto
     * 
     * @param text    o texto a ser avaliado
     * @param context o contexto de validação contendo as regras
     * @return true se o texto for válido, false caso contrário
     */
    private ValidationResult evaluateText(String text, ValidationContext context) {
        ScreenValidation validation = context.validation();

        if (text == null || text.isEmpty()) {
            if (validation.required()) {
                return ValidationResult.invalid("Campo obrigatório.");
            }
            return ValidationResult.VALID;
        }

        // Verifica comprimento mínimo
        if (validation.minLength() > 0 && text.length() < validation.minLength()) {
            return ValidationResult.invalid(StringUtils.concat(
                    "Valor deve conter pelo menos ", validation.minLength(), " caracteres."));
        }

        return switch (validation.dataType()) {
            case INTEGER -> evaluateInteger(text, validation);
            case DECIMAL -> evaluateDecimal(text, validation);
            case DATE -> evaluateDate(text, context);
            case TEXT -> ValidationResult.VALID;
        };
    }

    /**
     * Avalia se o texto representa um inteiro dentro dos limites definidos
     * 
     * @param text       o texto a ser avaliado
     * @param validation as regras de validação
     * @return true se o texto for um inteiro válido, false caso contrário
     */
    private ValidationResult evaluateInteger(String text, ScreenValidation validation) {
        if ("-".equals(text) || "+".equals(text)) {
            return ValidationResult.invalid("Informe um número inteiro válido.");
        }

        try {
            long value = Long.parseLong(text);
            if (value < validation.minValue()) {
                return ValidationResult.invalid(StringUtils.concat(
                        "Valor mínimo permitido é ", formatNumber(validation.minValue()), "."));
            }

            boolean withinMax = value <= validation.maxValue();
            if (!withinMax) {
                return ValidationResult.invalid(StringUtils.concat(
                        "Valor máximo permitido é ", formatNumber(validation.maxValue()), "."));
            }
            return ValidationResult.VALID;
        } catch (NumberFormatException ex) {
            return ValidationResult.invalid("Valor inteiro inválido.");
        }
    }

    /**
     * Avalia se o texto representa um decimal dentro dos limites definidos
     * 
     * @param text       o texto a ser avaliado
     * @param validation as regras de validação
     * @return true se o texto for um decimal válido, false caso contrário
     */
    private ValidationResult evaluateDecimal(String text, ScreenValidation validation) {
        if ("-".equals(text) || "+".equals(text) || ".".equals(text) || ",".equals(text)) {
            return ValidationResult.invalid("Informe um valor decimal válido.");
        }

        try {
            double value = parseDecimal(text);
            if (Double.isNaN(value)) {
                return ValidationResult.invalid("Valor decimal inválido.");
            }

            if (value < validation.minValue()) {
                return ValidationResult.invalid(StringUtils.concat(
                        "Valor mínimo permitido é ", formatNumber(validation.minValue()), "."));
            }

            boolean withinMax = value <= validation.maxValue();
            if (!withinMax) {
                return ValidationResult.invalid(StringUtils.concat(
                        "Valor máximo permitido é ", formatNumber(validation.maxValue()), "."));
            }
            return ValidationResult.VALID;
        } catch (NumberFormatException ex) {
            return ValidationResult.invalid("Valor decimal inválido.");
        }
    }

    /**
     * Converte o texto em um valor decimal (double), tratando vírgulas como pontos
     * 
     * @param text o texto a ser convertido
     * @return o valor decimal correspondente
     */
    private double parseDecimal(String text) {
        if (text == null || text.isBlank()) {
            return Double.NaN;
        }

        String sanitized = text.replace(',', '.');
        return Double.parseDouble(sanitized);
    }

    /**
     * Avalia se o texto representa uma data válida dentro dos limites definidos
     * 
     * @param text    o texto a ser avaliado
     * @param context o contexto de validação
     * @return true se o texto for uma data válida, false caso contrário
     */
    private ValidationResult evaluateDate(String text, ValidationContext context) {
        try {
            LocalDate value = LocalDate.parse(text, context.formatter());

            if (context.minDate() != null && value.isBefore(context.minDate())) {
                return ValidationResult.invalid(StringUtils.concat(
                        "Data mínima permitida é ", context.formatter().format(context.minDate()), "."));
            }

            if (context.maxDate() != null && value.isAfter(context.maxDate())) {
                return ValidationResult.invalid(StringUtils.concat(
                        "Data máxima permitida é ", context.formatter().format(context.maxDate()), "."));
            }

            return ValidationResult.VALID;
        } catch (DateTimeParseException ex) {
            ScreenValidation validation = context.validation();
            return ValidationResult.invalid(StringUtils.concat(
                    "Data ", text, " é inválida. Use o formato ", validation.datePattern(), "."));
        }
    }

    /**
     * Formata um número double para exibição, removendo casas decimais desnecessárias
     * 
     * @param value o valor a ser formatado
     * @return a representação em string do número
     */
    private String formatNumber(double value) {
        if (Double.isFinite(value) && value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private record ValidationResult(boolean valid, String message) {
        static final ValidationResult VALID = new ValidationResult(true, null);

        static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }

    /**
     * Contexto de validação contendo a anotação e dados derivados
     * OBS: Usa um record para simplificar o armazenamento dos dados
     */
    private record ValidationContext(ScreenValidation validation,
            DateTimeFormatter formatter,
            LocalDate minDate,
            LocalDate maxDate) {
    }

    /**
     * Manipulador de validação que gerencia o estado e as callbacks
     */
    private static final class ValidationHandler {
        private final Object screenInstance;
        private final Object callbacksInstance;
        private final String acronym;
        private final ScreenValidation validation;
        private final boolean hasValidationCallback;

        private String lastText;
        private Boolean lastValid;
        private String lastMessage;

        private ValidationHandler(Object screenInstance,
                Object callbacksInstance,
                String acronym,
                ScreenValidation validation,
                boolean hasValidationCallback) {
            this.screenInstance = screenInstance;
            this.callbacksInstance = callbacksInstance;
            this.acronym = acronym;
            this.validation = validation;
            this.hasValidationCallback = hasValidationCallback;
        }

        private void publish(String text, ValidationResult result, boolean force) {
            publishInternal(text, result, force);
        }

        private void publishOnBlur(String text, ValidationResult result) {
            publishInternal(text, result, true);
        }

        private void publishInternal(String text, ValidationResult result, boolean force) {
            boolean sameState = Objects.equals(lastText, text)
                    && Objects.equals(lastValid, result.valid())
                    && Objects.equals(lastMessage, result.message());

            if (sameState) {
                if (!force) {
                    return;
                }

                if (force && (result.message() == null || result.message().isBlank())) {
                    return;
                }
            }

            lastText = text;
            lastValid = result.valid();
            lastMessage = result.message();

            Status.EVENT = EventType.VALIDA;
            Status.VALIDA = result.valid();

            if (hasValidationCallback) {
                String safeText = text == null ? "" : text;
                CallbackInvoker.call(callbacksInstance, screenInstance, "valida", acronym, result.valid(), safeText);
            }

            if (!result.valid() && validation.showMessage() && force && result.message() != null
                    && !result.message().isBlank()) {
                Platform.runLater(() -> MessageUtil.warn("Validação", result.message()));
            }
        }
    }
}
