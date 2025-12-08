package com.connectasistemas.framework.processor;

import java.lang.reflect.Field;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.interfaces.CustomElement;
import com.connectasistemas.framework.utils.ScreenMetadata;
import javafx.scene.layout.Region;

/**
 * Processa anotações @Screen e @ScreenField
 * OBS: Gerencia a criação de campos e da janela via reflexão
 */
public class AnnotationProcessor {

    // Armazena os metadados extraídos
    private final ScreenMetadata metadata = new ScreenMetadata();

    /**
     * Processa uma classe de tela anotada com @Screen
     *
     * @param cls Classe genérica a ser processada
     * @return metadata Dados/Configurações da janela
     */
    public ScreenMetadata processScreen(Class<?> cls) {

        // Verifica se a classe tem @Screen
        // OBS: Caso não possua a anotação retorna dados vazios
        if (!cls.isAnnotationPresent(Screen.class)) {
            return metadata;
        }

        // Lê a anotação @Screen
        Screen screen = cls.getAnnotation(Screen.class);

        // Define título e tamanho
        metadata.setTitle(screen.title());
        metadata.setWidth(screen.width());
        metadata.setHeight(screen.height());
        metadata.setRoot(screen.region());

        // Instância a classe de callbacks se houver declarada na anotação
        // OBS: Seguindo o MVC a classe de callbacks seria o Controller
        Class<?> cbClass = screen.callbacks();
        if (cbClass != null && cbClass != Void.class) {
            try {
                Object cbInstance = cbClass.getDeclaredConstructor().newInstance();
                metadata.setCallbackInstance(cbInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Processa campos com @ScreenField
        // OBS: Carrega todos os campos com seus determinados field no map
        processFields(cls);

        return metadata;
    }

    // Processa todos os campos anotados com @ScreenField

    /**
     * Processa uma classe de tela anotada com @ScreenElement
     * OBS: Extrai os elementos da classe recebida
     * @param cls Classe genérica a ser processada
     */
    private void processFields(Class<?> cls) {
        for (var field : cls.getDeclaredFields()) {
            // Verifica se o campo possui @ScreenField
            if (!field.isAnnotationPresent(ScreenField.class)) {
                continue;
            }

            // Obtém a anotação
            ScreenField f = field.getAnnotation(ScreenField.class);

            validateCustomField(field, f);

            // Adiciona no metadata
            metadata.addField(f.acronym(), field);
        }
    }

    private void validateCustomField(Field field, ScreenField definition) {
        Class<?> type = field.getType();
        boolean implementsCustom = CustomElement.class.isAssignableFrom(type);

        if (definition.custom() && !implementsCustom) {
            throw new IllegalStateException(String.format(
                    "O campo %s foi marcado como custom mas não implementa CustomElement.",
                    field.getName()));
        }

        if (!definition.custom() && implementsCustom) {
            throw new IllegalStateException(String.format(
                    "O campo %s implementa CustomElement mas não está marcado com custom=true.",
                    field.getName()));
        }

        if (implementsCustom && !Region.class.isAssignableFrom(type)) {
            throw new IllegalStateException(String.format(
                    "O campo %s precisa estender Region para ser usado como elemento customizado.",
                    field.getName()));
        }
    }
}
