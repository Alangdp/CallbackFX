package com.connectasistemas.framework.utils.validation;

import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.enums.ValidationDataType;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.interfaces.ValidationBinder;
import com.connectasistemas.framework.utils.CallbackInvoker;
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
 * Binder genérico responsável por aplicar validações declaradas via {@link ScreenValidation}.
 */
public class ValidationBinderGeneric implements ValidationBinder {

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

    private TextInputControl resolveTextInputControl(Node node) {
        if (node instanceof TextInputControl control) {
            return control;
        }

        if (node instanceof TextEntryLabel label) {
            return label.getTextField();
        }

        return null;
    }

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

    private boolean withinMaxLength(String text, ScreenValidation validation) {
        if (validation.maxLength() < 0) {
            return true;
        }
        return text == null || text.length() <= validation.maxLength();
    }

    private boolean matchesCharacterPolicy(String text, ScreenValidation validation) {
        if (text == null || text.isEmpty()) {
            return true;
        }

        ValidationDataType dataType = validation.dataType();
        return switch (dataType) {
            case INTEGER -> text.matches("-?\\d*");
            case DECIMAL -> text.matches("-?\\d*(?:[\\.,]\\d*)?");
            case DATE -> text.chars().allMatch(ch -> Character.isDigit(ch)
                    || ch == '/' || ch == '-' || ch == '.' || Character.isWhitespace(ch));
            case TEXT -> checkAllowances(text, validation);
        };
    }

    private boolean checkAllowances(String text, ScreenValidation validation) {
        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch) && !validation.allowLetters()) {
                return false;
            }

            if (Character.isDigit(ch) && !validation.allowNumbers()) {
                return false;
            }

            if (Character.isWhitespace(ch) && !validation.allowWhitespace()) {
                return false;
            }

            if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch) && !validation.allowSymbols()) {
                return false;
            }
        }

        return true;
    }

    private boolean evaluateText(String text, ValidationContext context) {
        ScreenValidation validation = context.validation();

        if (text == null || text.isEmpty()) {
            return !validation.required();
        }

        if (validation.minLength() > 0 && text.length() < validation.minLength()) {
            return false;
        }

        return switch (validation.dataType()) {
            case INTEGER -> evaluateInteger(text, validation);
            case DECIMAL -> evaluateDecimal(text, validation);
            case DATE -> evaluateDate(text, context);
            case TEXT -> true;
        };
    }

    private boolean evaluateInteger(String text, ScreenValidation validation) {
        if ("-".equals(text) || "+".equals(text)) {
            return false;
        }

        try {
            long value = Long.parseLong(text);
            if (value < validation.minValue()) {
                return false;
            }

            return value <= validation.maxValue();
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean evaluateDecimal(String text, ScreenValidation validation) {
        if ("-".equals(text) || "+".equals(text) || ".".equals(text) || ",".equals(text)) {
            return false;
        }

        try {
            double value = parseDecimal(text);
            if (Double.isNaN(value)) {
                return false;
            }

            if (value < validation.minValue()) {
                return false;
            }

            return value <= validation.maxValue();
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private double parseDecimal(String text) {
        if (text == null || text.isBlank()) {
            return Double.NaN;
        }

        String sanitized = text.replace(',', '.');
        return Double.parseDouble(sanitized);
    }

    private boolean evaluateDate(String text, ValidationContext context) {
        try {
            LocalDate value = LocalDate.parse(text, context.formatter());

            if (context.minDate() != null && value.isBefore(context.minDate())) {
                return false;
            }

            if (context.maxDate() != null && value.isAfter(context.maxDate())) {
                return false;
            }

            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private record ValidationContext(ScreenValidation validation,
                                     DateTimeFormatter formatter,
                                     LocalDate minDate,
                                     LocalDate maxDate) {
    }

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
