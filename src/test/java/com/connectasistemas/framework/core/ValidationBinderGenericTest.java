package com.connectasistemas.framework.core;

import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.enums.ValidationDataType;
import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.internal.utils.ScreenAssembler;
import com.connectasistemas.framework.internal.utils.ScreenHierarchyRegistry;
import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;
import com.connectasistemas.framework.internal.utils.records.ScreenView;
import com.connectasistemas.framework.internal.validation.ValidationBinderGeneric;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link ValidationBinderGeneric}.
 * Camada 7 — verificação das regras de validação de dados.
 */
class ValidationBinderGenericTest {

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @AfterEach
    void tearDown() {
        ScreenManagerSharedData.resetScreenData();
        ScreenHierarchyRegistry.clear();
    }

    // ---- Cenário 1: maxLength limita o tamanho do texto no filterInput ----

    @Test
    @DisplayName("filterInput deve respeitar maxLength = 50")
    void shouldEnforceMaxLengthOnFilterInput() {
        ScreenView view = ScreenAssembler.compose(Example.class);
        Example screen = (Example) view.screenInstance();

        // filterInput tem @ScreenValidation(maxLength = 50)
        // O ValidationBinder aplica TextFormatter que bloqueia textos > 50
        TextFormatter<?> formatter = screen.filterInput.getTextFormatter();
        assertNotNull(formatter,
                "filterInput deve ter TextFormatter aplicado pela validação");

        // Tenta definir texto com 60 caracteres
        String longText = "A".repeat(60);
        screen.filterInput.setText(longText);

        // O TextFormatter deve impedir textos maiores que 50
        assertTrue(screen.filterInput.getText().length() <= 50,
                "filterInput não deve aceitar mais de 50 caracteres");
    }

    // ---- Cenário 2: required = true é declarado em projectNameField ----

    @Test
    @DisplayName("projectNameField deve ter required = true na anotação")
    void shouldMarkProjectNameFieldAsRequired() throws Exception {
        Field field = Example.class.getDeclaredField("projectNameField");
        ScreenValidation validation = field.getAnnotation(ScreenValidation.class);
        assertNotNull(validation, "projectNameField deve ter @ScreenValidation");
        assertTrue(validation.required(),
                "projectNameField deve ter required = true");
    }

    // ---- Cenário 3: versionField tem dataType INTEGER ----

    @Test
    @DisplayName("versionField deve ter dataType = INTEGER")
    void shouldDeclareIntegerDataTypeOnVersionField() throws Exception {
        Field field = Example.class.getDeclaredField("versionField");
        ScreenValidation validation = field.getAnnotation(ScreenValidation.class);
        assertNotNull(validation, "versionField deve ter @ScreenValidation");
        assertEquals(ValidationDataType.INTEGER, validation.dataType(),
                "versionField deve ter dataType INTEGER");
    }

    // ---- Cenário 4: versionField tem limites min=1 max=99 ----

    @Test
    @DisplayName("versionField deve ter minValue = 1 e maxValue = 99")
    void shouldDefineRangeForVersionField() throws Exception {
        Field field = Example.class.getDeclaredField("versionField");
        ScreenValidation validation = field.getAnnotation(ScreenValidation.class);
        assertNotNull(validation, "versionField deve ter @ScreenValidation");
        assertEquals(1, (int) validation.minValue(),
                "minValue deve ser 1");
        assertEquals(99, (int) validation.maxValue(),
                "maxValue deve ser 99");
    }

    // ---- Cenário 5: applyAll retorna false para node nulo ----

    @Test
    @DisplayName("applyAll deve retornar false para node nulo")
    void shouldReturnFalseForNullNode() {
        ValidationBinderGeneric binder = new ValidationBinderGeneric();
        ScreenValidation mockValidation = createSimpleValidation();

        boolean result = binder.applyAll(mockValidation, null, "test", new Object(), new Object());
        assertFalse(result, "applyAll deve retornar false quando node é nulo");
    }

    // ---- Cenário 6: applyAll retorna false para validation nula ----

    @Test
    @DisplayName("applyAll deve retornar false para validation nula")
    void shouldReturnFalseForNullValidation() {
        ValidationBinderGeneric binder = new ValidationBinderGeneric();
        TextField tf = new TextField();

        boolean result = binder.applyAll(null, tf, "test", new Object(), new Object());
        assertFalse(result, "applyAll deve retornar false quando validation é nula");
    }

    // ---- Cenário 7: applyAll aplica TextFormatter a um TextField ----

    @Test
    @DisplayName("applyAll deve aplicar TextFormatter a um TextField")
    void shouldApplyTextFormatterToTextField() {
        ValidationBinderGeneric binder = new ValidationBinderGeneric();
        ScreenValidation validation = createSimpleValidation();
        TextField tf = new TextField();

        boolean result = binder.applyAll(validation, tf, "test", new Object(), null);
        assertTrue(result, "applyAll deve retornar true para TextField válido");
        assertNotNull(tf.getTextFormatter(),
                "TextField deve ter TextFormatter após applyAll");
    }

    // ---- Cenário 8: Campo INTEGER rejeita letras ----

    @Test
    @DisplayName("Campo INTEGER deve rejeitar caracteres não numéricos")
    void shouldRejectLettersForIntegerField() {
        ScreenView view = ScreenAssembler.compose(Example.class);
        Example screen = (Example) view.screenInstance();

        // versionField é TextEntryLabel com INTEGER validation
        TextField versionTextField = screen.versionField.getTextField();
        assertNotNull(versionTextField.getTextFormatter(),
                "versionField deve ter TextFormatter após compose");

        // Define um texto numérico válido
        versionTextField.setText("5");
        assertEquals("5", versionTextField.getText(),
                "Texto numérico deve ser aceito");

        // Tenta definir texto com letras
        versionTextField.setText("abc");

        // O TextFormatter de INTEGER impede texto não numérico via política de caracteres
        // O campo pode ficar vazio ou manter o valor anterior ao rejeitar
        String text = versionTextField.getText();
        assertTrue(text.matches("-?\\d*") || text.isEmpty(),
                "Campo INTEGER não deve conter letras");
    }

    // ---- Cenário 9: validateOn referencia saveButton ----

    @Test
    @DisplayName("campos obrigatórios devem ter validateOn = 'saveButton'")
    void shouldReferenceValidateOnSaveButton() throws Exception {
        Field projectNameField = Example.class.getDeclaredField("projectNameField");
        ScreenValidation validation = projectNameField.getAnnotation(ScreenValidation.class);
        assertNotNull(validation, "projectNameField deve ter @ScreenValidation");
        assertEquals("saveButton", validation.validateOn(),
                "projectNameField deve validar no clique de saveButton");
    }

    /**
     * Cria uma ScreenValidation simples para testes isolados.
     */
    @SuppressWarnings("all")
    private ScreenValidation createSimpleValidation() {
        return new ScreenValidation() {
            @Override public Class<? extends Annotation> annotationType() { return ScreenValidation.class; }
            @Override public boolean required() { return false; }
            @Override public int maxLength() { return 100; }
            @Override public int minLength() { return 0; }
            @Override public double minValue() { return Long.MIN_VALUE; }
            @Override public double maxValue() { return Long.MAX_VALUE; }
            @Override public boolean showMessage() { return false; }
            @Override public String validateOn() { return ""; }
            @Override public ValidationDataType dataType() { return ValidationDataType.TEXT; }
            @Override public boolean allowLetters() { return true; }
            @Override public boolean allowNumbers() { return true; }
            @Override public boolean allowSymbols() { return true; }
            @Override public boolean allowWhitespace() { return true; }
            @Override public String datePattern() { return "dd/MM/yyyy"; }
            @Override public String minDate() { return ""; }
            @Override public String maxDate() { return ""; }
        };
    }
}
