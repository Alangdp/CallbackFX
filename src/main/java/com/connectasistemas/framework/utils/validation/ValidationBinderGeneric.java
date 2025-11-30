package com.connectasistemas.framework.utils.validation;

import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.enums.ValidationDataType;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.interfaces.ValidationBinder;
import com.connectasistemas.framework.utils.CallbackInvoker;
import com.connectasistemas.framework.utils.MessageUtil;
import com.connectasistemas.framework.utils.Status;
import com.connectasistemas.framework.utils.StringUtils;
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
                callbacksInstance != null && CallbackInvoker.exists(callbacksInstance, "valida", acronym));

        UnaryOperator<TextFormatter.Change> changeOperator = change -> {
            String newText = change.getControlNewText();

            if (!withinMaxLength(newText, validation)) {
                handler.publish(newText, false);
                return null;
            }

            if (!matchesCharacterPolicy(newText, validation)) {
                handler.publish(newText, false);
                return null;
            }

            boolean valid = evaluateText(newText, context);
            handler.publish(newText, valid);
            return change;
        };

        control.setTextFormatter(new TextFormatter<>(changeOperator));

        control.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                String text = control.getText();
                boolean valid = evaluateText(text, context);
                handler.publishOnBlur(text, valid);
            }
        });

        control.textProperty().addListener((obs, oldV, newV) -> {
            boolean valid = evaluateText(newV, context);
            handler.publish(newV, valid);
        });

        handler.publish(control.getText(), evaluateText(control.getText(), context));
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
    private boolean withinMaxLength(String text, ScreenValidation validation) {
        if (validation.maxLength() < 0) {
            return true;
        }

        boolean withinLimit = text == null || text.length() <= validation.maxLength();
        if (!withinLimit) {
            notifyValidationFailure(validation,
                    StringUtils.concat("Valor deve ter no máximo ", validation.maxLength(), " caracteres."));
        }
        return withinLimit;
    }

    /**
     * Verifica se o texto corresponde à política de caracteres definida
     * 
     * @param text       o texto a ser verificado
     * @param validation a anotação ScreenValidation contendo as regras
     * @return true se corresponder à política, false caso contrário
     */
    private boolean matchesCharacterPolicy(String text, ScreenValidation validation) {
        if (text == null || text.isEmpty()) {
            return true;
        }

        ValidationDataType dataType = validation.dataType();
        boolean allowed;
        String failureMessage = null;

        switch (dataType) {
            case INTEGER -> {
                allowed = text.matches("-?\\d*");
                if (!allowed) {
                    failureMessage = "Informe apenas números inteiros.";
                }
            }
            case DECIMAL -> {
                allowed = text.matches("-?\\d*(?:[\\.,]\\d*)?");
                if (!allowed) {
                    failureMessage = "Informe um número decimal válido.";
                }
            }
            case DATE -> {
                allowed = text.chars().allMatch(ch -> Character.isDigit(ch)
                        || ch == '/' || ch == '-' || ch == '.' || Character.isWhitespace(ch));
                if (!allowed) {
                    failureMessage = "Informe a data usando apenas dígitos e separadores (/, -, .).";
                }
            }
            case TEXT -> allowed = checkAllowances(text, validation);
            default -> allowed = true;
        }
        ;

        if (!allowed) {
            if (failureMessage != null) {
                notifyValidationFailure(validation, failureMessage);
            }
        }

        return allowed;
    }

    /**
     * Verifica se o texto contém apenas os tipos de caracteres permitidos
     * 
     * @param text       o texto a ser verificado
     * @param validation a anotação ScreenValidation contendo as regras
     * @return true se o texto estiver em conformidade, false caso contrário
     */
    private boolean checkAllowances(String text, ScreenValidation validation) {
        for (char ch : text.toCharArray()) {

            // Valida se é um caractere E deve validar ser permitido
            if (Character.isLetter(ch) && !validation.allowLetters()) {
                notifyValidationFailure(validation, "Letras não são permitidas para este campo.");
                return false;
            }

            // Valida se é um dígito E deve validar ser permitido
            if (Character.isDigit(ch) && !validation.allowNumbers()) {
                notifyValidationFailure(validation, "Números não são permitidos para este campo.");
                return false;
            }

            // Valida se é um espaço em branco E deve validar ser permitido
            if (Character.isWhitespace(ch) && !validation.allowWhitespace()) {
                notifyValidationFailure(validation, "Espaços em branco não são permitidos para este campo.");
                return false;
            }

            // Valida se é um símbolo E deve validar ser permitido
            if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch) && !validation.allowSymbols()) {
                notifyValidationFailure(validation, "Símbolos não são permitidos para este campo.");
                return false;
            }
        }

        return true;
    }

    /**
     * Avalia o texto com base nas regras de validação fornecidas no contexto
     * 
     * @param text    o texto a ser avaliado
     * @param context o contexto de validação contendo as regras
     * @return true se o texto for válido, false caso contrário
     */
    private boolean evaluateText(String text, ValidationContext context) {
        ScreenValidation validation = context.validation();

        if (text == null || text.isEmpty()) {
            if (validation.required()) {
                notifyValidationFailure(validation, "Campo obrigatório.");
                return false;
            }
            return true;
        }

        // Verifica comprimento mínimo
        if (validation.minLength() > 0 && text.length() < validation.minLength()) {
            notifyValidationFailure(validation,
                    StringUtils.concat("Valor deve conter pelo menos ", validation.minLength(), " caracteres."));
            return false;
        }

        return switch (validation.dataType()) {
            case INTEGER -> evaluateInteger(text, validation);
            case DECIMAL -> evaluateDecimal(text, validation);
            case DATE -> evaluateDate(text, context);
            case TEXT -> true;
        };
    }

    /**
     * Avalia se o texto representa um inteiro dentro dos limites definidos
     * 
     * @param text       o texto a ser avaliado
     * @param validation as regras de validação
     * @return true se o texto for um inteiro válido, false caso contrário
     */
    private boolean evaluateInteger(String text, ScreenValidation validation) {
        if ("-".equals(text) || "+".equals(text)) {
            notifyValidationFailure(validation, "Informe um número inteiro válido.");
            return false;
        }

        try {
            long value = Long.parseLong(text);
            if (value < validation.minValue()) {
                notifyValidationFailure(validation,
                        StringUtils.concat("Valor mínimo permitido é ", formatNumber(validation.minValue()), "."));
                return false;
            }

            boolean withinMax = value <= validation.maxValue();
            if (!withinMax) {
                notifyValidationFailure(validation,
                        StringUtils.concat("Valor máximo permitido é ", formatNumber(validation.maxValue()), "."));
            }
            return withinMax;
        } catch (NumberFormatException ex) {
            notifyValidationFailure(validation, "Valor inteiro inválido.");
            return false;
        }
    }

    /**
     * Avalia se o texto representa um decimal dentro dos limites definidos
     * 
     * @param text       o texto a ser avaliado
     * @param validation as regras de validação
     * @return true se o texto for um decimal válido, false caso contrário
     */
    private boolean evaluateDecimal(String text, ScreenValidation validation) {
        if ("-".equals(text) || "+".equals(text) || ".".equals(text) || ",".equals(text)) {
            notifyValidationFailure(validation, "Informe um valor decimal válido.");
            return false;
        }

        try {
            double value = parseDecimal(text);
            if (Double.isNaN(value)) {
                notifyValidationFailure(validation, "Valor decimal inválido.");
                return false;
            }

            if (value < validation.minValue()) {
                notifyValidationFailure(validation,
                        StringUtils.concat("Valor mínimo permitido é ", formatNumber(validation.minValue()), "."));
                return false;
            }

            boolean withinMax = value <= validation.maxValue();
            if (!withinMax) {
                notifyValidationFailure(validation,
                        StringUtils.concat("Valor máximo permitido é ", formatNumber(validation.maxValue()), "."));
            }
            return withinMax;
        } catch (NumberFormatException ex) {
            notifyValidationFailure(validation, "Valor decimal inválido.");
            return false;
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
    private boolean evaluateDate(String text, ValidationContext context) {
        try {
            LocalDate value = LocalDate.parse(text, context.formatter());

            if (context.minDate() != null && value.isBefore(context.minDate())) {
                notifyValidationFailure(context.validation(),
                        StringUtils.concat("Data mínima permitida é ", context.formatter().format(context.minDate()),
                                "."));
                return false;
            }

            if (context.maxDate() != null && value.isAfter(context.maxDate())) {
                notifyValidationFailure(context.validation(),
                        StringUtils.concat("Data máxima permitida é ", context.formatter().format(context.maxDate()),
                                "."));
                return false;
            }

            return true;
        } catch (DateTimeParseException ex) {
            ScreenValidation validation = context.validation();
            notifyValidationFailure(validation,
                    StringUtils.concat("Data ", text, " é inválida. Use o formato ", validation.datePattern(), "."));
            return false;
        }
    }

    /**
     * Notifica falha de validação exibindo uma mensagem ao usuário, se configurado para tal
     * 
     * @param validation a anotação ScreenValidation contendo as regras
     * @param message    a mensagem de falha a ser exibida
     */
    private void notifyValidationFailure(ScreenValidation validation, String message) {
        if (!validation.showMessage()) {
            return;
        }

        MessageUtil.warn("Validação", message);
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
        private final boolean hasValidationCallback;

        private String lastText;
        private Boolean lastValid;

        private ValidationHandler(Object screenInstance,
                Object callbacksInstance,
                String acronym,
                boolean hasValidationCallback) {
            this.screenInstance = screenInstance;
            this.callbacksInstance = callbacksInstance;
            this.acronym = acronym;
            this.hasValidationCallback = hasValidationCallback;
        }

        private void publish(String text, boolean valid) {
            publishInternal(text, valid, false);
        }

        private void publishOnBlur(String text, boolean valid) {
            publishInternal(text, valid, true);
        }

        private void publishInternal(String text, boolean valid, boolean force) {
            if (!force && Objects.equals(lastText, text) && Objects.equals(lastValid, valid)) {
                return;
            }

            lastText = text;
            lastValid = valid;

            Status.EVENT = EventType.VALIDA;
            Status.VALIDA = valid;

            if (hasValidationCallback) {
                String safeText = text == null ? "" : text;
                CallbackInvoker.call(callbacksInstance, screenInstance, "valida", acronym, valid, safeText);
            }
        }
    }
}
